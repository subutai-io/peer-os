package container

import (
	"bufio"
	"errors"
	"gopkg.in/lxc/go-lxc.v2"
	"os"
	"runtime"
	"strconv"
	"subutai/config"
	"subutai/lib/fs"
	"subutai/lib/net"
	"subutai/log"
)

// getList returns list of all containers
func All() []string {
	return lxc.DefinedContainerNames(config.Agent.LxcPrefix)
}

func IsTemplate(name string) bool {
	if fs.IsSubvolumeReadonly(config.Agent.LxcPrefix + name + "/rootfs/") {
		return true
	}
	return false
}

func Templates() (containers []string) {
	for _, name := range All() {
		if IsTemplate(name) {
			containers = append(containers, name)
		}
	}
	return
}

func Containers() (containers []string) {
	for _, name := range All() {
		if !IsTemplate(name) {
			containers = append(containers, name)
		}
	}
	return
}

func IsContainer(name string) bool {
	for _, item := range All() {
		if name == item {
			return true
		}
	}
	return false
}

func State(name string) (state string) {
	containers := lxc.Containers(config.Agent.LxcPrefix)
	for i := range containers {
		if containers[i].Name() == name {
			return containers[i].State().String()
		}
	}
	return "UNKNOWN"
}

func Start(name string) {
	c, err := lxc.NewContainer(name, config.Agent.LxcPrefix)
	log.Check(log.FatalLevel, "Looking for container "+name, err)

	c.Start()
	// err = c.Start()
	// log.Check(log.FatalLevel, "Starting container "+name, err)
}
func Stop(name string) {
	c, err := lxc.NewContainer(name, config.Agent.LxcPrefix)
	log.Check(log.FatalLevel, "Looking for container "+name, err)

	c.Stop()
}

func AttachExec(name string, command []string) (output []string, err error) {
	if !IsContainer(name) {
		return output, errors.New("Container does not exists")
	}

	container, _ := lxc.NewContainer(name, config.Agent.LxcPrefix)
	if container.State() != lxc.RUNNING {
		return output, errors.New("Container is " + container.State().String())
	}

	buf_r, buf_w, _ := os.Pipe()
	container.RunCommand(command, lxc.AttachOptions{
		Namespaces: -1,
		UID:        0,
		GID:        0,
		StdoutFd:   buf_w.Fd(),
	})

	buf_w.Close()
	defer buf_r.Close()

	out := bufio.NewScanner(buf_r)
	for out.Scan() {
		output = append(output, out.Text())
	}

	return output, nil
}

func Destroy(name string) {
	c, err := lxc.NewContainer(name, config.Agent.LxcPrefix)
	log.Check(log.FatalLevel, "Creating container object", err)

	if c.State() == lxc.RUNNING {
		err := c.Stop()
		log.Check(log.FatalLevel, "Stopping container", err)
	}

	fs.SubvolumeDestroy("lxc/" + name + "-opt")
	fs.SubvolumeDestroy("lxc-data/" + name + "-home")
	fs.SubvolumeDestroy("lxc-data/" + name + "-var")

	err = c.Destroy()
	log.Check(log.FatalLevel, "Destroying container", err)

	log.Info(name + " destroyed")
}

func GetParent(name string) string {
	if !IsContainer(name) {
		return "Container does not exists"
	}
	// c, _ := lxc.NewContainer(name)
	configFileName := config.Agent.LxcPrefix + name + "/config"
	// return GetConfigItem(c.ConfigFileName(), "subutai.parent")
	return GetConfigItem(configFileName, "subutai.parent")
}

func Clone(parent, child string) {
	var backend lxc.BackendStore
	backend.Set("btrfs")

	a, err := lxc.NewContainer(parent, config.Agent.LxcPrefix)
	log.Check(log.FatalLevel, "Looking for container "+parent, err)

	err = a.Clone(child, lxc.CloneOptions{Backend: backend})
	log.Check(log.FatalLevel, "Cloning container", err)

	fs.SubvolumeClone("lxc/"+parent+"-opt", "lxc/"+child+"-opt")
	fs.SubvolumeClone("lxc-data/"+parent+"-home", "lxc-data/"+child+"-home")
	fs.SubvolumeClone("lxc-data/"+parent+"-var", "lxc-data/"+child+"-var")

	SetContainerConf(child, [][]string{
		{"lxc.network.link", ""},
		{"lxc.network.veth.pair", "x"},
		{"lxc.network.script.up", config.Agent.AppPrefix + "bin/create_ovs_interface"},
		{"subutai.git.branch", child},
		{"subutai.parent", parent},
		{"lxc.mount.entry", config.Agent.LxcPrefix + "lxc/" + child + "-opt opt none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + "lxc-data/" + child + "-home home none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + "lxc-data/" + child + "-var var none bind,rw 0 0"},
	})
}

func ResetNet(name string) {
	SetContainerConf(name, [][]string{
		{"lxc.network.type", "veth"},
		{"lxc.network.flags", "up"},
		{"lxc.network.link", "lxcbr0"},
		{"lxc.network.ipv4.gateway", ""},
		{"lxc.network.veth.pair", ""},
		{"lxc.network.script.up", ""},
		{"lxc.network.mtu", ""},
		{"lxc.network.ipv4", ""},
		{"#vlan_id", ""},
	})
}

func QuotaRAM(name string, size ...string) int {
	c, _ := lxc.NewContainer(name, config.Agent.LxcPrefix)
	i, _ := strconv.Atoi(size[0])
	if i > 0 {
		c.SetMemoryLimit(lxc.ByteSize(i * 1024 * 1024))
		SetContainerConf(name, [][]string{{"lxc.cgroup.memory.limit_in_bytes", size[0] + "M"}})
	}
	limit, _ := c.MemoryLimit()
	return int(limit / 1024 / 1024)
}

func QuotaCPU(name string, size ...string) int {
	c, _ := lxc.NewContainer(name, config.Agent.LxcPrefix)
	cfsPeriod := 10000
	quota, _ := strconv.Atoi(size[0])
	if size[0] != "" && State(name) == "RUNNING" {
		value := strconv.Itoa(cfsPeriod * runtime.NumCPU() * quota / 100)
		c.SetCgroupItem("cpu.cfs_quota_us", value)
		SetContainerConf(name, [][]string{{"lxc.cgroup.cpu.cfs_quota_us", value}})
	}
	quota, _ = strconv.Atoi(c.CgroupItem("cpu.cfs_quota_us")[0])
	return quota * 100 / cfsPeriod / runtime.NumCPU()
}

func QuotaCPUset(name string, size ...string) string {
	c, _ := lxc.NewContainer(name, config.Agent.LxcPrefix)
	if size[0] != "" {
		c.SetCgroupItem("cpuset.cpus", size[0])
		SetContainerConf(name, [][]string{{"lxc.cgroup.cpuset.cpus", size[0]}})
	}
	return c.CgroupItem("cpuset.cpus")[0]
}

func QuotaNet(name string, size ...string) string {
	c, _ := lxc.NewContainer(name, config.Agent.LxcPrefix)
	nic := GetConfigItem(c.ConfigFileName(), "lxc.network.veth.pair")
	return net.RateLimit(nic, size[0])
}
