package connect

import (
	"encoding/json"
	"errors"
	"net"
	"os"
	"runtime"
	"strings"
	"subutai/agent/container"
	"subutai/agent/utils"
	"subutai/log"
)

const (
	RHostID = "HOST_ID"
)

type RHost struct {
	UUID       string                `json:"id"`
	Hostname   string                `json:"hostname"`
	Pk         string                `json:"publicKey"`
	Cert       string                `json:"cert"`
	Secret     string                `json:"secret"`
	Ifaces     []utils.Iface         `json:"interfaces"`
	Arch       string                `json:"arch"`
	Ipv4       string                `json:"-"`
	Containers []container.Container `json:"hostInfos"`
}

func NewRH() *RHost {
	name, err := os.Hostname()
	log.Check(log.WarnLevel, "Getting hostname", err)
	ipv4, err := getIp()
	log.Check(log.WarnLevel, "Getting IP-address", err)

	return &RHost{
		Hostname:   name,
		Ifaces:     utils.GetInterfaces(),
		UUID:       "",
		Ipv4:       ipv4,
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

// gets gpg public key from ~/.gnupg/pubring.gpg
func (r *RHost) GetKey(name string) string {
	pk := utils.GetPk(name)
	return pk
}

func getIp() (string, error) {
	ifaces, err := net.Interfaces()
	log.Check(log.WarnLevel, "Getting interfaces list", err)
	for _, i := range ifaces {
		addrs, err := i.Addrs()
		log.Check(log.WarnLevel, "Getting IP-addresses for interfaces", err)
		for _, addr := range addrs {
			var ip net.IP
			switch v := addr.(type) {
			case *net.IPNet:
				ip = v.IP
			case *net.IPAddr:
				ip = v.IP
			}
			if ip == nil || ip.IsLoopback() {
				continue
			}
			ip = ip.To4()
			if ip == nil {
				continue
			}
			return ip.String(), nil
		}
	}
	return "", errors.New("Could not get IPv4")
}
