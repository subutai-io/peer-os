package agent

import (
	"os"
	"runtime"
	"subutai/agent/alert"
	"subutai/agent/container"
	"subutai/agent/utils"
	"subutai/config"
	"subutai/log"
)

type Response struct {
	Beat Heartbeat `json:"response"`
}

type Heartbeat struct {
	Type       string                `json:"type"`
	Hostname   string                `json:"hostname"`
	Id         string                `json:"id"`
	Arch       string                `json:"arch"`
	Instance   string                `json:"instance"`
	Interfaces []utils.Iface         `json:"interfaces,omitempty"`
	Containers []container.Container `json:"containers"`
	Alert      []alert.Load          `json:"alert, omitempty"`
}

func Beat() *Heartbeat {
	hn, err := os.Hostname()
	log.Check(log.WarnLevel, "Getting hostname for heartbeat", err)
	beat := &Heartbeat{
		Type:       config.Broker.HeartbeatTopic,
		Hostname:   hn,
		Arch:       runtime.GOARCH,
		Interfaces: utils.GetInterfaces(),
		Containers: container.GetActiveContainers(false),
	}
	return beat
}
