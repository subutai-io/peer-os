package lib

import (
	"fmt"
	"github.com/subutai-io/Subutai/agent/config"
	"github.com/subutai-io/Subutai/agent/lib/net"
	"github.com/subutai-io/Subutai/agent/lib/net/p2p"
	"github.com/subutai-io/Subutai/agent/log"
	"os"
	"os/exec"
	"strings"
)

func P2P(c, d, u, l, p bool, args []string) {
	if c {
		if len(args) > 8 {
			p2p.Create(args[4], args[5], args[6], args[7], args[8])
		} else {
			fmt.Println("Wrong usage")
		}
	} else if u {
		if len(args) > 6 {
			p2p.UpdateKey(args[4], args[5], args[6])
		} else {
			fmt.Println("Wrong usage")
		}
	} else if d {
		if len(args) > 4 {
			p2p.Remove(args[4])
		} else {
			fmt.Println("Wrong usage")
		}
	} else if p {
		if len(args) > 4 {
			p2p.Peers(args[4])
		} else {
			fmt.Println("Wrong usage")
		}
	} else if l {
		p2p.Print()
	}
}

func LxcManagementNetwork(args []string) {
	if len(args) < 3 {
		log.Error("Not enough arguments")
	}
	switch args[2] {
	case "-D", "--deletegateway":
		net.DeleteGateway(args[3])
	case "-v", "--listvnimap":
		listVNIMap()
	case "-r", "--removetunnel":
		removeTunnel(args[3])
	case "-T", "--creategateway":
		return
		// net.CreateGateway(args[3], args[4])
	case "-M", "--removevni":
		delVNI(args[3], args[4], args[5])
	case "-E", "--reservevni":
		reserveVNI(args[3], args[4], args[5])
	case "-m", "--createvnimap":
		createVNIMap(args[3], args[4], args[5], args[6])
	case "-c", "--createtunnel":
		log.Check(log.FatalLevel, "Creating tunnel", createTunnel(args[3], args[4], args[5]))
	case "-l", "--listtunnel":
		fmt.Println("List of Tunnels\n--------")
		for _, v := range listTunnel() {
			fmt.Println(string(v))
		}
	case "-Z", "--vniop":
		switch args[3] {
		case "deleteall":
			net.DeleteAllVNI(args[4])
			net.DeleteGateway(args[4])
		case "delete":
			net.DeleteVNI(args[4], args[5], args[6])
		case "list":
			net.ListVNI()
		}
	}
}

func createTunnel(tunnel, addr, tunType string) error {
	net.IfTunExist(tunnel)
	log.Check(log.FatalLevel, "Checking IP validity "+addr, net.CheckIPValidity(listTunnel(), addr))
	if tunType == "vxlan" || tunType == "gre" {
		log.Check(log.FatalLevel, "Creating tunnel port",
			exec.Command("ovs-vsctl", "--may-exist", "add-port", "wan", tunnel, "--", "set", "interface", tunnel, "type="+tunType,
				"options:stp_enable=true", "options:key=flow", "options:remote_ip="+addr).Run())
	} else {
		log.Error("Tunnel type must be vxlan or gre")
	}
	return nil
}

func listTunnel() []string {
	var list []string

	ret, err := exec.Command("ovs-vsctl", "show").CombinedOutput()
	log.Check(log.FatalLevel, "Getting OVS interfaces list", err)
	ports := strings.Split(string(ret), "\n")

	for k, port := range ports {
		if strings.Contains(string(port), "remote_ip") {
			iface := strings.Fields(ports[k-2])
			tunnel := strings.Trim(iface[1], "\"")
			addr := strings.Fields(port)
			line := tunnel + "-" + strings.Trim(strings.Trim(addr[2], "remote_ip="), "\",")
			list = append(list, line)
		}
	}
	return list
}

func removeTunnel(tunnel string) {
	log.Check(log.WarnLevel, "Removing "+tunnel+"_vni_vlan",
		os.Remove(config.Agent.DataPrefix+"var/subutai-network/"+tunnel+"_vni_vlan"))
	log.Check(log.FatalLevel, "Removing port "+tunnel,
		exec.Command("ovs-vsctl", "--if-exists", "del-port", tunnel).Run())
}

func createVNIMap(tunnelPortName, vni, vlan, envid string) {
	if _, err := os.Stat(config.Agent.DataPrefix + "/var/subutai-network/vni_reserve"); os.IsNotExist(err) {
		log.Error("Do Reserve first. No reserved VNIs, file for reserved VNI is not exist")
	}
	net.CreateVNIFile(tunnelPortName + "_vni_vlan")
	net.MakeVNIMap(tunnelPortName, vni, vlan, envid)
}

func listVNIMap() {
	if _, err := os.Stat(config.Agent.DataPrefix + "/var/subutai-network/"); os.IsNotExist(err) {
		log.Error("Network data folder: " + err.Error())
	}
	net.DisplayVNIMap()
}

func delVNI(tunnelPortName, vni, vlan string) {
	if _, err := os.Stat(config.Agent.DataPrefix + "/var/subutai-network/"); os.IsNotExist(err) {
		log.Error("Network data folder: " + err.Error())
	}
	net.DelVNI(tunnelPortName, vni, vlan)
	log.Info(vni + " " + vlan + " deleted from " + tunnelPortName)
}

func reserveVNI(vni, vlan, envid string) {
	// check: create vni file
	net.CreateVNIFile("vni_reserve")
	net.MakeReservation(vni, vlan, envid)
}
