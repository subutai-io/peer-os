package template

import (
	"crypto/rand"
	"fmt"
	"github.com/subutai-io/Subutai/agent/config"
	"github.com/subutai-io/Subutai/agent/lib/container"
	"github.com/subutai-io/Subutai/agent/lib/fs"
	"github.com/subutai-io/Subutai/agent/log"
	"net"
	"os"
	"os/exec"
	"strings"
)

func mac() string {
	buf := make([]byte, 6)
	_, err := rand.Read(buf)
	log.Check(log.ErrorLevel, "Generating random mac", err)
	return fmt.Sprintf("00:16:3e:%02x:%02x:%02x", buf[3], buf[4], buf[5])
}

func MngInit() {
	fs.ReadOnly("management", false)
	container.SetContainerUid("management")
	container.SetContainerConf("management", [][]string{
		{"lxc.network.hwaddr", mac()},
		{"lxc.network.veth.pair", "management"},
		{"lxc.network.script.up", config.Agent.AppPrefix + "bin/create_ovs_interface"},
		{"lxc.network.link", ""},
		// TODO following lines kept for back compatibility with old templates, should be deleted when all templates will be replaced.
		{"lxc.mount.entry", config.Agent.LxcPrefix + "management/home home none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + "management/opt opt none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + "management/var var none bind,rw 0 0"},
	})

	container.Start("management")
	exec.Command("dhclient", "mng-net").Run()

	wan, err := net.InterfaceByName("wan")
	log.Check(log.ErrorLevel, "Getting WAN interface info", err)
	wanIP, err := wan.Addrs()
	log.Check(log.ErrorLevel, "Getting WAN interface adresses", err)
	if len(wanIP) > 0 {
		ip := strings.Split(wanIP[0].String(), "/")
		if len(ip) > 0 {
			log.Info("******************************")
			log.Info("Subutai Management UI will shortly be available at https://" + ip[0] + ":8443 (admin/secret)")
			log.Info("SSH access to Management: ssh root@" + ip[0] + " -p2222 (ubuntu)")
			log.Info("Don't forget to change default passwords")
			log.Info("******************************")
		}
	}

	os.Exit(0)
}

func MngStop() {
	exec.Command("iptables", "-t", "nat", "--flush", "PREROUTING").Run()
}

func MngDel() {
	exec.Command("iptables", "-t", "nat", "--flush", "PREROUTING").Run()
	exec.Command("ovs-vsctl", "del-port", "wan", "management").Run()
	exec.Command("ovs-vsctl", "del-port", "wan", "mng-gw").Run()
	exec.Command("dhclient", "-r", "mng-net").Run()
}
