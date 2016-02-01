package lib

import (
	"github.com/subutai-io/Subutai/agent/lib/container"
	"github.com/subutai-io/Subutai/agent/lib/fs"
	"github.com/subutai-io/Subutai/agent/lib/template"
	"github.com/subutai-io/Subutai/agent/log"
)

func LxcDemote(name, ip, vlan string) {
	checkAvailabilityLxcDemote(name)
	container.SetContainerConf(name, [][]string{
		{"lxc.hook.pre-start", ""},
		{"subutai.template.package", ""},
		{"subutai.git.uuid", ""},
	})
	configureNetworkDemote(name, ip, vlan)
	fs.ReadOnly(name, false)
	log.Info(name + " demote succesfully")
}

func checkAvailabilityLxcDemote(name string) {
	if !container.IsTemplate(name) {
		log.Error("Container " + name + " is not a template")
	}
	if template.IsRegistered(name) {
		log.Error("Container " + name + " is already registered")
	}
}

func configureNetworkDemote(name, ip, vlan string) {
	LxcNetwork(name, ip, vlan, false, false)
}
