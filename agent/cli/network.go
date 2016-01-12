package lib

import (
	"io/ioutil"
	"os"
	"strconv"
	"strings"
	"subutai/config"
	"subutai/lib/container"
	"subutai/lib/net"
	"subutai/log"
)

func LxcNetwork(name, ip, vlan string, r, l bool) {
	// check: if container name given
	if !container.IsContainer(name) {
		log.Error("Container " + name + " doesn't exist")
	}
	if r {
		removeNetConf(name)
		log.Info("Environment IP and VLAN ID Removing Done")
	} else if l {
		listNetConf(name)
	} else {
		makeNetConf(name, ip, vlan)
		log.Info("Network is configured.")
	}
}

func makeNetConf(name, ip, vlan string) {
	container.SetContainerConf(name, [][]string{
		{"lxc.network.ipv4", ip},
		{"lxc.network.link", ""},
		{"lxc.network.veth.pair", "x"},
		{"lxc.network.script.up", config.Agent.AppPrefix + "bin/create_ovs_interface"},
		{"#vlan_id", vlan},
	})

	// check: iv vlan integer... creates a ovs problem otherwise.
	if _, err := strconv.Atoi(vlan); err == nil {
		// check: calculate and get default gateway
		gw := net.CalculateGW(ip)
		upstart := config.Agent.LxcPrefix + name + "/rootfs/etc/network/if-up.d/upstart"
		upfile, err := os.OpenFile(upstart, os.O_APPEND|os.O_WRONLY, 0600)
		log.Check(log.FatalLevel, "opening upstart file", err)

		defer upfile.Close()
		_, err = upfile.Write([]byte(gw))
		log.Check(log.FatalLevel, "writing upstart file", err)

		// check: update network.
		iface := container.GetConfigItem(config.Agent.LxcPrefix+name+"/config", "lxc.network.veth.pair")
		net.UpdateNetwork(iface, vlan)
		if container.State(name) != "RUNNING" {
			container.Start(name)
		}
		_, err = container.AttachExec(name, []string{"ifconfig", "eth0", ip})
		log.Check(log.FatalLevel, "setting container IP", err)
	}

}

func removeNetConf(name string) {
	log.Info("removing from " + name)
	if container.GetConfigItem(config.Agent.LxcPrefix+name+"/config", "lxc.network.ipv4") == "" &&
		container.GetConfigItem(config.Agent.LxcPrefix+name+"/config", "#vlan_id") == "" {
		log.Info("network items already removed")
	}
	// check: configure ovs : gets lxc.network.ipv4 -> convert and exec:ovs-vsctl
	iface := container.GetConfigItem(config.Agent.LxcPrefix+name+"/config", "lxc.network.veth.pair")
	net.ConfigureOVS(iface)
	if container.State(name) != "RUNNING" {
		container.Start(name)
	}
	_, err := container.AttachExec(name, []string{"ifconfig", "eth0", "0"})
	log.Check(log.WarnLevel, "resetting eth0", err)

	fp, err := ioutil.ReadFile(config.Agent.LxcPrefix + name + "/config")
	log.Check(log.FatalLevel, "reading config file", err)

	fpA := strings.Split(string(fp), "\n")
	for k, v := range fpA {
		if strings.Contains(v, "lxc.network.ipv4") || strings.Contains(v, "#vlan_id") {
			fpA[k] = ""
		}
	}
	fpS := strings.Join(fpA, "\n")
	log.Check(log.FatalLevel, "writing config file", ioutil.WriteFile(config.Agent.LxcPrefix+name+"/config", []byte(fpS), 0744))
}

func listNetConf(name string) {
	log.Info("list for " + name)
	log.Info("Environment IP: " + container.GetConfigItem(config.Agent.LxcPrefix+name+"/config", "lxc.network.ipv4"))
	log.Info("Vlan ID: " + container.GetConfigItem(config.Agent.LxcPrefix+name+"/config", "#vlan_id"))
}
