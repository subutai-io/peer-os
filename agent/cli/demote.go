package lib

import (
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/fs"
	"github.com/subutai-io/base/agent/log"
)

func LxcDemote(name, ip, vlan string) {
	if !container.IsTemplate(name) {
		log.Error("Container " + name + " is not a template")
	}

	LxcNetwork(name, ip, vlan, false, false)
	fs.ReadOnly(name, false)
	log.Info(name + " demote succesfully")
}
