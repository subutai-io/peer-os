package lib

import (
	"fmt"
	"os"
	"strings"
	"subutai/config"
	"subutai/lib/net"
	"subutai/log"
)

func LxcManagementNetwork(args []string) {
	if len(args) < 3 {
		log.Error("Not enough arguments")
	}
	switch args[2] {
	case "-s", "--showflow":
		showFlow(args[3])
	case "-p", "--showport":
		showPort(args[3])
	case "-L", "--listn2n":
		net.PrintN2NTunnels()
	case "-D", "--deletegateway":
		net.DeleteGateway(args[3])
	case "-S", "--listopenedtap":
		net.ListTapDevice()
	case "-v", "--listvnimap":
		listVNIMap()
	case "-r", "--removetunnel":
		removeTunnel(args[3])
	case "-e", "--reloadn2n":
		reloadN2N(args[3], args[4])
	case "-T", "--creategateway":
		net.CreateGateway(args[3], args[4])
	case "-M", "--removevni":
		delVNI(args[3], args[4], args[5])
	case "-R", "--removen2n":
		net.RemoveP2PTunnel(args[4])
	case "-E", "--reservvni":
		reservVNI(args[3], args[4], args[5])
	case "-m", "--createvnimap":
		createVNIMap(args[3], args[4], args[5], args[6])
	case "-c", "--createtunnel":
		log.Check(log.FatalLevel, "create tunnel", createTunnel(args[3], args[4], args[5]))
	case "-f", "--addflow":
		net.AddFlowConfig(args[3], args[4])
		log.Info("Flow configuration added")
	case "-l", "--listtunnel":
		liste := listTunnel()
		fmt.Println("List of Tunnels\n--------")
		for _, v := range liste {
			fmt.Println(string(v))
		}
	case "-d", "--deleteflow":
		if len(args)-3 < 2 {
			net.DeleteFlow(args[3], "")
		} else {
			net.DeleteFlow(args[3], args[4])
		}
	case "-N", "--addn2n":
		net.CreateP2PTunnel(args[5], args[6], args[7])
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

func createTunnel(tunnelPortName, tunnelIPAddress, tunnelType string) error {
	log.Info("tunnel port name: " + tunnelPortName)
	log.Info("tunnel IP address: " + tunnelIPAddress)
	log.Info("tunnel type: " + tunnelType)
	log.Check(log.FatalLevel, "check tunnel validity ", net.CheckTunnelPortNameValidity(tunnelPortName))
	log.Check(log.FatalLevel, "check ip validity "+tunnelIPAddress, net.CheckIPValidity(listTunnel(), tunnelIPAddress))
	if tunnelType == "vxlan" || tunnelType == "gre" {
		log.Check(log.FatalLevel, "create tunnel ", net.CreateTunnel(listTunnel(), tunnelPortName, tunnelIPAddress, tunnelType))
	} else {
		log.Error("Tunnel type must be vxlan or gre")
	}
	return nil
}
func listTunnel() []string { // we need a list when ip address checking.
	var returnArr []string
	list := net.ListTunnels()
	listA := strings.Split(string(list), "\n")

	for k, v := range listA {
		if strings.Contains(string(v), "remote_ip") {
			devInt := strings.Fields(listA[k-2])
			strLine := strings.Trim(devInt[1], "\"")
			devIP := strings.Fields(v)
			strLine = strLine + "-" + strings.Trim(strings.Trim(devIP[2], "remote_ip="), "\"}")
			returnArr = append(returnArr, strLine)
		}
	}
	return returnArr
}
func removeTunnel(tunnelPortName string) {
	retVal := net.CheckTunnelPortNameValidity(tunnelPortName)
	log.Check(log.WarnLevel, " remove "+tunnelPortName+"_vni_vlan",
		os.Remove(config.Agent.DataPrefix+"var/subutai-network/"+tunnelPortName+"_vni_vlan"))

	// basically it return err if given tunnelPortName exits.
	if retVal != nil {
		log.Info(tunnelPortName + " found in system.")
		log.Check(log.FatalLevel, "remove tunnel", net.RemovePort(tunnelPortName))
		log.Info(tunnelPortName + " removed")
	} else {
		log.Info(tunnelPortName + " not exists in system so NOT to remove")
	}

}

func showFlow(bridgeName string) {
	s, err := net.DumpBridge(bridgeName)
	if err != nil {
		log.Error("showFlow " + string(s))
	}
	log.Info("Flow Table informations of " + bridgeName)
	fmt.Println(s) // uufff...
}

func showPort(bridgeName string) {
	s, err := net.DumpPort(bridgeName)
	if err != nil {
		log.Error("showPort ", string(s))
	}
	log.Info("Port informations of " + bridgeName)
	fmt.Println(s)
}

func createVNIMap(tunnelPortName, vni, vlan, envid string) {
	// check: if there is vni file
	if _, err := os.Stat(config.Agent.DataPrefix + "/var/subutai-network/vni_reserve"); os.IsNotExist(err) {
		log.Error("Do Reserve first. No reserved VNIs, not exist file for reserved VNI")
	}
	net.CreateVNIFile(tunnelPortName + "_vni_vlan")
	// check: control if there is such entry in nvi_reserv file.
	ret, _ := net.CheckVNIFile(tunnelPortName+"_vni_vlan", vni, vlan, envid)
	if ret[0] == true {
		log.Info("vni found")
	}
	if ret[1] == true {
		log.Info("vlanid found")
	}
	if ret[2] == true {
		log.Info("envid found")
	}
	if ret[3] == true {
		log.Info("reservation found")
	}

	net.MakeVNIMap(tunnelPortName, vni, vlan, envid)
	log.Info("vni map created: " + vni + " " + vlan + " " + envid)
}

func listVNIMap() {
	if _, err := os.Stat(config.Agent.DataPrefix + "/var/subutai-network/"); os.IsNotExist(err) {
		log.Error("folder not found" + err.Error())
	}
	net.DisplayVNIMap()
}

func delVNI(tunnelPortName, vni, vlan string) {
	if _, err := os.Stat(config.Agent.DataPrefix + "/var/subutai-network/"); os.IsNotExist(err) {
		log.Error("folder not found" + err.Error())
	}
	net.DelVNI(tunnelPortName, vni, vlan)
	log.Info(vni + " " + vlan + " deleted from " + tunnelPortName)
}

func reservVNI(vni, vlan, envid string) {
	// check: create vni file
	net.CreateVNIFile("vni_reserve")
	net.MakeReservation(vni, vlan, envid)
	log.Info(vni + " " + vlan + " " + envid + " is reserved")

}

func reloadN2N(interfaceName, communityName string) {
	log.Info("This function is not implemented yet")
}
