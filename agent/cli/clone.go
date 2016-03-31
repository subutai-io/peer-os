package lib

import (
	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/gpg"
	"github.com/subutai-io/base/agent/log"
	"io/ioutil"
	"net"
	"os"
	"strings"
)

func LxcClone(parent, child, envId, addr, token string) {
	if !container.IsTemplate(parent) {
		LxcImport(parent, "", token)
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
	container.SetApt(child)
	setDns(child)
	LxcStart(child)

	log.Info(child + " with ID " + gpg.GetFingerprint(child) + " successfully cloned")

}

func setEnvironmentId(name, envId string) {
	err := os.MkdirAll(config.Agent.LxcPrefix+name+"/rootfs/etc/subutai", 755)
	log.Check(log.FatalLevel, "Creating etc/subutai directory", err)

	config, err := os.Create(config.Agent.LxcPrefix + name + "/rootfs/etc/subutai/lxc-config")
	log.Check(log.FatalLevel, "Creating lxc-config file", err)
	defer config.Close()

	_, err = config.WriteString("[Subutai-Agent]\n" + envId + "\n")
	log.Check(log.FatalLevel, "Writing environment id to config", err)

	config.Sync()
}

func setDns(name string) {
	dns := container.GetConfigItem(config.Agent.LxcPrefix+name+"/config", "lxc.network.ipv4.gateway")
	if len(dns) == 0 {
		dns = "10.10.0.254"
	}

	resolv := []byte("domain\tintra.lan\nsearch\tintra.lan\nnameserver\t" + dns + "\n")
	log.Check(log.DebugLevel, "Writing resolv.conf.orig",
		ioutil.WriteFile(config.Agent.LxcPrefix+name+"/rootfs/etc/resolvconf/resolv.conf.d/original", resolv, 0644))
	log.Check(log.DebugLevel, "Writing resolv.conf",
		ioutil.WriteFile(config.Agent.LxcPrefix+name+"/rootfs/etc/resolv.conf", resolv, 0644))
}

func setStaticNetwork(name string) {
	data, err := ioutil.ReadFile(config.Agent.LxcPrefix + name + "/rootfs/etc/network/interfaces")
	log.Check(log.WarnLevel, "Opening /etc/network/interfaces", err)

	err = ioutil.WriteFile(config.Agent.LxcPrefix+name+"/rootfs/etc/network/interfaces",
		[]byte(strings.Replace(string(data), "dhcp", "manual", 1)), 0644)
	log.Check(log.WarnLevel, "Setting internal eth0 interface to manual", err)
}

func addNetConf(name, addr string) {
	ipvlan := strings.Fields(addr)
	_, network, _ := net.ParseCIDR(ipvlan[0])
	gw := []byte(network.IP)
	gw[3]++
	container.SetContainerConf(name, [][]string{
		{"lxc.network.ipv4", ipvlan[0]},
		{"lxc.network.ipv4.gateway", net.IP(gw).String()},
		{"lxc.network.mtu", ""},
		{"#vlan_id", ipvlan[1]},
	})
	setStaticNetwork(name)
}
