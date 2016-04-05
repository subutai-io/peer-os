package lib

import (
	"time"

	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/log"
)

// LxcStart starts the given containerName
func LxcStart(name string) {
	if container.IsContainer(name) && container.State(name) == "STOPPED" {
		container.Start(name)
	}
	state := container.State(name)
	for i := 0; i < 60; i++ {
		if state == "RUNNING" || state == "STARTING" {
			log.Info(name + " started")
			return
		}
		log.Info("Waiting for container start (60 sec)")
		time.Sleep(time.Second)
		state = container.State(name)
	}
	log.Error(name + " start failed.")
}
