package p2p

import (
	"bufio"
	"bytes"
	"fmt"
	"net"
	"os/exec"
	"strings"

	"github.com/subutai-io/base/agent/log"
)

// Create adds new P2P interface to the Resource Host. This interface connected to the swarm.
func Create(interfaceName, localPeepIPAddr, hash, key, ttl, portRange string) {
	cmd := []string{"start", "-key", key, "-dev", interfaceName, "-ttl", ttl, "-hash", hash}
	if localPeepIPAddr != "dhcp" {
		cmd = append(cmd, "-ip", localPeepIPAddr)
	}
	if len(portRange) > 2 {
		cmd = append(cmd, "-ports", localPeepIPAddr)
	}
	log.Check(log.FatalLevel, "Creating p2p interface", exec.Command("p2p", cmd...).Run())
}

// Remove deletes P2P interface from the Resource Host.
func Remove(hash string) {
	log.Check(log.WarnLevel, "Removing p2p interface", exec.Command("p2p", "stop", "-hash", hash).Run())
}

// RemoveByIface deletes P2P interface from the Resource Host.
func RemoveByIface(name string) {
	mac := ""
	interfaces, _ := net.Interfaces()
	for _, iface := range interfaces {
		if iface.Name == name {
			mac = iface.HardwareAddr.String()
		}
	}
	out, _ := exec.Command("p2p", "show").Output()
	scanner := bufio.NewScanner(bytes.NewReader(out))
	for scanner.Scan() {
		line := strings.Fields(scanner.Text())
		if len(line) > 1 && line[0] == mac {
			Remove(line[2])
		}
	}
	iptablesCleanUp(name)
}

func iptablesCleanUp(name string) {
	out, _ := exec.Command("iptables-save").Output()
	scanner := bufio.NewScanner(bytes.NewReader(out))
	for scanner.Scan() {
		line := scanner.Text()
		if strings.Contains(line, name) {
			args := strings.Fields(line)
			args[0] = "-D"
			exec.Command("iptables", append([]string{"-t", "nat"}, args...)...).Run()
		}
	}
}

// UpdateKey sets new encryption key for the P2P instance to replace it during work.
func UpdateKey(hash, newkey, ttl string) {
	err := exec.Command("p2p", "set", "-key", newkey, "-ttl", ttl, "-hash", hash).Run()
	log.Check(log.FatalLevel, "Updating p2p key", err)
}

// Version returns version of the P2P on the Resource Host.
func Version() {
	out, err := exec.Command("p2p", "version").CombinedOutput()
	fmt.Printf("%s", out)
	log.Check(log.FatalLevel, "Getting p2p version", err)
}

// Peers prints list of the participants of the swarm.
func Peers(hash string) {
	args := []string{"show", "-hash", hash}
	if hash == "" {
		args = []string{"show"}
	}
	out, err := exec.Command("p2p", args...).Output()
	log.Check(log.FatalLevel, "Getting list of p2p participants", err)
	fmt.Println(string(out))
}
