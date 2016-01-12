package lib

import (
	"subutai/lib/container"
	"subutai/log"
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
