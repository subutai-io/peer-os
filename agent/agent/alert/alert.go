package alert

import (
	"bufio"
	"bytes"
	"io/ioutil"
	"os/exec"
	"runtime"
	"strconv"
	"strings"
	"time"

	"github.com/subutai-io/base/agent/agent/container"
	"github.com/subutai-io/base/agent/config"
)

type Values struct {
	Current int `json:"current,omitempty"`
	Quota   int `json:"quota,omitempty"`
}

type HDD struct {
	Partition string `json:"partition"`
	Current   int    `json:"current"`
	Quota     int    `json:"quota"`
}

type Load struct {
	Container string  `json:"id,omitempty"`
	CPU       *Values `json:"cpu,omitempty"`
	RAM       *Values `json:"ram,omitempty"`
	Disk      []HDD   `json:"hdd,omitempty"`
}

var (
	cpu   = make(map[string][]int)
	stats = make(map[string]Load)
)

func read(path string) (i int) {
	out, _ := ioutil.ReadFile(path)
	i, _ = strconv.Atoi(strings.TrimSpace(string(out)))
	return
}

func id() (list map[string]string) {
	list = map[string]string{}
	out, _ := exec.Command("btrfs", "subvolume", "list", config.Agent.LxcPrefix).Output()
	scanner := bufio.NewScanner(bytes.NewReader(out))
	for scanner.Scan() {
		line := strings.Fields(scanner.Text())
		if len(line) > 8 {
			list[line[8]] = line[1]
		}
	}
	return
}

func stat() string {
	out, _ := exec.Command("btrfs", "qgroup", "show", "-r", "--raw", config.Agent.LxcPrefix).Output()
	return string(out)
}

func ramQuota(cont string) []int {
	u := read("/sys/fs/cgroup/memory/lxc/" + cont + "/memory.usage_in_bytes")
	l := read("/sys/fs/cgroup/memory/lxc/" + cont + "/memory.limit_in_bytes")

	var ramUsage = []int{0, l / 1024 / 1024}
	if l != 0 {
		ramUsage[0] = u * 100 / l
	}

	return ramUsage
}

func quotaCPU(name string) int {
	cfsPeriod := 100000
	cfs_quota_us, err := ioutil.ReadFile("/sys/fs/cgroup/cpu,cpuacct/lxc/" + name + "/cpu.cfs_quota_us")
	if err != nil {
		return -1
	}
	quota, _ := strconv.Atoi(strings.TrimSpace(string(cfs_quota_us)))
	return quota * 100 / cfsPeriod / runtime.NumCPU()
}

func cpuLoad(cont string) []int {
	avgload := []int{0, 0}
	if len(cpu[cont]) == 0 {
		cpu[cont] = []int{0, 0, 0, 0, 0}
	}
	ticks, err := ioutil.ReadFile("/sys/fs/cgroup/cpuacct/lxc/" + cont + "/cpuacct.stat")
	if err != nil {
		return avgload
	}

	tick := strings.Fields(string(ticks))
	if len(tick) != 4 {
		return avgload
	}

	usertick, _ := strconv.Atoi(tick[1])
	systick, _ := strconv.Atoi(tick[3])
	cpu[cont] = append([]int{usertick + systick}, cpu[cont][0:4]...)

	if cpu[cont][4] == 0 {
		return avgload
	}
	avgload[0] = (cpu[cont][0] - cpu[cont][4]) / runtime.NumCPU() / 20
	avgload[1] = quotaCPU(cont)
	if avgload[1] != 0 {
		avgload[0] = avgload[0] * 100 / avgload[1]
	}
	return avgload
}

func diskQuota(mountid, diskMap string) []int {
	var u, l string
	for _, line := range strings.Split(diskMap, "\n") {
		row := strings.Fields(line)
		if len(row) > 3 {
			if row[0] == "0/"+mountid {
				u, l = row[2], row[3]
			}
		}
	}

	used, err := strconv.Atoi(u)
	if err != nil {
		used = 0
	}

	limit, err := strconv.Atoi(l)
	if err != nil {
		limit = 0
	}

	var diskUsage = []int{0, limit / 1024 / 1024 / 1024}
	if limit != 0 {
		diskUsage[0] = used * 100 / limit
	}
	return diskUsage
}

func AlertProcessing() {
	for {
		stats = Alert()
		time.Sleep(time.Second * 5)
	}
}

func Alert() (load map[string]Load) {
	load = make(map[string]Load)
	diskMap := stat()
	diskIDs := id()

	files, _ := ioutil.ReadDir("/sys/fs/cgroup/cpu/lxc/")
	for _, cont := range files {
		if !cont.IsDir() {
			continue
		}

		cpuValues := cpuLoad(cont.Name())
		ramValues := ramQuota(cont.Name())

		disk := []HDD{}
		for _, v := range []string{"rootfs", "opt", "var", "home"} {
			diskValues := diskQuota(diskIDs["lib/lxc/"+cont.Name()+"/"+v], diskMap)
			disk = append(disk, HDD{Current: diskValues[0], Quota: diskValues[1], Partition: v})
		}

		load[cont.Name()] = Load{
			CPU:  &Values{Current: cpuValues[0], Quota: cpuValues[1]},
			RAM:  &Values{Current: ramValues[0], Quota: ramValues[1]},
			Disk: disk,
		}
	}
	return load
}

func CurrentAlerts(list []container.Container) []Load {
	var load []Load
	for _, v := range list {
		var item Load

		if stats[v.Name].CPU != nil && stats[v.Name].CPU.Current > 80 {
			item.CPU = &Values{Current: stats[v.Name].CPU.Current, Quota: stats[v.Name].CPU.Quota}
		}
		if stats[v.Name].RAM != nil && stats[v.Name].RAM.Current > 80 {
			item.RAM = &Values{Current: stats[v.Name].RAM.Current, Quota: stats[v.Name].RAM.Quota}
		}

		for _, v := range stats[v.Name].Disk {
			if v.Current > 80 {
				item.Disk = append(item.Disk, HDD{Current: v.Current, Quota: v.Quota, Partition: v.Partition})
			}
		}

		if item.CPU != nil || item.RAM != nil || len(item.Disk) > 0 {
			item.Container = v.Id
			load = append(load, item)
		}
	}
	return load
}
