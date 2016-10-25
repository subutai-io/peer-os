package lib

import (
	"time"

	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/log"
)

// LxcStart starts a Subutai container and checks if container state changed to "running" or "starting".
// If state is not changing for 60 seconds, then the "start" operation is considered to have failed.
func LxcStart(name string) {
	if container.IsContainer(name) && container.State(name) == "STOPPED" {
		container.Start(name)
	} else {
		return
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
