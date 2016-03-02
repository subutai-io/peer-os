package lib

import (
	"bufio"
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/influxdata/influxdb/client/v2"
	"github.com/subutai-io/Subutai/agent/config"
	"github.com/subutai-io/Subutai/agent/lib/container"
	"github.com/subutai-io/Subutai/agent/lib/fs"
	"github.com/subutai-io/Subutai/agent/log"
	"io/ioutil"
	"os"
	"os/exec"
	"regexp"
	"runtime"
	"strconv"
	"strings"
	"time"
)

var (
	clnt     client.Client
	interval string
)

type hostStat struct {
	Host string `json:"host"`
	CPU  struct {
		Model     string      `json:"model"`
		CoreCount int         `json:"coreCount"`
		Idle      interface{} `json:"idle"`
		Frequency string      `json:"frequency"`
	} `json:"CPU"`
	Disk struct {
		Total interface{} `json:"total"`
		Used  interface{} `json:"used"`
	} `json:"Disk"`
	RAM struct {
		Free  interface{} `json:"free"`
		Total interface{} `json:"total"`
	} `json:"RAM"`
}

type quotaUsage struct {
	Container string `json:"container"`
	CPU       int    `json:"cpu"`
	Disk      struct {
		Home   int `json:"home"`
		Opt    int `json:"opt"`
		Rootfs int `json:"rootfs"`
		Var    int `json:"var"`
	} `json:"Disk"`
	RAM int `json:"ram"`
}

func initdb() {
	var err error
	clnt, err = client.NewHTTPClient(client.HTTPConfig{
		Addr:               "https://" + config.Influxdb.Server + ":8086",
		Username:           config.Influxdb.User,
		Password:           config.Influxdb.Pass,
		InsecureSkipVerify: true,
	})
	log.Check(log.FatalLevel, "Initialize db connection", err)
	return
}

func queryDB(cmd string) (res []client.Result, err error) {
	q := client.Query{
		Command:  cmd,
		Database: config.Influxdb.Db,
	}
	if response, err := clnt.Query(q); err == nil {
		if response.Error() != nil {
			return res, response.Error()
		}
		res = response.Results
	}
	if len(res) == 0 || len(res[0].Series) == 0 {
		err = errors.New("No result")
	}
	return res, err
}

func n2nLoad(i string) string {
	res, err := queryDB("SELECT non_negative_derivative(mean(value),1s) as bps FROM host_net WHERE iface =~ /n2n*/ and time > now() - " + i + " GROUP BY time(1m), iface, type fill(none)")
	if err != nil {
		log.Warn("No data received for n2n load")
		return ""
	}
	b, _ := json.Marshal(res[0])
	return string(b)
}

func proxyLoad(i string) string {
	res, err := queryDB("SELECT sum(\"in\") as \"in\",sum(out) as out FROM rproxy WHERE time > now() - " + i + " GROUP BY time(1m), domain, ip fill(none)")
	if err != nil {
		log.Warn("No data received for reverse proxy load")
		return ""
	}
	b, _ := json.Marshal(res[0])
	return string(b)
}

func ramLoad(h string) (memfree, memtotal interface{}) {
	file, err := os.Open("/proc/meminfo")
	defer file.Close()
	if log.Check(log.WarnLevel, "Reading /proc/meminfo", err) {
		return
	}
	scanner := bufio.NewScanner(bufio.NewReader(file))
	for scanner.Scan() {
		line := strings.Fields(strings.Replace(scanner.Text(), ":", "", -1))
		value, _ := strconv.Atoi(line[1])
		if line[0] == "MemTotal" {
			memtotal = value * 1024
		} else if line[0] == "MemFree" {
			memfree = value * 1024
		}
	}
	return
}

func getCPUstat() (idle, total uint64) {
	contents, err := ioutil.ReadFile("/proc/stat")
	if err != nil {
		return
	}
	lines := strings.Split(string(contents), "\n")
	for _, line := range lines {
		fields := strings.Fields(line)
		if fields[0] == "cpu" {
			numFields := len(fields)
			for i := 1; i < numFields; i++ {
				val, err := strconv.ParseUint(fields[i], 10, 64)
				if err != nil {
					fmt.Println("Error: ", i, fields[i], err)
				}
				total += val
				if i == 4 {
					idle = val
				}
			}
			return
		}
	}
	return
}

func cpuLoad(h string) interface{} {
	res, err := queryDB("SELECT non_negative_derivative(mean(value),1s) FROM host_cpu WHERE hostname =~ /^" + h + "$/ AND type =~ /idle/ AND time > now() - 1m GROUP BY time(10s), type, hostname fill(none)")
	if err == nil && len(res) > 0 && len(res[0].Series) > 0 && len(res[0].Series[0].Values) > 0 && len(res[0].Series[0].Values[0]) > 1 {
		return res[0].Series[0].Values[0][1]
	}
	idle0, total0 := getCPUstat()
	time.Sleep(3 * time.Second)
	idle1, total1 := getCPUstat()
	cpuUsage := 100 * (float64(total1-total0) - float64(idle1-idle0)) / float64(total1-total0)
	return 100 - cpuUsage
}

func diskLoad(h string) (disktotal, diskused interface{}) {
	out, _ := exec.Command("df", "-TB1").Output()
	scanner := bufio.NewScanner(bytes.NewReader(out))
	for scanner.Scan() {
		line := strings.Fields(scanner.Text())
		if strings.HasPrefix(line[1], "btrfs") {
			disktotal, _ = strconv.Atoi(line[3])
			diskused, _ = strconv.Atoi(line[2])
			break
		}
	}
	return
}

func cpuQuotaUsage(h string) int {
	cpuCurLoad, err := queryDB("SELECT non_negative_derivative(mean(value), 1s) FROM lxc_cpu WHERE time > now() - 1m and lxc =~ /^" + h + "$/ GROUP BY time(10s), type fill(none)")
	if err != nil {
		log.Error("No data received for container cpu load")
	}
	sys, err := cpuCurLoad[0].Series[0].Values[0][1].(json.Number).Float64()
	user, err := cpuCurLoad[0].Series[1].Values[0][1].(json.Number).Float64()
	log.Check(log.FatalLevel, "Decoding cpu load", err)
	cpuUsage := 0
	if container.QuotaCPU(h, "") != 0 {
		cpuUsage = (int(sys+user) * 100) / container.QuotaCPU(h, "")
	}

	return cpuUsage
}

func read(path string) (i int) {
	f, err := os.Open(path)
	log.Check(log.FatalLevel, "Reading "+path, err)

	scanner := bufio.NewScanner(bufio.NewReader(f))
	for scanner.Scan() {
		i, err = strconv.Atoi(scanner.Text())
		log.Check(log.FatalLevel, "Converting string", err)
	}
	return
}

func ramQuotaUsage(h string) int {
	u := read("/sys/fs/cgroup/memory/lxc/" + h + "/memory.usage_in_bytes")
	l := read("/sys/fs/cgroup/memory/lxc/" + h + "/memory.limit_in_bytes")

	ramUsage := 0
	if l != 0 {
		ramUsage = u * 100 / l
	}

	return ramUsage
}

func diskQuotaUsage(path string) int {
	u, err := strconv.Atoi(fs.Stat(path, "usage", true))
	if err != nil {
		u = 0
	}

	l, err := strconv.Atoi(fs.Stat(path, "quota", true))
	if err != nil {
		l = 0
	}

	diskUsage := 0
	if l != 0 {
		diskUsage = u * 100 / l
	}

	return diskUsage
}

func quota(h string) string {

	usage := new(quotaUsage)
	usage.Container = h
	usage.CPU = cpuQuotaUsage(h)
	usage.RAM = ramQuotaUsage(h)
	usage.Disk.Rootfs = diskQuotaUsage(h + "/rootfs")
	usage.Disk.Home = diskQuotaUsage(h + "/home")
	usage.Disk.Opt = diskQuotaUsage(h + "/opt")
	usage.Disk.Var = diskQuotaUsage(h + "/var")

	a, err := json.Marshal(usage)
	if err != nil {
		log.Warn("Cannot marshal sysload result json")
		return ""
	}

	return string(a)
}

func sysLoad(h string) string {

	result := new(hostStat)
	result.Host = h
	result.CPU.Idle = cpuLoad(h)
	result.CPU.Model = grep("model name", "/proc/cpuinfo")
	result.CPU.CoreCount = runtime.NumCPU()
	result.CPU.Frequency = grep("cpu MHz", "/proc/cpuinfo")
	result.RAM.Free, result.RAM.Total = ramLoad(h)
	result.Disk.Used, result.Disk.Total = diskLoad(h)

	a, err := json.Marshal(result)
	if err != nil {
		log.Warn("Cannot marshal sysload result json")
		return ""
	}

	return string(a)
}

func grep(str, filename string) string {
	regex, err := regexp.Compile(str)
	if err != nil {
		log.Warn("Cannot compile regexp for: " + str)
		return ""
	}
	fh, err := os.Open(filename)
	if err != nil {
		log.Warn("Cannot open " + filename)
		return ""
	}
	f := bufio.NewReader(fh)

	defer fh.Close()
	buf := make([]byte, 64)
	for {
		buf, _, err = f.ReadLine()
		if err != nil {
			log.Warn("Cannot read line from file")
			return ""
		}
		if regex.MatchString(string(buf)) {
			line := strings.Split(string(buf), ":")
			return line[1]
		}
	}
}

func Stats(command, host, interval string) {
	initdb()
	if len(interval) == 0 {
		interval = "10m"
	}

	switch command {
	case "n2n":
		fmt.Println(n2nLoad(interval))
	case "proxy":
		fmt.Println(proxyLoad(interval))
	case "quota":
		fmt.Println(quota(host))
	case "system":
		fmt.Println(sysLoad(host))
	}
}
