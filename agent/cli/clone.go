package lib

import (
	"io/ioutil"
	"net"
	"os"
	"strings"
	"subutai/config"
	"subutai/lib/container"
	"subutai/lib/gpg"
	"subutai/log"
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

	container.SetContainerUid(child)
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

func setStaticNetwork(name string) {
	data, err := ioutil.ReadFile(config.Agent.LxcPrefix + name + "/rootfs/etc/network/interfaces")
	log.Check(log.WarnLevel, "Opening /etc/network/interfaces", err)

	err = ioutil.WriteFile(config.Agent.LxcPrefix+name+"/rootfs/etc/network/interfaces",
		[]byte(strings.Replace(string(data), "dhcp", "manual", 1)), 0644)
	log.Check(log.WarnLevel, "Setting internal eth0 interface to manual", err)
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
	setStaticNetwork(c)
}
