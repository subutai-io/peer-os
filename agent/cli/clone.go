package lib

import (
	"io/ioutil"
	"net"
	"os"
	"os/exec"
	"strconv"
	"strings"
	"subutai/config"
	"subutai/lib/container"
	"subutai/lib/gpg"
	"subutai/log"
	"syscall"
)

func LxcClone(parent, child, envId, addr, token string) {
	if !container.IsTemplate(parent) {
		LxcImport(parent)
	}
	if container.IsContainer(child) {
		log.Error("Container " + child + " already exist")
	}

	container.Clone(parent, child)
	gpg.GenerateKey(child)

	if len(token) != 0 {
		gpg.ExchageAndEncrypt(child, token)
	}

	if len(envId) != 0 {
		setEnvironmentId(child, envId)
	}

	if len(addr) != 0 {
		addNetConf(child, addr)
	}

	setContainerUid(child)
	LxcStart(child)

	container.AptUpdate(child)
	// container.Start(child)
	// log.Info(child + " successfully cloned")
}

func setEnvironmentId(container, envId string) {
	err := os.MkdirAll(config.Agent.LxcPrefix+container+"/rootfs/etc/subutai", 755)
	log.Check(log.FatalLevel, "Creating etc/subutai directory", err)

	config, err := os.Create(config.Agent.LxcPrefix + container + "/rootfs/etc/subutai/lxc-config")
	log.Check(log.FatalLevel, "Creating lxc-config file", err)
	defer config.Close()

	_, err = config.WriteString("[Subutai-Agent]\n" + envId + "\n")
	log.Check(log.FatalLevel, "Writing environment id to config", err)

	config.Sync()
}

func addNetConf(c, addr string) {
	ipvlan := strings.Fields(addr)
	_, network, _ := net.ParseCIDR(ipvlan[0])
	gw := []byte(network.IP)
	gw[3]++
	container.SetContainerConf(c, [][]string{
		{"lxc.network.ipv4", ipvlan[0]},
		{"lxc.network.ipv4.gateway", net.IP(gw).String()},
		{"lxc.network.mtu", "1340"},
		{"#vlan_id", ipvlan[1]},
	})
}

func setContainerUid(c string) {
	var uidlast []byte

	uidlast, _ = ioutil.ReadFile(config.Agent.LxcPrefix + "uidmaplast")

	uid, _ := strconv.Atoi(string(uidlast))
	newuid := strconv.Itoa(uid + 65536)

	err := ioutil.WriteFile(config.Agent.LxcPrefix+"uidmaplast", []byte(newuid), 0644)
	log.Check(log.FatalLevel, "Writing new uid to map", err)

	container.SetContainerConf(c, [][]string{
		{"lxc.include", config.Agent.AppPrefix + "share/lxc/config/ubuntu.common.conf"},
		{"lxc.include", config.Agent.AppPrefix + "share/lxc/config/ubuntu.userns.conf"},
		{"lxc.id_map", "u 0 " + newuid + " 65536"},
		{"lxc.id_map", "g 0 " + newuid + " 65536"},
	})

	s, _ := os.Stat(config.Agent.LxcPrefix + c + "/rootfs")
	parentuid := strconv.Itoa(int(s.Sys().(*syscall.Stat_t).Uid))

	exec.Command("uidmapshift", "-b", config.Agent.LxcPrefix+c+"/rootfs/", parentuid, newuid, "65536").Run()
	exec.Command("uidmapshift", "-b", config.Agent.LxcPrefix+"lxc/"+c+"-opt/", parentuid, newuid, "65536").Run()
	exec.Command("uidmapshift", "-b", config.Agent.LxcPrefix+"lxc-data/"+c+"-home/", parentuid, newuid, "65536").Run()
	exec.Command("uidmapshift", "-b", config.Agent.LxcPrefix+"lxc-data/"+c+"-var/", parentuid, newuid, "65536").Run()

	err = os.Chmod(config.Agent.LxcPrefix+c, 0755)
	if err != nil {
		log.Error("Chmod for " + c + " failed: " + err.Error())
	}
}
