package container

import (
	"bufio"
	"bytes"
	"errors"
	"io/ioutil"
	"os"
	"os/exec"
	"runtime"
	"strconv"
	"strings"
	"syscall"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/fs"
	"github.com/subutai-io/base/agent/lib/net"
	"github.com/subutai-io/base/agent/log"

	"gopkg.in/lxc/go-lxc.v2"
)

// All returns list of all containers
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
	c, err := lxc.NewContainer(name, config.Agent.LxcPrefix)
	if err != nil {
		return "UNKNOWN"
	}
	switch c.State() {
	case lxc.STOPPED:
		return "STOPPED"
	case lxc.RUNNING:
		return "RUNNING"
	case lxc.STARTING:
		return "STARTING"
	case lxc.STOPPING:
		return "STOPPING"
	case lxc.ABORTING:
		return "ABORTING"
	case lxc.FREEZING:
		return "FREEZING"
	case lxc.FROZEN:
		return "FROZEN"
	case lxc.THAWED:
		return "THAWED"
	}
	return "UNKNOWN"
}

func SetApt(name string) {
	gateway := GetConfigItem(config.Agent.LxcPrefix+name+"/config", "lxc.network.ipv4.gateway")
	if len(gateway) == 0 {
		gateway = "10.10.0.254"
	}

	repo := []byte("deb http://" + gateway + "/apt/main trusty main restricted universe multiverse\n" +
		"deb http://" + gateway + "/apt/main trusty-updates main restricted universe multiverse\n" +
		"deb http://" + gateway + "/apt/security trusty-security main restricted universe multiverse\n")
	log.Check(log.DebugLevel, "Writing apt source repo list",
		ioutil.WriteFile(config.Agent.LxcPrefix+name+"/rootfs/etc/apt/sources.list", repo, 0644))

	// kurjun := []byte("deb [arch=amd64,all] http://" + config.Management.Host + ":8330/rest/kurjun/vapt trusty main contrib\n" +
	// 	"deb [arch=amd64,all] http://" + config.Cdn.Url + ":8330/kurjun/rest/deb trusty main contrib\n")
	kurjun := []byte("deb http://" + config.Cdn.Url + ":8080/kurjun/rest/apt /\n")
	log.Check(log.DebugLevel, "Writing apt source kurjun list",
		ioutil.WriteFile(config.Agent.LxcPrefix+name+"/rootfs/etc/apt/sources.list.d/subutai-repo.list", kurjun, 0644))
}

func AptUpdate(name string) {
	c, err := lxc.NewContainer(name, config.Agent.LxcPrefix)
	log.Check(log.FatalLevel, "Looking for container "+name, err)
	c.RunCommand([]string{"bash", "-c", "sleep 5 && apt update -o Acquire::http::Timeout=5 -o Dir::Etc::sourcelist=\"/etc/apt/sources.list.d/subutai-repo.list\" >/dev/null 2>&1 &"}, lxc.DefaultAttachOptions)
}

func Start(name string) {
	c, err := lxc.NewContainer(name, config.Agent.LxcPrefix)
	log.Check(log.FatalLevel, "Looking for container "+name, err)

	c.Start()

	if _, err := os.Stat(config.Agent.LxcPrefix + name + "/.stop"); err == nil {
		log.Check(log.WarnLevel, "Deleting .stop file to "+name, os.Remove(config.Agent.LxcPrefix+name+"/.stop"))
	}
	if _, err := os.Stat(config.Agent.LxcPrefix + name + "/.start"); os.IsNotExist(err) {
		f, err := os.Create(config.Agent.LxcPrefix + name + "/.start")
		log.Check(log.WarnLevel, "Creating .start file to "+name, err)
		defer f.Close()
	}
}

func Stop(name string) {
	c, err := lxc.NewContainer(name, config.Agent.LxcPrefix)
	log.Check(log.FatalLevel, "Looking for container "+name, err)

	c.Stop()

	if _, err := os.Stat(config.Agent.LxcPrefix + name + "/.start"); err == nil {
		log.Check(log.WarnLevel, "Creating .start file to "+name, os.Remove(config.Agent.LxcPrefix+name+"/.start"))
	}
	if _, err := os.Stat(config.Agent.LxcPrefix + name + "/.stop"); os.IsNotExist(err) {
		f, err := os.Create(config.Agent.LxcPrefix + name + "/.stop")
		log.Check(log.WarnLevel, "Creating .stop file to "+name, err)
		defer f.Close()
	}
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
	if !log.Check(log.WarnLevel, "Creating container object", err) && c.State() == lxc.RUNNING {
		log.Check(log.FatalLevel, "Stopping container", c.Stop())
	}
	fs.SubvolumeDestroy(config.Agent.LxcPrefix + name)

	log.Info(name + " destroyed")
}

func GetParent(name string) string {
	if !IsContainer(name) {
		return "Container does not exists"
	}
	configFileName := config.Agent.LxcPrefix + name + "/config"
	return GetConfigItem(configFileName, "subutai.parent")
}

func Clone(parent, child string) {
	var backend lxc.BackendStore
	backend.Set("btrfs")

	c, err := lxc.NewContainer(parent, config.Agent.LxcPrefix)
	log.Check(log.FatalLevel, "Looking for container "+parent, err)

	fs.SubvolumeCreate(config.Agent.LxcPrefix + child)
	err = c.Clone(child, lxc.CloneOptions{Backend: backend})
	log.Check(log.FatalLevel, "Cloning container", err)

	fs.SubvolumeClone(config.Agent.LxcPrefix+parent+"/home", config.Agent.LxcPrefix+child+"/home")
	fs.SubvolumeClone(config.Agent.LxcPrefix+parent+"/opt", config.Agent.LxcPrefix+child+"/opt")
	fs.SubvolumeClone(config.Agent.LxcPrefix+parent+"/var", config.Agent.LxcPrefix+child+"/var")

	SetContainerConf(child, [][]string{
		{"lxc.network.link", ""},
		{"lxc.network.veth.pair", strings.Replace(GetConfigItem(config.Agent.LxcPrefix+child+"/config", "lxc.network.hwaddr"), ":", "", -1)},
		{"lxc.network.script.up", config.Agent.AppPrefix + "bin/create_ovs_interface"},
		{"subutai.parent", parent},
		{"lxc.mount.entry", config.Agent.LxcPrefix + child + "/home home none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + child + "/opt opt none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + child + "/var var none bind,rw 0 0"},
		{"lxc.network.mtu", ""},
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

// QuotaCPU sets container CPU limitation and return current value in percents.
// If passed value < 100, we assume that this value mean percents.
// If passed value > 100, we assume that this value mean MHz.
func QuotaCPU(name string, size ...string) int {
	c, _ := lxc.NewContainer(name, config.Agent.LxcPrefix)
	cfsPeriod := 100000
	tmp, _ := strconv.Atoi(size[0])
	quota := float32(tmp)

	if quota > 100 {
		out, err := ioutil.ReadFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
		freq, _ := strconv.Atoi(strings.TrimSpace(string(out)))
		freq = freq / 1000
		if log.Check(log.DebugLevel, "Getting CPU max frequency", err) {
			out, _ := ioutil.ReadFile("/proc/cpuinfo")
			scanner := bufio.NewScanner(bytes.NewReader(out))
			for scanner.Scan() {
				if strings.HasPrefix(scanner.Text(), "cpu MHz") {
					freq, _ = strconv.Atoi(strings.TrimSpace(strings.Split(strings.Split(scanner.Text(), ":")[1], ".")[0]))
					break
				}
			}
		}
		quota = quota * 100 / float32(freq) / float32(runtime.NumCPU())
	}

	if size[0] != "" && State(name) == "RUNNING" {
		value := strconv.Itoa(int(float32(cfsPeriod) * float32(runtime.NumCPU()) * quota / 100))
		c.SetCgroupItem("cpu.cfs_quota_us", value)
		SetContainerConf(name, [][]string{{"lxc.cgroup.cpu.cfs_quota_us", value}})
	}

	result, _ := strconv.Atoi(c.CgroupItem("cpu.cfs_quota_us")[0])
	return result * 100 / cfsPeriod / runtime.NumCPU()
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

func SetContainerConf(container string, conf [][]string) {
	confPath := config.Agent.LxcPrefix + container + "/config"
	newconf := ""

	file, err := os.Open(confPath)
	log.Check(log.FatalLevel, "Opening container config "+confPath, err)
	scanner := bufio.NewScanner(bufio.NewReader(file))

	for scanner.Scan() {
		newline := scanner.Text() + "\n"
		for i := 0; i < len(conf); i++ {
			line := strings.Split(scanner.Text(), "=")
			if len(line) > 1 && strings.Trim(line[0], " ") == conf[i][0] {
				if newline = ""; len(conf[i][1]) > 0 {
					newline = conf[i][0] + " = " + conf[i][1] + "\n"
				}
				conf = append(conf[:i], conf[i+1:]...)
				break
			}
		}
		newconf = newconf + newline
	}
	file.Close()

	for i := range conf {
		if conf[i][1] != "" {
			newconf = newconf + conf[i][0] + " = " + conf[i][1] + "\n"
		}
	}

	log.Check(log.FatalLevel, "Writing container config "+confPath, ioutil.WriteFile(confPath, []byte(newconf), 0644))
}

func GetConfigItem(path, item string) string {
	config, _ := os.Open(path)
	defer config.Close()
	scanner := bufio.NewScanner(config)
	for scanner.Scan() {
		line := strings.Split(scanner.Text(), "=")
		if strings.Trim(line[0], " ") == item {
			return strings.Trim(line[1], " ")
		}
	}
	return ""
}

func SetContainerUid(c string) {
	var uidlast []byte

	uidlast, _ = ioutil.ReadFile(config.Agent.LxcPrefix + "uidmaplast")

	uid, _ := strconv.Atoi(string(uidlast))
	newuid := strconv.Itoa(uid + 65536)

	err := ioutil.WriteFile(config.Agent.LxcPrefix+"uidmaplast", []byte(newuid), 0644)
	log.Check(log.FatalLevel, "Writing new uid to map", err)

	SetContainerConf(c, [][]string{
		{"lxc.include", config.Agent.AppPrefix + "share/lxc/config/ubuntu.common.conf"},
		{"lxc.include", config.Agent.AppPrefix + "share/lxc/config/ubuntu.userns.conf"},
		{"lxc.id_map", "u 0 " + newuid + " 65536"},
		{"lxc.id_map", "g 0 " + newuid + " 65536"},
	})

	s, _ := os.Stat(config.Agent.LxcPrefix + c + "/rootfs")
	parentuid := strconv.Itoa(int(s.Sys().(*syscall.Stat_t).Uid))

	exec.Command("uidmapshift", "-b", config.Agent.LxcPrefix+c+"/rootfs/", parentuid, newuid, "65536").Run()
	exec.Command("uidmapshift", "-b", config.Agent.LxcPrefix+c+"/home/", parentuid, newuid, "65536").Run()
	exec.Command("uidmapshift", "-b", config.Agent.LxcPrefix+c+"/opt/", parentuid, newuid, "65536").Run()
	exec.Command("uidmapshift", "-b", config.Agent.LxcPrefix+c+"/var/", parentuid, newuid, "65536").Run()

	log.Check(log.ErrorLevel, "Setting chmod 755 on lxc home", os.Chmod(config.Agent.LxcPrefix+c, 0755))
}
