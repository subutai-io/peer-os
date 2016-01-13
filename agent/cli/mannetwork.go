package lib

import (
	"fmt"
	"io/ioutil"
	n "net"
	"os"
	"os/exec"
	"strconv"
	"strings"
	"subutai/config"
	"subutai/lib/net"
	"subutai/log"
	"syscall"
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
		listOpenedN2NTunnel()
	case "-D", "--deletegateway":
		deleteGateway(args[3])
	case "-S", "--listopenedtab":
		listOpenedTapDevice()
	case "-V", "--removetab":
		removeTapDevice(args[3])
	case "-v", "--listvnimap":
		listVNIMap()
	case "-r", "--removetunnel":
		removeTunnel(args[3])
	case "-e", "--reloadn2n":
		reloadN2N(args[3], args[4])
	case "-T", "--creategateway":
		createGateway(args[3], args[4])
	case "-M", "--removevni":
		delVNI(args[3], args[4], args[5])
	case "-R", "--removen2n":
		removeN2NTunnel(args[3], args[4])
	case "-E", "--reservvni":
		reservVNI(args[3], args[4], args[5])
	case "-m", "--createvnimap":
		createVNIMap(args[3], args[4], args[5], args[6])
	case "-c", "--createtunnel":
		log.Check(log.FatalLevel, "create tunnel", createTunnel(args[3], args[4], args[5]))
	case "-f", "--addflow":
		addFlow(args[3], args[4])
		log.Info("Flow configuration added")
	case "-l", "--listtunnel":
		liste := listTunnel()
		fmt.Println("List of Tunnels\n--------")
		for _, v := range liste {
			fmt.Println(string(v))
		}
	case "-d", "--deleteflow":
		if len(args)-3 < 2 {
			deleteFlow(args[3], "")
		} else {
			deleteFlow(args[3], args[4])
		}
	case "-N", "--addn2n":
		p2pTunnel(args[5], args[6], args[7])
		// if len(args)-3 == 8 {
		// func createN2NTunnel(interfaceName, communityName, localPeepIPAddr) {
		// func createN2NTunnel(superNodeIPaddr, superNodePort, interfaceName, communityName,localPeepIPAddr, keyType, keyFile, managementPort string) {
		// createN2NTunnel(args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10])
		// } else if len(args)-3 == 7 { // management port can be empty
		// createN2NTunnel(args[3], args[4], args[5], args[6], args[7], args[8], args[9], "")
		// } else {
		// log.Warn("please check you have given all arguments correctly")
		// }
	case "-Z", "--vniop":
		switch args[3] {
		case "deleteall":
			net.DeleteAllVNI(args[4])
			deleteGateway(args[4])
		case "delete":
			net.DeleteVNI(args[4], args[5], args[6])
		case "list":
			net.ListVNI()
		}
	}
}

func p2pTunnel(interfaceName, communityName, localPeepIPAddr string) {
	log.Check(log.FatalLevel, "p2p command: ", exec.Command("p2p", "-dev", interfaceName, "-ip", localPeepIPAddr, "-hash", communityName).Run())
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

func addFlow(bridgeName, flowConfiguration string) {
	log.Info("flow configuration adding with: " + bridgeName + " " + flowConfiguration)
	log.Check(log.FatalLevel, "addFlow: ", net.AddFlowConfig(bridgeName, flowConfiguration))
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
func deleteFlow(bridgeName, matchCase string) {
	log.Check(log.FatalLevel, "deleteflow ", net.DeleteFlow(bridgeName, matchCase))
	log.Info(bridgeName + " flows are deleted according to " + matchCase)
}

// refer to n2n.go
func createN2NTunnel(superNodeIPaddr, superNodePort, interfaceName, communityName,
	localPeepIPAddr, keyType, keyFile, managementPort string) {
	// check: if there is /var/subutai-network/edgePorts.txt
	log.Info("n2n create tunnel started")
	log.Info("superNode IP: " + superNodeIPaddr)
	log.Info("superNode port: " + superNodePort)
	log.Info("interface name: " + interfaceName)
	log.Info("community name: " + communityName)
	log.Info("local peer IP: " + localPeepIPAddr)
	log.Info("key type: " + keyType)
	log.Info("key file: " + keyFile)
	log.Info("management port: " + managementPort)
	net.CheckOrCreateEdgePortFile()
	// check: if port is given or available.

	if managementPort == "" {
	CutTheLoop:
		for i := 5645; i < 65535; i++ {
			_, err := n.Listen("udp", ":"+string(i))
			if err != nil {
				// then this port is empty.
				managementPort = strconv.Itoa(i)
				break CutTheLoop // cut the loop
			}
		}
	} else if _, err := n.Listen("udp", managementPort); err == nil {
		// ups this is port is being used...
		log.Error("port is used by other process. " + err.Error())
	}
	if managementPort == "" {
		// still??? then there is no port left
		log.Error("no available port in computer")
	}
	net.ProcessEdge(superNodeIPaddr, superNodePort, interfaceName, communityName,
		localPeepIPAddr, keyType, keyFile, managementPort)

}

func listOpenedN2NTunnel() {
	net.PrintN2NTunnels()
}

func removeN2NTunnel(interfaceName, communityName string) {
	pid := net.ReturnPID(interfaceName, communityName)
	i, _ := strconv.Atoi(pid)
	log.Check(log.FatalLevel, "remove n2n tunnel: ", syscall.Kill(i, syscall.SIGHUP))
	f, err := ioutil.ReadFile(config.Agent.DataPrefix + "/var/subutai-network/edgePorts.txt")
	if err != nil {
		log.Error("read from edgePorts.txt " + err.Error())
	}
	lines := strings.Split(string(f), "\n")
	for k, v := range lines {
		temp := strings.Split(string(v), ",")
		if temp[2] == interfaceName && temp[3] == communityName {
			lines[k] = ""
		}
	}
	str := []byte(strings.Join(lines, "\n"))
	log.Check(log.FatalLevel, "write edgePorts.txt: ",
		ioutil.WriteFile(config.Agent.DataPrefix+"/var/subutai-network/edgePorts.txt", []byte(str), 0744))
	log.Info(interfaceName + " " + communityName + " " + " n2n tunnel is removed")
}

func createGateway(vlanip, vlanid string) {
	net.CreateGateway(vlanip, vlanid)
	log.Info("gateway created")
}

func deleteGateway(vlanid string) {
	net.DeleteGateway(vlanid)
	log.Info("gateway " + vlanid + " removed")
}

func listOpenedTapDevice() {
	net.ListTapDevice()
}

func removeTapDevice(interfaceName string) {
	pid := net.ReturnPID(interfaceName, " ") // i don't know if this will work..
	i, _ := strconv.Atoi(pid)
	log.Check(log.FatalLevel, "remove tap device: ", syscall.Kill(i, syscall.SIGHUP))
	log.Info(interfaceName + " tap device removed")
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
	net.CheckOrCreateEdgePortFile()
	net.ReloadN2N(interfaceName, communityName)
	log.Info(interfaceName + " " + communityName + " reloaded")
}
