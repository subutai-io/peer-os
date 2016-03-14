package lib

import (
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/template"
	"github.com/subutai-io/base/agent/log"
)

// LxcStop stops the given containerName
func LxcStop(name string) {
	if container.IsContainer(name) && container.State(name) == "RUNNING" {
		container.Stop(name)
	}
	if container.State(name) == "STOPPED" {
		log.Info(name + " stopped")
		if name == "management" {
			template.MngStop()
		}
	} else {
		log.Error(name + " stop failed")
	}
}
