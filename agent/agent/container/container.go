package container

import (
	"bufio"
	"io/ioutil"
	"os"
	"strconv"
	"strings"
	"subutai/log"

	"github.com/subutai-io/base/agent/agent/utils"
	"github.com/subutai-io/base/agent/config"
	cont "github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/gpg"
	lxc "gopkg.in/lxc/go-lxc.v2"
)

// Container describes Subutai container with all required options for the Management server.
type Container struct {
	ID         string        `json:"id"`
	Name       string        `json:"name"`
	Hostname   string        `json:"hostname"`
	Status     string        `json:"status,omitempty"`
	Arch       string        `json:"arch"`
	Interfaces []utils.Iface `json:"interfaces"`
	Parent     string        `json:"templateName,omitempty"`
	Vlan       int           `json:"vlan,omitempty"`
	Pk         string        `json:"publicKey,omitempty"`
}

// Credentials returns information about IDs from container. This informations is user for command execution only.
func Credentials(name, container string) (uid int, gid int) {
	path := config.Agent.LxcPrefix + container + "/rootfs/etc/passwd"
	u, g := parsePasswd(path, name)
	uid, err := strconv.Atoi(u)
	log.Check(log.DebugLevel, "Parsing user UID from container", err)
	gid, err = strconv.Atoi(g)
	log.Check(log.DebugLevel, "Parsing user GID from container", err)
	return uid, gid
}

func parsePasswd(path, name string) (uid string, gid string) {
	file, err := os.Open(path)
	if err != nil {
		return "", ""
	}
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

// Active provides list of active Subutai containers.
func Active(details bool) []Container {
	contArr := []Container{}

	for _, c := range cont.Containers() {
		hostname, err := ioutil.ReadFile(config.Agent.LxcPrefix + c + "/rootfs/etc/hostname")
		if err != nil {
			continue
		}
		configpath := config.Agent.LxcPrefix + c + "/config"

		container := Container{
			ID:         gpg.GetFingerprint(c),
			Name:       c,
			Hostname:   strings.TrimSpace(string(hostname)),
			Status:     cont.State(c),
			Arch:       strings.ToUpper(cont.GetConfigItem(configpath, "lxc.arch")),
			Interfaces: interfaces(c),
			Parent:     cont.GetConfigItem(configpath, "subutai.parent"),
		}
		if details {
			container.Pk = gpg.GetContainerPk(c)
		}

		contArr = append(contArr, container)
	}
	return contArr
}

func interfaces(name string) []utils.Iface {
	iface := new(utils.Iface)

	c, err := lxc.NewContainer(name, config.Agent.LxcPrefix)
	if err != nil {
		return []utils.Iface{*iface}
	}

	iface.InterfaceName = "eth0"
	listip, err := c.IPAddress(iface.InterfaceName)
	if err == nil {
		iface.IP = strings.Join(listip, " ")
	}

	return []utils.Iface{*iface}
}
