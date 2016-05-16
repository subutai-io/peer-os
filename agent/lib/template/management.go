package template

import (
	"crypto/rand"
	"fmt"
	"net"
	"os/exec"
	"strings"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/fs"
	"github.com/subutai-io/base/agent/lib/gpg"
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
	container.SetContainerUid("management")
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
	gpg.GenerateKey("management")
	container.Start("management")
	exec.Command("dhclient", "mng-net").Run()

	nic, err := net.InterfaceByName("eth1")
	if err != nil {
		nic, err = net.InterfaceByName("wan")
		log.Check(log.ErrorLevel, "Getting interface info", err)
	}
	mngIp, err := nic.Addrs()
	log.Check(log.ErrorLevel, "Getting interface addresses", err)
	if len(mngIp) > 0 {
		ip := strings.Split(mngIp[0].String(), "/")
		if len(ip) > 0 {
			log.Info("******************************")
			log.Info("Subutai Management UI will shortly be available at https://" + ip[0] + ":8443 (admin/secret)")
			log.Info("SSH access to Management: ssh root@" + ip[0] + " -p2222 (ubuntu)")
			log.Info("Don't forget to change default passwords")
			log.Info("******************************")
		}
	}
}

func MngStop() {
	for _, port := range []string{"5005", "8443", "8444"} {
		exec.Command("iptables", "-t", "nat", "-D", "PREROUTING", "-i", "wan", "-p",
			"tcp", "--dport", port, "-j", "DNAT", "--to-destination", "10.10.10.1:"+port).Run()
	}
	exec.Command("iptables", "-t", "nat", "-D", "PREROUTING", "-i", "wan", "-p",
		"tcp", "--dport", "2222", "-j", "DNAT", "--to-destination", "10.10.10.1:22").Run()
}

func MngDel() {
	exec.Command("ovs-vsctl", "del-port", "wan", "management").Run()
	exec.Command("ovs-vsctl", "del-port", "wan", "mng-gw").Run()
	exec.Command("dhclient", "-r", "mng-net").Run()
}
