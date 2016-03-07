package container

import (
	"bufio"
	"github.com/subutai-io/base/agent/agent/utils"
	"github.com/subutai-io/base/agent/config"
	cont "github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/gpg"
	lxc "gopkg.in/lxc/go-lxc.v2"
	"os"
	"strconv"
	"strings"
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
		if err != nil {
			continue
		}

		container.Id = gpg.GetFingerprint(c)
		if details {
			container.Pk = gpg.GetContainerPk(c)
		}
		configpath := config.Agent.LxcPrefix + c + "/config"
		container.Arch = strings.ToUpper(cont.GetConfigItem(configpath, "lxc.arch"))
		container.Name = cont.GetConfigItem(configpath, "lxc.utsname")
		container.Parent = cont.GetConfigItem(configpath, "subutai.parent")
		container.Status = lxc_c.State().String()
		container.Hostname = c
		container.Interfaces = GetContainerIfaces(container.Name)

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

func GetContainerIfaces(name string) []utils.Iface {
	iface := new(utils.Iface)

	c, err := lxc.NewContainer(name, config.Agent.LxcPrefix)
	if err != nil {
		return []utils.Iface{*iface}
	}

	iface.InterfaceName = "eth0"
	listip, _ := c.IPAddress(iface.InterfaceName)
	iface.Ip = strings.Join(listip, " ")

	return []utils.Iface{*iface}
}
