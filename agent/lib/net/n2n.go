package net

import (
	"errors"
	"fmt"
	"io/ioutil"
	"net"
	"os"
	"os/exec"
	"strconv"
	"strings"
	"subutai/config"
	"subutai/log"
)

func CheckOrCreateEdgePortFile() {
	if _, err := os.Stat(config.Agent.DataPrefix + "/var/subutai-network/"); os.IsNotExist(err) {
		log.Check(log.FatalLevel, "create /var/subutai-network folder", os.MkdirAll(config.Agent.DataPrefix+"var/subutai-network", 0755))
	}
	if _, err := os.Stat(config.Agent.DataPrefix + "/var/subutai-network/edgePorts.txt"); os.IsNotExist(err) {
		_, err = os.Create(config.Agent.DataPrefix + "/var/subutai-network/edgePorts.txt")
		log.Check(log.FatalLevel, "Creating edgePorts.txt", err)
	}
}

func checkN2NVars(superNodeIPaddr, localPeepIPAddr, superNodePort, keyType string) error {
	superIP := net.ParseIP(superNodeIPaddr)
	if superIP == nil {
		return errors.New(superNodeIPaddr + " is not valid ip")
	}
	localIP := net.ParseIP(localPeepIPAddr)
	if localIP == nil {
		return errors.New(localPeepIPAddr + " is not valid ip")
	}
	if s, err := strconv.Atoi(superNodePort); err != nil || s > 65535 {
		return err
	}
	if keyType == "string" || keyType == "file" {
		log.Info("key type is ok")
	} else {
		return errors.New(keyType + " is not valid.")
	}
	return nil
}

func edgeCommand(superNodeIPaddr, superNodePort, interfaceName, communityName,
	localPeepIPAddr, keyType, keyFile, managementPort string) {
	log.Check(log.FatalLevel, "checkn2nvars-1: ", checkN2NVars(superNodeIPaddr, localPeepIPAddr, superNodePort, keyType))
	if keyType == "string" {
		// edge -l $supernodeIpAddress:$supernodePort -d $interfaceName -c $communityName -k $keyFile -r -a $localPeerIpAddress -t $mgmtport
		log.Check(log.FatalLevel, "edge command: ", exec.Command("edge", "-l", superNodeIPaddr+":"+superNodePort,
			"-d", interfaceName, "-c", communityName, "-k", keyFile, "-r", "-a", localPeepIPAddr,
			"-t", managementPort).Run())
		log.Info(keyType + " 1 edge command run.")
	} else if keyType == "file" {
		// check: if file exists
		if _, err := os.Stat(keyFile); os.IsNotExist(err) {
			log.Error(keyFile + " does not exist")
		}
		log.Check(log.FatalLevel, "edge command: ", exec.Command("edge", "-l", superNodeIPaddr+":"+superNodePort,
			"-d", interfaceName, "-c", communityName, "-K", keyFile, "-r", "-a", localPeepIPAddr,
			"-t", managementPort).Run())
		log.Info(keyType + " 2 edge command run.")
	}
}

func ProcessEdge(superNodeIPaddr, superNodePort, interfaceName, communityName,
	localPeepIPAddr, keyType, keyFile, managementPort string) {
	// check: if there is a record in edgePort.txt
	joinedArgs := strings.Join([]string{superNodeIPaddr, superNodePort, interfaceName, communityName, localPeepIPAddr, keyType, keyFile, managementPort}, ",")
	f, err := ioutil.ReadFile(config.Agent.DataPrefix + "/var/subutai-network/edgePorts.txt")
	log.Check(log.FatalLevel, "Reading edgePorts.txt", err)
	found := false

	for _, v := range strings.Split(string(f), "\n") {
		if string(v) == joinedArgs {
			found = true
			break
		}
	}
	if found == true {
		// log.Info("found record in edgePorts.txt")
		// check: if there is a alive process.... seems like there is no package to find process with name
		if ps, err := exec.Command("ps", "-ax").CombinedOutput(); err != nil {
			log.Error("ps run: " + err.Error())
		} else {
			for _, v := range strings.Split(string(ps), "\n") {
				if strings.Contains(string(v), interfaceName) ||
					strings.Contains(string(v), communityName) ||
					strings.Contains(string(v), localPeepIPAddr) {
					log.Error("edge PROCESS with given options exists already")
				}
			}
		} // no process found... good.
		log.Info("exists in file but no process found")
		edgeCommand(superNodeIPaddr, superNodePort, interfaceName, communityName, localPeepIPAddr, keyType, keyFile, managementPort)
	} else {
		log.Info("NOT found any record in edgePorts.txt")
		log.Check(log.FatalLevel, "checkn2nvars-2: ", checkN2NVars(superNodeIPaddr, localPeepIPAddr, superNodePort, keyType))
		edgeCommand(superNodeIPaddr, superNodePort, interfaceName, communityName, localPeepIPAddr, keyType, keyFile, managementPort)
		// check: add values to edgePorts.txt
		f, err := os.OpenFile(config.Agent.DataPrefix+"/var/subutai-network/edgePorts.txt", os.O_APPEND|os.O_WRONLY, 0600)
		log.Check(log.FatalLevel, "Opening edgePorts.txt", err)
		if _, err = f.WriteString(joinedArgs + "\n"); err != nil {
			log.Error("append to edgePorts.txt" + err.Error())
		}
		defer f.Close()
	}

	// log.Info("New Com device is created: " + interfaceName)
	// log.Info("New CommunityName is created: " + communityName)
}

func PrintN2NTunnels() {
	if p, err := exec.Command("ps", "ax").CombinedOutput(); err != nil {
		log.Error("PrintN2NTunnels " + err.Error())
	} else {
		// log.Info("ACTIVE process")
		fmt.Printf("%0s %20s %20s", "LocalPeerIP", "LocalInterface", "Community")
		fmt.Println()
		for _, v := range strings.Split(string(p), "\n") {
			if strings.Contains(string(v), "p2p") {
				vArr := strings.Fields(string(v))
				// fmt.Println(len(vArr))
				fmt.Printf("%0s %20s %20s", vArr[8], vArr[6], vArr[10])
				// fmt.Printf("%0s %20s %20s %20s %20s", vArr[15], vArr[6], vArr[17], vArr[8], vArr[10])
				fmt.Println()

			}
		}
	}
	// log.Info("end of list")
}

func ReturnPID(interfaceName, communityName string) string {
	var retVal string
	if p, err := exec.Command("ps", "aux").CombinedOutput(); err != nil {
		log.Error("ReturnPID " + err.Error())
	} else {
	CutTheLoop:
		for _, v := range strings.Split(string(p), "\n") {
			if strings.Contains(string(v), interfaceName) && strings.Contains(string(v), communityName) {
				vArr := strings.Fields(string(v))
				retVal = vArr[1]
				break CutTheLoop
			}
		}
	}
	return retVal
}

func ReloadN2N(interfaceName, communityName string) {
	var port string
	if f, err := ioutil.ReadFile(config.Agent.DataPrefix + "/var/subutai-network/edgePorts.txt"); err != nil {
		log.Error("ReloadN2N read edgePorts " + err.Error())
	} else {
	CutTheLoop:
		for _, v := range strings.Split(string(f), "\n") {
			if strings.Contains(v, interfaceName+","+communityName) {
				p := strings.Split(v, ",")
				port = p[7]
				break CutTheLoop
			}
		}
	}
	if port == "" {
		log.Error(interfaceName + " and " + communityName + " does not have a management port")
	}
	log.Check(log.FatalLevel, "run nc ", exec.Command("nc", "-w1", "-4u", "localhost", port).Run())
}
