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
	if container.State(name) == "RUNNING" {
		log.Info(name + " started")
	} else {
		log.Error(name + " start failed")
	}
}
