package lib

import (
	"github.com/subutai-io/Subutai/agent/lib/container"
	"github.com/subutai-io/Subutai/agent/log"
)

// LxcStop stops the given containerName
func LxcStop(name string) {
	if container.IsContainer(name) && container.State(name) == "RUNNING" {
		container.Stop(name)
	}
	if container.State(name) == "STOPPED" {
		log.Info(name + " stopped")
	} else {
		log.Error(name + " stop failed")
	}
}
