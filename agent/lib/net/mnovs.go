package net

import (
	"errors"
	"net"
	"os/exec"
	"strings"
	"subutai/log"
)

func RemovePort(tunnelPortName string) error {

	log.Check(log.FatalLevel, "remove port 1",
		exec.Command("ovs-vsctl", "--if-exists", "del-port", tunnelPortName).Run())
	log.Check(log.FatalLevel, "remove port 2",
		exec.Command("ovs-vsctl", "--if-exists", "del-br", "br-"+tunnelPortName).Run())
	log.Check(log.FatalLevel, "remove port 3",
		exec.Command("ovs-vsctl", "--if-exists", "del-port", "br-int", "intto"+tunnelPortName).Run())
	return nil
}
func CheckTunnelPortNameValidity(name string) error {

	ret, _ := exec.Command("ovs-vsctl", "list-ports", "br-"+name).CombinedOutput()
	str := strings.Split(string(ret), "\n")
	if !strings.Contains(name, "tunnel") {
		return errors.New("tunnel name must containe \"tunnel\" tag")
	}
	for _, v := range str {
		if strings.Contains(string(v), "no bridge named") {
			return nil
		} else if strings.Contains(string(v), name) {
			return errors.New("Tunnel Port Name " + name + " is already exists")
		}
	}
	return nil
}

func ListTunnels() string {
	list, err := exec.Command("ovs-vsctl", "show").CombinedOutput()
	log.Check(log.FatalLevel, "Getting OVS interfaces", err)
	return string(list)
}

func CheckIPValidity(l []string, name string) error {
	ip := net.ParseIP(name) // to see if it is valid ip...

	for _, v := range l {
		if strings.Contains(string(v), ip.String()) {
			return errors.New(ip.String() + " is already used")
		}
	}
	return nil
}

func CreateTunnel(l []string, tunnelPortName, tunnelIPAddress, tunnelType string) error {
	// check: ip addr is already checked if valid... so
	var getTun string
	t := strings.Split(tunnelIPAddress, ".")
	t1 := t[0] + "." + t[1] + "." + t[2]
	for _, v := range l {
		if strings.Contains(string(v), t1) {
			ss := strings.Split(v, " ")
			getTun = ss[0]
		}
	}
	if getTun != "" {
		log.Info("getTun: " + getTun)
		out, err := exec.Command("ovs-vsctl", "--may-exist", "add-port", "br-"+getTun, tunnelPortName).CombinedOutput()
		log.Check(log.FatalLevel, "Adding port to Open vSwitch"+string(out), err)
		out, err = exec.Command("ovs-vsctl", "set", "Interface", tunnelPortName, "type="+tunnelType, "options:key=flow", "options:remote_ip="+tunnelIPAddress).CombinedOutput()
		log.Check(log.FatalLevel, "Adding tunnel to Open vSwitch"+string(out), err)
	} else {
		log.Check(log.FatalLevel, "Create tunnel 0",
			exec.Command("ovs-vsctl", "--may-exist", "add-br", "br-"+tunnelPortName).Run())
		log.Check(log.FatalLevel, "Create tunnel 1",
			exec.Command("ovs-vsctl", "set", "bridge", "br-"+tunnelPortName, "stp_enable=true").Run())
		log.Check(log.FatalLevel, "Create tunnel 2",
			exec.Command("ovs-vsctl", "add-port", "br-"+tunnelPortName, tunnelPortName+"toint").Run())
		log.Check(log.FatalLevel, "Create tunnel 3",
			exec.Command("ovs-vsctl", "set", "Interface", tunnelPortName+"toint", "type=patch").Run())
		log.Check(log.FatalLevel, "Create tunnel 4",
			exec.Command("ovs-vsctl", "set", "Interface", tunnelPortName+"toint", "options:peer=intto"+tunnelPortName).Run())
		log.Check(log.FatalLevel, "Create tunnel 5",
			exec.Command("ovs-vsctl", "add-port", "br-int", "intto"+tunnelPortName).Run())
		log.Check(log.FatalLevel, "Create tunnel 6",
			exec.Command("ovs-vsctl", "set", "Interface", "intto"+tunnelPortName, "type=patch").Run())
		log.Check(log.FatalLevel, "Create tunnel 7",
			exec.Command("ovs-vsctl", "set", "Interface", "intto"+tunnelPortName, "options:peer="+tunnelPortName+"toint").Run())
		log.Check(log.FatalLevel, "Create tunnel 8",
			exec.Command("ovs-vsctl", "--may-exist", "add-port", "br-"+tunnelPortName, tunnelPortName).Run())
		log.Check(log.FatalLevel, "Create tunnel 9",
			exec.Command("ovs-vsctl", "set", "Interface", tunnelPortName, "type="+tunnelType,
				"options:key=flow", "options:remote_ip="+tunnelIPAddress).Run())
	}
	return nil
}

func checkBridgeName(bridgeName string) error {
	result, err := exec.Command("ovs-vsctl", "list-br").Output()
	if err != nil {
		return err
	}
	if !strings.Contains(string(result), bridgeName) {
		return errors.New(bridgeName + " is not in the list of bridges.")
	}
	return nil
}

func AddFlowConfig(bridgeName, flowCfg string) error {
	log.Check(log.FatalLevel, "check bridge name", checkBridgeName(bridgeName))
	log.Info("Bridge name is ok")
	result, errExec := exec.Command("ovs-ofctl", "add-flow", bridgeName, flowCfg).CombinedOutput()
	if errExec != nil {
		return errors.New(string(result))
	}
	return nil
}

func DumpBridge(bridgeName string) (string, error) {
	log.Check(log.FatalLevel, "check bridge name", checkBridgeName(bridgeName))
	result, err := exec.Command("ovs-ofctl", "dump-flows", bridgeName).CombinedOutput()
	if err != nil {
		return string(result), err
	}
	return string(result), nil
}

func DumpPort(bridgeName string) (string, error) {
	log.Check(log.FatalLevel, "check bridge name", checkBridgeName(bridgeName))
	result, err := exec.Command("ovs-ofctl", "show", bridgeName).CombinedOutput()
	if err != nil {
		return string(result), err
	}
	return string(result), nil
}

func DeleteFlow(bridgeName, matchCase string) error {
	log.Check(log.FatalLevel, "check bridge name", checkBridgeName(bridgeName))
	if matchCase == "all" {
		s, err := exec.Command("ovs-ofctl", "del-flows", bridgeName).CombinedOutput()
		if err != nil {
			return errors.New(string(s))
		}
	} else {
		s, err := exec.Command("ovs-ofctl", "del-flows", bridgeName, matchCase).CombinedOutput()
		if err != nil {
			return errors.New(string(s))
		}
	}
	return nil
}
