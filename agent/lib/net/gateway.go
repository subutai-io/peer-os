package net

import (
	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/log"
	"io"
	"io/ioutil"
	"os"
	"os/exec"
)

// DelIface removes OVS bridges and ports by name, brings system interface down
func DelIface(iface string) {
	log.Debug("Removing interface " + iface)
	exec.Command("ovs-vsctl", "--if-exists", "del-br", iface).Run()
	exec.Command("ovs-vsctl", "--if-exists", "del-port", iface).Run()
	exec.Command("ifconfig", iface, "down").Run()
}

// RestoreDefaultConf restores default values in "hosts" and "resolv.conf" inside container
func RestoreDefaultConf(contName string) {
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
			rollbackConf(contName)
		}
	}
}

// If RestoreDefaultConf fails, rollbackConf restores old configs
func rollbackConf(contName string) {
	filePath := config.Agent.LxcPrefix + contName + "/rootfs/etc/"
	for _, file := range []string{"hosts", "resolv.conf"} {
		log.Check(log.FatalLevel, "Removing incorrect"+file, os.Remove(filePath+file))
		log.Check(log.FatalLevel, "Restoring backup", os.Rename(filePath+file+".BACKUP", filePath+file))
	}
}
