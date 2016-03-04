package lib

import (
	"github.com/subutai-io/Subutai/agent/config"
	"github.com/subutai-io/Subutai/agent/lib/container"
	"github.com/subutai-io/Subutai/agent/lib/net"
	"github.com/subutai-io/Subutai/agent/lib/template"
)

func LxcDestroy(name string) {
	net.DelIface(container.GetConfigItem(config.Agent.LxcPrefix+name+"/config", "lxc.network.veth.pair"))
	container.Destroy(name)

	if name == "management" {
		template.MngDel()
	}
}
