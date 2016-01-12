package net

import (
	"fmt"
	"io"
	"io/ioutil"
	"net"
	"os"
	"os/exec"
	"strings"
	"subutai/cli/lib"
	"subutai/config"
	"subutai/log"
)

func CalculateGW(ipS string) string {
	ip, ipnet, err := net.ParseCIDR(ipS)
	log.Check(log.FatalLevel, "Parsing IP", err)
	ip = ip.To4()
	ip = ip.Mask(ipnet.Mask)
	ip[3]++
	return ip.String()
}

func prepareStrForServiceInterface(vlanip, vlanid string) string {
	str := "    ifconfig br-" + vlanid + " " + vlanip + "\n" +
		"    ovs-ofctl add-flow br-" + vlanid + " priority=2500,ip,nw_src=10.10.10.0/24 actions=drop\n" +
		"    ovs-ofctl add-flow br-int priority=2500,ip,nw_src=10.10.10.0/24,nw_dst=" + vlanip + " actions=drop\n" +
		"    ovs-ofctl add-flow br-" + vlanid + " priority=2600,ip,nw_src=10.10.10.1 actions=normal\n" +
		"    ovs-ofctl add-flow br-int priority=2600,ip,nw_src=10.10.10.1,nw_dst=" + vlanip + " actions=normal\n"

	return str
}

func writeinServiceInterface(vlanip, vlanid string) {
	str := prepareStrForServiceInterface(vlanid, vlanip)
	if f, err := ioutil.ReadFile(config.Agent.DataPrefix + "/var/subutai-network/service-interface"); err != nil {
		lib.CopyFile(config.Agent.AppPrefix+"/var/service-interface", config.Agent.DataPrefix+"/var/subutai-network/service-interface")
		// log.Error("read file:" + config.Agent.DataPrefix + "/var/subutai-network/service-interface")
	} else {
		lines := strings.Split(string(f), "\n")
		for k, v := range lines {
			if strings.Contains(string(v), "#Re-init OVS Flows") {
				lines[k] = str + "#Re-init OVS Flows\n"
			}
		}
		str := strings.Join(lines, "\n")
		log.Check(log.FatalLevel, "write "+config.Agent.DataPrefix+"/var/subutai-network/service-interface",
			ioutil.WriteFile(config.Agent.DataPrefix+"/var/subutai-network/service-interface", []byte(str), 0744))
	}
}
func CreateGateway(vlanip, vlanid string) {
	// check: control ip. no need to get returns.
	// controlIp(vlanip)
	// check: add to ovs-vsctl --db=unix:$SUBUTAI_DATA_PREFIX/ovs/db.sock add-br br-$vlanID + ifconfig  br-$vlanID $ipAdd
	log.Check(log.FatalLevel, "ovs-vsctl add-br ", exec.Command("ovs-vsctl", "add-br", "br-"+vlanid).Run())
	log.Check(log.FatalLevel, "ifconfig ", exec.Command("ifconfig", "br-"+vlanid, vlanip).Run())
	log.Check(log.FatalLevel, "add-port br-vlan",
		exec.Command("ovs-vsctl", "add-port", "br-"+vlanid, vlanid+"toint").Run())
	log.Check(log.FatalLevel, "set interface 1",
		exec.Command("ovs-vsctl", "set", "interface", vlanid+"toint", "type=patch").Run())
	log.Check(log.FatalLevel, "set interface 2",
		exec.Command("ovs-vsctl", "set", "interface", vlanid+"toint", "options:peer=intto"+vlanid).Run())
	log.Check(log.FatalLevel, "add-port br-int",
		exec.Command("ovs-vsctl", "add-port", "br-int", "intto"+vlanid).Run())
	log.Check(log.FatalLevel, "set interface 3",
		exec.Command("ovs-vsctl", "set", "interface", "intto"+vlanid, "type=patch").Run())
	log.Check(log.FatalLevel, "set interface 4",
		exec.Command("ovs-vsctl", "set", "interface", "intto"+vlanid, "options:peer="+vlanid+"toint").Run())
	log.Check(log.FatalLevel, "set port",
		exec.Command("ovs-vsctl", "set", "port", "br-"+vlanid, "tag="+vlanid).Run())
	// check: "ovs-ofctl"

	log.Check(log.FatalLevel, "ovsflow 1",
		exec.Command("ovs-ofctl", "add-flow", "br-"+vlanid, "priority=2500,ip,nw_src=10.10.10.0/24 actions=drop").Run())
	log.Check(log.FatalLevel, "ovsFlow 2",
		exec.Command("ovs-ofctl", "add-flow", "br-int", "priority=2500,ip,nw_src=10.10.10.0/24,nw_dst="+vlanip+" actions=drop").Run())
	log.Check(log.FatalLevel, "ovsFlow 3",
		exec.Command("ovs-ofctl", "add-flow", "br-"+vlanid, "priority=2600,ip,nw_src=10.10.10.1 actions=normal").Run())
	log.Check(log.FatalLevel, "ovsFlow 4",
		exec.Command("ovs-ofctl", "add-flow", "br-int", "priority=2600,ip,nw_src=10.10.10.1,nw_dst="+vlanip+" actions=normal").Run())

	// check: write them service-interface file
	writeinServiceInterface(vlanid, vlanip)
	// check: br-int + eth and only ip.
	// since we are not using openflow aynmore we do not need net_bridge_blocker!
}

func DeleteGateway(vlanid string) {

	log.Check(log.FatalLevel, "ovs-vsctl del-br",
		exec.Command("ovs-vsctl", "del-br", "br-"+vlanid).Run())
	log.Check(log.FatalLevel, "ovs-vsctl del-port",
		exec.Command("ovs-vsctl", "del-port", "br-int", "intto"+vlanid).Run())
}

func ListTapDevice() {
	if ps, err := exec.Command("ps", "ax").CombinedOutput(); err != nil {
		log.Error("ps exec error", err.Error())
	} else {
		fmt.Printf("%0s %20s\n", "TapDeviceInterface", "TapDeviceIPAddress")
		for _, v := range strings.Split(string(ps), "\n") {
			if strings.Contains(v, "tap_create -d") {
				fmt.Printf("%0s %20s\n", v) // leave like this for now..
			}
		}
	}
}

func RemoveDefaultGW(contName string) {
	filePath := config.Agent.LxcPrefix + contName + "/rootfs/etc/"
	for _, file := range []string{"hosts", "resolv.conf"} {

		_, err := os.Stat(filePath + file)
		log.Check(log.PanicLevel, "Checking "+file+" file", err)

		openFile, err := os.Open(filePath + file)
		log.Check(log.PanicLevel, "Opening "+file+" file", err)

		fileBck, err := os.Create(filePath + file + ".BACKUP")
		log.Check(log.PanicLevel, "Creating "+file+" backup file", err)

		_, err = io.Copy(fileBck, openFile)
		log.Check(log.FatalLevel, "Copying "+file+" backup", err)

		defer openFile.Close()
		defer fileBck.Close()

		val := "domain\tintra.lan\nsearch\tintra.lan\nnameserver\t10.10.10.1"
		if file == "hosts" {
			val = "127.0.0.1\tlocalhost\n127.0.1.1\t" + contName
		}
		if log.Check(log.WarnLevel, "Applying new config", ioutil.WriteFile(filePath+file, []byte(val), 0644)) {
			rollbackGW(contName)
		}
	}
}

func rollbackGW(contName string) {
	filePath := config.Agent.LxcPrefix + contName + "/rootfs/etc/"
	for _, file := range []string{"hosts", "resolv.conf"} {
		log.Check(log.FatalLevel, "Removing incorrect"+file, os.Remove(filePath+file))
		log.Check(log.FatalLevel, "Restoring backup", os.Rename(filePath+file+".BACKUP", filePath+file))
	}
}
