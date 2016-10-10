package lib

import (
	"net"
	"strings"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/gpg"
	"github.com/subutai-io/base/agent/log"
)

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
		container.SetEnvId(child, envId)
	}

	if len(addr) != 0 {
		addNetConf(child, addr)
	}

	//Need to change it in parent templates
	container.SetContainerUid(child)
	container.SetApt(child)
	container.SetDns(child)

	//Security matters workaround. Need to change it in parent templates
	container.DisableSSHPwd(child)

	LxcStart(child)

	log.Info(child + " with ID " + gpg.GetFingerprint(child) + " successfully cloned")

}

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
