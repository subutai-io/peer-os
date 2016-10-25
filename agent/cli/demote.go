package lib

import (
	"strings"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/fs"
	"github.com/subutai-io/base/agent/log"
)

// LxcDemote converts template into regular Subutai container.
//
// A Subutai template is a "locked down" container only to be used for cloning purposes. It cannot be started, and its file system cannot be modified: it's read-only.
// Normal operational containers are promoted into templates, but sometimes you might want to demote them back to regular containers.
// This is what the demote sub command does: it reverts a template without children back into a normal container.
//
// Demoted container will use NAT network interface and dynamic IP address if opposite options are not specified.
func LxcDemote(name, ip, vlan string) {
	if !container.IsTemplate(name) {
		log.Error("Container " + name + " is not a template")
	}

	netConf(name, ip, vlan)
	fs.ReadOnly(name, false)
	container.SetContainerUID(name)
	log.Info(name + " demote succesfully")
}

// netConf sets default values for container network
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
