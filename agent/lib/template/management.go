package template

import (
	"crypto/rand"
	"fmt"
	"io/ioutil"
	"os"
	"os/exec"
	"subutai/lib/container"
	"subutai/lib/fs"
	"subutai/log"
)

func mac() string {
	buf := make([]byte, 6)
	_, err := rand.Read(buf)
	log.Check(log.ErrorLevel, "Generating random mac", err)
	// return fmt.Sprintf("%02x:%02x:%02x:%02x:%02x:%02x", buf[0], buf[1], buf[2], buf[3], buf[4], buf[5])
	return fmt.Sprintf("00:16:3e:%02x:%02x:%02x", buf[3], buf[4], buf[5])
}

func MngInit() {
	fs.ReadOnly("management", false)
	container.SetContainerUid("management")
	container.SetContainerConf("management", [][]string{
		{"lxc.network.hwaddr", mac()},
		{"lxc.network.hwaddr", mac()},
	})

	exec.Command("ovs-vsctl", "--may-exist", "add-br", "br-mng").Run()
	exec.Command("ovs-vsctl", "del-port", "br-int", "eth0").Run()
	exec.Command("ovs-vsctl", "--may-exist", "add-port", "br-mng", "eth0").Run()
	exec.Command("dhclient", "-r", "br-int").Run()
	exec.Command("dhclient", "br-mng").Run()

	exec.Command("ovs-vsctl", "--may-exist", "add-port", "br-int", "eth1").Run()
	exec.Command("ovs-vsctl", "--may-exist", "add-br", "br-tun").Run()
	exec.Command("ovs-vsctl", "--may-exist", "add-port", "br-tun", "tunTOint").Run()
	exec.Command("ovs-vsctl", "--may-exist", "add-port", "br-int", "intTOtun").Run()
	exec.Command("ovs-vsctl", "set", "interface", "tunTOint", "type=patch", "options:peer=intTOtun").Run()
	exec.Command("ovs-vsctl", "set", "interface", "intTOtun", "type=patch", "options:peer=tunTOint").Run()
	exec.Command("ovs-vsctl", "set", "bridge", "br-tun", "stp_enable=true").Run()
	exec.Command("ovs-ofctl", "add-flow", "br-tun", "\"priority=2500,dl_vlan=0xffff actions=drop\"").Run()

	err := ioutil.WriteFile("/writable/system-data/etc/network/interfaces.d/eth0",
		[]byte("allow-hotplug eth0\niface eth0 inet manual\n"), 0644)
	log.Check(log.WarnLevel, "Writing eth0 config", err)

	err = ioutil.WriteFile("/writable/system-data/etc/network/interfaces.d/eth1",
		[]byte("allow-hotplug eth1\niface eth1 inet manual\n"), 0644)
	log.Check(log.WarnLevel, "Writing eth1 config", err)

	container.Start("management")
	exec.Command("dhclient", "br-int").Run()

	f, err := os.OpenFile("/etc/hosts", os.O_APPEND|os.O_WRONLY, 0600)
	log.Check(log.WarnLevel, "Opening /etc/hosts file", err)
	defer f.Close()
	_, err = f.WriteString("10.10.10.1	management gw.intra.lan")
	log.Check(log.WarnLevel, "Adding gw.intra.lan to hosts", err)

	os.Exit(0)
}

func MngDel() {
	exec.Command("ovs-vsctl", "del-br", "br-mng").Run()
	exec.Command("ovs-vsctl", "del-port", "br-int", "eth1").Run()
	exec.Command("ovs-vsctl", "del-port", "br-int", "mng-lan").Run()
	exec.Command("ovs-vsctl", "add-port", "br-int", "eth0").Run()
	exec.Command("dhclient", "-r", "br-int").Run()
	exec.Command("dhclient", "br-int").Start()
}
