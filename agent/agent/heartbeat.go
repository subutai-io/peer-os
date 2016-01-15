package agent

import (
	"subutai/agent/alert"
	"subutai/agent/container"
	"subutai/agent/utils"
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
