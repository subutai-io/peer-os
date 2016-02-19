package lib

import (
	"github.com/subutai-io/Subutai/agent/config"
	"github.com/subutai-io/Subutai/agent/lib/container"
	"github.com/subutai-io/Subutai/agent/lib/net"
	"github.com/subutai-io/Subutai/agent/lib/template"
	"github.com/subutai-io/Subutai/agent/log"
)

func LxcDestroy(name string) {
	if !container.IsContainer(name) {
		log.Error(name + " doesn't exist")
	}
	net.DelIface(container.GetConfigItem(config.Agent.LxcPrefix+name+"/config", "lxc.network.veth.pair"))
	container.Destroy(name)

	if name == "management" {
		template.MngDel()
	}
}
