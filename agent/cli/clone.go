package lib

import (
	"net"
	"strings"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/gpg"
	"github.com/subutai-io/base/agent/log"
)

// LxcClone function creates new `child` container from a Subutai `parent` template.
//
// If the specified template argument is not deployed in system, Subutai first tries to import it, and if import succeeds, it then continues to clone from the imported template image.
// By default, clone will use the NAT-ed network interface with IP address received from the Subutai DHCP server, but this behavior can be changed with command options described below.
//
// If `-i` option is defined, separate bridge interface will be created in specified VLAN and new container will receive static IP address.
// Option `-e` writes the environment ID string inside new container.
// Option `-t` is intended to check the origin of new container creation request during environment build.
// This is one of the security checks which makes sure that each container creation request is authorized by registered user.
//
// The clone options are not intended for manual use: unless you're confident about what you're doing. Use default clone format without additional options to create Subutai containers.
func LxcClone(parent, child, envId, addr, token string) {
	if id := strings.Split(parent, "id:"); len(id) > 1 {
		kurjun, _ := config.CheckKurjun()
		parent = idToName(id[1], kurjun, token)
	}

	if !container.IsTemplate(parent) {
		LxcImport(parent, "", token, false)
	}
	if container.IsContainer(child) {
		log.Error("Container " + child + " already exist")
	}

	container.Clone(parent, child)
	gpg.GenerateKey(child)

	if len(token) != 0 {
		gpg.ExchageAndEncrypt(child, token)
	}

	if len(envId) != 0 {
		container.SetEnvID(child, envId)
	}

	if len(addr) != 0 {
		addNetConf(child, addr)
	}

	//Need to change it in parent templates
	container.SetContainerUID(child)
	container.SetApt(child)
	container.SetDNS(child)

	//Security matters workaround. Need to change it in parent templates
	container.DisableSSHPwd(child)

	LxcStart(child)

	log.Info(child + " with ID " + gpg.GetFingerprint(child) + " successfully cloned")

}

// addNetConf adds network related configuration values to container config file
func addNetConf(name, addr string) {
	ipvlan := strings.Fields(addr)
	_, network, _ := net.ParseCIDR(ipvlan[0])
	gw := []byte(network.IP)
	gw[3]++
	container.SetContainerConf(name, [][]string{
		{"lxc.network.ipv4", ipvlan[0]},
		{"lxc.network.ipv4.gateway", net.IP(gw).String()},
		{"#vlan_id", ipvlan[1]},
	})
	container.SetStaticNet(name)
}
