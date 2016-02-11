package net

import (
	"errors"
	"github.com/subutai-io/Subutai/agent/log"
	"net"
	"os/exec"
	"strings"
)

func IfTunExist(name string) {
	ret, err := exec.Command("ovs-vsctl", "list-ports", "wan").CombinedOutput()
	log.Check(log.FatalLevel, "Getting port list", err)
	ports := strings.Split(string(ret), "\n")

	for _, port := range ports {
		if port == name {
			log.Error("Tunnel port " + name + " is already exists")
		}
	}
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
