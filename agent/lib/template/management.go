package template

import (
	"crypto/rand"
	"fmt"
	"github.com/subutai-io/Subutai/agent/config"
        "github.com/subutai-io/Subutai/agent/lib/container"
        "github.com/subutai-io/Subutai/agent/lib/fs"
        "github.com/subutai-io/Subutai/agent/log"
	"os"
	"os/exec"
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
	})

	container.Start("management")
	exec.Command("dhclient", "mng-net")
	os.Exit(0)

}

func MngDel() {
	exec.Command("ovs-vsctl", "del-port", "wan", "management").Run()
	exec.Command("ovs-vsctl", "del-port", "wan", "mng-gw").Run()
}
