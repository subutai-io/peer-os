package lib

import (
	"bufio"
	"bytes"
	"io/ioutil"
	"os"
	"os/exec"
	"runtime"
	"strconv"
	"strings"
	"time"

	"github.com/influxdata/influxdb/client/v2"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
)

var (
	traff     = []string{"in", "out"}
	cgtype    = []string{"cpuacct", "memory"}
	metrics   = []string{"total", "used", "available"}
	cpu       = []string{"user", "nice", "system", "idle", "iowait"}
	lxcmemory = map[string]bool{"cache": true, "rss": true, "Cached": true, "MemFree": true}
	memory    = map[string]bool{"Active": true, "Buffers": true, "Cached": true, "MemFree": true}
)

var (
	dbclient client.Client
	bp       client.BatchPoints
)

func Collect() {
	initInfluxdb()
	for {
		_, _, err := dbclient.Ping(time.Second)
		if err == nil {
			bp, _ = client.NewBatchPoints(client.BatchPointsConfig{Database: config.Influxdb.Db, RetentionPolicy: "hour"})
			netStat()
			cgroupStat()
			btrfsStat()
			diskFree()
			cpuStat()
			memStat()
		}
		if err != nil || dbclient.Write(bp) != nil {
			initInfluxdb()
		}
		time.Sleep(time.Second * 30)
	}
}

func initInfluxdb() {
	dbclient, _ = client.NewHTTPClient(client.HTTPConfig{
		Addr:               "https://" + config.Influxdb.Server + ":8086",
		Username:           config.Influxdb.User,
		Password:           config.Influxdb.Pass,
		InsecureSkipVerify: true,
	})
}

func parsefile(hostname, lxc, cgtype, filename string) {
	file, err := os.Open(filename)
	if err != nil {
		return
	}
	defer file.Close()
	scanner := bufio.NewScanner(bufio.NewReader(file))
	for scanner.Scan() {
		line := strings.Split(scanner.Text(), " ")
		value, _ := strconv.ParseInt(line[1], 10, 62)
		if cgtype == "memory" && lxcmemory[line[0]] {
			point, _ := client.NewPoint("lxc_"+cgtype,
				map[string]string{"hostname": lxc, "type": line[0]},
				map[string]interface{}{"value": value / int64(runtime.NumCPU())},
				time.Now())
			bp.AddPoint(point)
		} else if cgtype == "cpuacct" {
			point, _ := client.NewPoint("lxc_cpu",
				map[string]string{"hostname": lxc, "type": line[0]},
				map[string]interface{}{"value": value / int64(runtime.NumCPU())},
				time.Now())
			bp.AddPoint(point)
		}
	}

}

func cgroupStat() {
	hostname, _ := os.Hostname()
	for _, item := range cgtype {
		path := "/sys/fs/cgroup/" + item + "/lxc/"
		files, _ := ioutil.ReadDir(path)
		for _, f := range files {
			if f.IsDir() {
				parsefile(hostname, f.Name(), item, path+f.Name()+"/"+item+".stat")
			}
		}
	}
}

func netStat() {
	lxcnic := make(map[string]string)
	files, _ := ioutil.ReadDir(config.Agent.LxcPrefix)
	for _, f := range files {
		lxcnic[container.GetConfigItem(config.Agent.LxcPrefix+f.Name()+"/config", "lxc.network.veth.pair")] = f.Name()
	}

	out, err := ioutil.ReadFile("/proc/net/dev")
	if err != nil {
		return
	}
	scanner := bufio.NewScanner(bytes.NewReader(out))
	traffic := make([]int, 2)
	for scanner.Scan() {
		if strings.Contains(scanner.Text(), ":") {
			line := strings.Fields(scanner.Text())
			traffic[0], _ = strconv.Atoi(line[1])
			traffic[1], _ = strconv.Atoi(line[9])
			nicname := strings.Split(line[0], ":")[0]

			metric := "host_net"
			hostname, _ := os.Hostname()
			if lxcnic[nicname] != "" {
				metric = "lxc_net"
				hostname = lxcnic[nicname]
			}

			for i := range traffic {
				point, _ := client.NewPoint(metric,
					map[string]string{"hostname": hostname, "iface": nicname, "type": traff[i]},
					map[string]interface{}{"value": traffic[i]},
					time.Now())
				bp.AddPoint(point)
			}
		}
	}
}

func btrfsStat() {
	list := make(map[string]string)
	out, _ := exec.Command("btrfs", "subvolume", "list", config.Agent.LxcPrefix).Output()
	scanner := bufio.NewScanner(bytes.NewReader(out))
	for scanner.Scan() {
		line := strings.Fields(scanner.Text())
		list["0/"+line[1]] = line[8]
	}
	out, _ = exec.Command("btrfs", "qgroup", "show", "-r", "--raw", config.Agent.LxcPrefix).Output()
	scanner = bufio.NewScanner(bytes.NewReader(out))
	for scanner.Scan() {
		line := strings.Fields(scanner.Text())
		path := strings.Split(list[line[0]], "/")
		if len(path) > 3 {
			value, _ := strconv.Atoi(line[2])
			point, _ := client.NewPoint("lxc_disk",
				map[string]string{"hostname": path[2], "mount": path[3], "type": "used"},
				map[string]interface{}{"value": value},
				time.Now())
			bp.AddPoint(point)
		}
	}
}

func diskFree() {
	hostname, _ := os.Hostname()
	out, _ := exec.Command("df", "-B1").Output()
	scanner := bufio.NewScanner(bytes.NewReader(out))
	for scanner.Scan() {
		line := strings.Fields(scanner.Text())
		if strings.HasPrefix(line[0], "/dev") {
			for i := range metrics {
				value, _ := strconv.Atoi(line[i+1])
				point, _ := client.NewPoint("host_disk",
					map[string]string{"hostname": hostname, "mount": line[5], "type": metrics[i]},
					map[string]interface{}{"value": value},
					time.Now())
				bp.AddPoint(point)
			}
		}
	}
}

func memStat() {
	hostname, _ := os.Hostname()
	file, err := os.Open("/proc/meminfo")
	if err != nil {
		return
	}
	defer file.Close()
	scanner := bufio.NewScanner(bufio.NewReader(file))
	for scanner.Scan() {
		line := strings.Fields(strings.Replace(scanner.Text(), ":", "", -1))
		value, _ := strconv.ParseInt(line[1], 10, 62)
		if memory[line[0]] {
			point, _ := client.NewPoint("host_memory",
				map[string]string{"hostname": hostname, "type": line[0]},
				map[string]interface{}{"value": value * 1024},
				time.Now())
			bp.AddPoint(point)
		}
	}
}

func cpuStat() {
	hostname, _ := os.Hostname()
	file, err := os.Open("/proc/stat")
	if err != nil {
		return
	}
	defer file.Close()
	scanner := bufio.NewScanner(bufio.NewReader(file))
	for scanner.Scan() {
		line := strings.Fields(scanner.Text())
		if line[0] == "cpu" {
			for i := range cpu {
				value, _ := strconv.Atoi(line[i+1])
				point, _ := client.NewPoint("host_cpu",
					map[string]string{"hostname": hostname, "type": cpu[i]},
					map[string]interface{}{"value": value / runtime.NumCPU()},
					time.Now())
				bp.AddPoint(point)
			}
		}
	}
}
