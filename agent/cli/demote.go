package lib

import (
	"strings"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/fs"
	"github.com/subutai-io/base/agent/log"
)

func LxcDemote(name, ip, vlan string) {
	if !container.IsTemplate(name) {
		log.Error("Container " + name + " is not a template")
	}

	netConf(name, ip, vlan)
	fs.ReadOnly(name, false)
	container.SetContainerUid(name)
	log.Info(name + " demote succesfully")
}

func netConf(name, ip, vlan string) {
	container.SetContainerConf(name, [][]string{
		{"lxc.network.ipv4", ip},
		{"lxc.network.link", ""},
		{"lxc.network.veth.pair", strings.Replace(container.GetConfigItem(config.Agent.LxcPrefix+name+"/config", "lxc.network.hwaddr"), ":", "", -1)},
		{"lxc.network.script.up", config.Agent.AppPrefix + "bin/create_ovs_interface"},
		{"#vlan_id", vlan},
	})
	return
}
