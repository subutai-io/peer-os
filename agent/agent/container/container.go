package container

import (
	"bufio"
	lxc "gopkg.in/lxc/go-lxc.v2"
	"os"
	"strconv"
	"strings"
	"subutai/agent/utils"
	"subutai/config"
	cont "subutai/lib/container"
	"subutai/log"
)

const (
	utsname = "lxc.utsname"
)

type Container struct {
	Hostname   string        `json:"hostname"`
	Id         string        `json:"id"`
	Arch       string        `json:"arch"`
	Interfaces []utils.Iface `json:"interfaces"`
	Status     string        `json:"status,omitempty"`
	Parent     string        `json:"templateName,omitempty"`
	Vlan       int           `json:"vlan,omitempty"`
	Pk         string        `json:"publicKey,omitempty"`
	Name       string        `json:"name"`
}

func GetCredentials(name, container string) (uid int, gid int) {
	path := config.Agent.LxcPrefix + container + "/rootfs/etc/passwd"
	u, g := parsePasswd(path, name)
	uid, _ = strconv.Atoi(u)
	gid, _ = strconv.Atoi(g)
	return uid, gid
}

func parsePasswd(path, name string) (uid string, gid string) {
	file, _ := os.Open(path)
	defer file.Close()
	scanner := bufio.NewScanner(file)
	scanner.Split(bufio.ScanLines)

	for scanner.Scan() {
		if strings.Contains(scanner.Text(), name) {
			arr := strings.Split(scanner.Text(), ":")
			if len(arr) > 3 {
				return arr[2], arr[3]
			}
		}
	}
	return "", ""
}

func GetActiveContainers(details bool) []Container {
	aCont := cont.Containers()
	contArr := []Container{}

	for _, c := range aCont {
		container := new(Container)
		lxc_c, err := lxc.NewContainer(c, config.Agent.LxcPrefix)
		if log.Check(log.WarnLevel, "Getting container "+c, err) {
			continue
		}

		container.Id = utils.GetFingerprint(c)
		if details {
			container.Pk = utils.GetContainerPk(c)
		}
		configpath := config.Agent.LxcPrefix + c + "/config"
		container.Arch = strings.ToUpper(cont.GetConfigItem(configpath, "lxc.arch"))
		container.Name = cont.GetConfigItem(configpath, "lxc.utsname")
		container.Parent = cont.GetConfigItem(configpath, "subutai.parent")
		container.Status = lxc_c.State().String()
		container.Hostname = c
		container.Interfaces = GetContainerIfaces(lxc_c)

		PoolInstance().AddHost(container.Id, container.Name)
		vlan_id := cont.GetConfigItem(configpath, "#vlan_id")
		if len(vlan_id) > 0 {
			container.Vlan, _ = strconv.Atoi(vlan_id)
		} else {
			container.Vlan = 0
		}
		contArr = append(contArr, *container)

		lxc.Release(lxc_c)
	}
	return contArr
}

func GetContainerIfaces(c *lxc.Container) []utils.Iface {
	buf_r, buf_w, _ := os.Pipe()
	var output []string
	c.RunCommand([]string{"ifconfig"}, lxc.AttachOptions{
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

	interface_arr := []utils.Iface{}
	iface := new(utils.Iface)
	iface.InterfaceName = "eth0"

	for _, line := range output {
		if strings.HasPrefix(line, "eth0") {
			iface.Mac = strings.Fields(line)[4]
		}
		if iface.Mac != "" && strings.Contains(line, "inet addr") {
			if strings.Split(strings.Fields(line)[1], ":")[1] != "127.0.0.1" {
				iface.Ip = strings.Split(strings.Fields(line)[1], ":")[1]
			}
			break
		}
	}

	interface_arr = append(interface_arr, *iface)
	return interface_arr
}
