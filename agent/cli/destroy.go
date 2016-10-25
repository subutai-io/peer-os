package lib

import (
	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/net"
	"github.com/subutai-io/base/agent/lib/template"
	"github.com/subutai-io/base/agent/log"
)

// LxcDestroy simply removes every resource associated with a Subutai container or template:
// data, network, configs, etc.
//
// The destroy command always runs each step in "force" mode to provide reliable deletion results;
// even if some instance components were already removed, the destroy command will continue to perform all operations
// once again while ignoring possible underlying errors: i.e. missing configuration files.
func LxcDestroy(name string) {
	if len(name) == 0 {
		log.Error("Please specify container or template name")
	}
	net.DelIface(container.GetConfigItem(config.Agent.LxcPrefix+name+"/config", "lxc.network.veth.pair"))
	container.Destroy(name)

	if name == "management" {
		template.MngStop()
		template.MngDel()
	}
}
