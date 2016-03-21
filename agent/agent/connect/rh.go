package connect

import (
	"encoding/json"
	"os"
	"runtime"
	"strings"

	"github.com/subutai-io/base/agent/agent/container"
	"github.com/subutai-io/base/agent/agent/utils"
	"github.com/subutai-io/base/agent/log"
)

type RHost struct {
	UUID       string                `json:"id"`
	Hostname   string                `json:"hostname"`
	Pk         string                `json:"publicKey"`
	Cert       string                `json:"cert"`
	Secret     string                `json:"secret"`
	Ifaces     []utils.Iface         `json:"interfaces"`
	Arch       string                `json:"arch"`
	Containers []container.Container `json:"hostInfos"`
}

func NewRH() *RHost {
	name, _ := os.Hostname()
	return &RHost{
		Hostname:   name,
		Cert:       utils.PublicCert(),
		Ifaces:     utils.GetInterfaces(),
		Arch:       strings.ToUpper(runtime.GOARCH),
		Containers: container.GetActiveContainers(true),
	}
}

func (r *RHost) Json() string {
	enc, err := json.Marshal(r)
	if log.Check(log.WarnLevel, "Marshal Resource host json", err) {
		return ""
	}
	return string(enc)
}
