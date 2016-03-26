package lib

import (
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/log"
)

// LxcStop stops the given containerName
func LxcStart(name string) {
	if container.IsContainer(name) && container.State(name) == "STOPPED" {
		container.Start(name)
	}
	state := container.State(name)
	if state == "RUNNING" || state == "STARTING" {
		log.Info(name + " started")
	} else {
		log.Error(name + " start failed. State: " + state)
	}
}
