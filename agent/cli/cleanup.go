package lib

import (
	"github.com/influxdata/influxdb/client/v2"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/net"
	"github.com/subutai-io/base/agent/lib/net/p2p"
)

// Cleanup command takes the environment's VLAN tag as its only argument.
// It performs a number of operations on the system to remove all resources associated with the environment and its tag components:
// containers, network interfaces, proxy service configurations, environment statistics, etc.
func Cleanup(vlan string) {
	for _, name := range container.Containers() {
		if container.GetConfigItem(config.Agent.LxcPrefix+name+"/config", "#vlan_id") == vlan {
			LxcDestroy(name)
		}
	}
	net.DelIface("gw-" + vlan)
	p2p.RemoveByIface("p2p" + vlan)
	cleanupNetStat(vlan)
	ProxyDel(vlan, "", true)
}

// cleanupNetStat drops data from database about network trafic for specified VLAN
func cleanupNetStat(vlan string) {
	c, _ := client.NewHTTPClient(client.HTTPConfig{
		Addr:               "https://" + config.Influxdb.Server + ":8086",
		Username:           config.Influxdb.User,
		Password:           config.Influxdb.Pass,
		InsecureSkipVerify: true,
	})
	queryInfluxDB(c, `drop series from host_net where iface = 'p2p`+vlan+`'`)
	queryInfluxDB(c, `drop series from host_net where iface = 'gw-`+vlan+`'`)
}
