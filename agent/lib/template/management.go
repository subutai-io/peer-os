package template

import (
	"crypto/rand"
	"fmt"
	"os/exec"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/fs"
	"github.com/subutai-io/base/agent/lib/gpg"
	"github.com/subutai-io/base/agent/lib/net"
	"github.com/subutai-io/base/agent/log"
)

func Mac() string {
	buf := make([]byte, 6)
	_, err := rand.Read(buf)
	log.Check(log.ErrorLevel, "Generating random mac", err)
	return fmt.Sprintf("00:16:3e:%02x:%02x:%02x", buf[3], buf[4], buf[5])
}

func MngInit() {
	fs.ReadOnly("management", false)
	container.SetContainerUID("management")
	container.SetContainerConf("management", [][]string{
		{"lxc.network.hwaddr", Mac()},
		{"lxc.network.veth.pair", "management"},
		{"lxc.network.script.up", config.Agent.AppPrefix + "bin/create_ovs_interface"},
		{"lxc.network.link", ""},
		// TODO following lines kept for back compatibility with old templates, should be deleted when all templates will be replaced.
		{"lxc.mount.entry", config.Agent.LxcPrefix + "management/home home none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + "management/opt opt none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + "management/var var none bind,rw 0 0"},
	})
	container.SetApt("management")
	container.SetContainerUID("management")
	gpg.GenerateKey("management")
	container.Start("management")

	log.Info("********************")
	log.Info("Subutai Management UI will be shortly available at https://" + net.GetIp() + ":8443")
	log.Info("login: admin")
	log.Info("password: secret")
	log.Info("********************")
}

func MngStop() {
	for _, iface := range []string{"wan", "eth1", "eth2"} {
		for _, port := range []string{"8443", "8444"} {
			exec.Command("iptables", "-t", "nat", "-D", "PREROUTING", "-i", iface, "-p",
				"tcp", "--dport", port, "-j", "DNAT", "--to-destination", "10.10.10.1:"+port).Run()
		}
	}
}

func MngDel() {
	exec.Command("ovs-vsctl", "del-port", "wan", "management").Run()
	exec.Command("ovs-vsctl", "del-port", "wan", "mng-gw").Run()
	exec.Command("dhclient", "-r", "mng-net").Run()
}
