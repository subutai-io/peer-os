package lib

import (
	"fmt"
	"os/exec"
	"strings"

	"github.com/subutai-io/base/agent/lib/net"
	"github.com/subutai-io/base/agent/log"
)

func VxlanTunnel(create, del, remoteip, vlan, vni string, list bool) {
	if len(create) > 0 {
		tunnelCreate(create, remoteip, vlan, vni)
	} else if len(del) > 0 {
		net.DelIface(del)
		return
	} else if list {
		tunnelList()
		return
	}
}

func tunnelCreate(tunnel, addr, vlan, vni string) {
	log.Check(log.WarnLevel, "Creating bridge ", exec.Command("ovs-vsctl", "--may-exist", "add-br", "gw-"+vlan).Run())

	log.Check(log.FatalLevel, "Creating tunnel port",
		exec.Command("ovs-vsctl", "--may-exist", "add-port", "gw-"+vlan, tunnel, "--", "set", "interface", tunnel, "type=vxlan",
			"options:stp_enable=true", "options:key="+vni, "options:remote_ip="+string(addr)).Run())

	log.Check(log.FatalLevel, "MakeVNIMap set port: ", exec.Command("ovs-vsctl", "--if-exists", "set", "port", tunnel, "tag="+vlan).Run())
}

func tunnelList() {
	ret, err := exec.Command("ovs-vsctl", "show").CombinedOutput()
	log.Check(log.FatalLevel, "Getting OVS interfaces list", err)
	ports := strings.Split(string(ret), "\n")

	for k, port := range ports {
		if strings.Contains(port, "remote_ip") {
			tunnel := strings.Trim(strings.Trim(ports[k-2], "Interface "), "\"")
			tag := strings.TrimLeft(ports[k-3], "tag: ")
			addr := strings.Fields(port)
			vni := strings.Trim(strings.Trim(addr[1], "{key="), "\",")
			ip := strings.Trim(strings.Trim(addr[2], "remote_ip="), "\",")
			fmt.Println(tunnel, ip, tag, vni)
		}
	}
}
