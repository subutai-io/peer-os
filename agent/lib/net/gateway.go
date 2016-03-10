package net

import (
	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/log"
	"io"
	"io/ioutil"
	"net"
	"os"
	"os/exec"
)

func CalculateGW(ipS string) string {
	ip, ipnet, err := net.ParseCIDR(ipS)
	log.Check(log.FatalLevel, "Parsing IP", err)
	ip = ip.To4()
	ip = ip.Mask(ipnet.Mask)
	ip[3]++
	return ip.String()
}

func DeleteGateway(vlan string) {
	log.Debug("Removeing gateway gw-" + vlan)
	exec.Command("ovs-vsctl", "del-port", "gw-"+vlan).Run()
	exec.Command("ifconfig", "gw-"+vlan, "down").Run()
}

func DelIface(iface string) {
	log.Debug("Removing interface " + iface)
	exec.Command("ovs-vsctl", "del-port", iface).Run()
	exec.Command("ifconfig", iface, "down").Run()
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
