package lib

import (
	"subutai/config"
	"subutai/lib/container"
	"subutai/lib/net"
)

func Cleanup(vlan string) {
	for _, name := range container.Containers() {
		if container.GetConfigItem(config.Agent.LxcPrefix+name+"/config", "#vlan_id") == vlan {
			container.Destroy(name)
		}
	}

	net.DeleteGateway(vlan)
	net.DeleteAllVNI(vlan)
	ProxyDel(vlan, "", true)
}
