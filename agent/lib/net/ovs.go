package net

import (
	"bufio"
	"bytes"
	"os/exec"
	"strconv"
	"strings"
	"subutai/log"
)

func RateLimit(nic string, rate ...string) string {
	if rate[0] != "" {
		burst, _ := strconv.Atoi(rate[0])
		burst = burst / 10

		exec.Command("ovs-vsctl", "set", "interface", nic,
			"ingress_policing_rate="+rate[0]).Run()

		exec.Command("ovs-vsctl", "set", "interface", nic,
			"ingress_policing_burst="+strconv.Itoa(burst)).Run()
	}

	out, _ := exec.Command("ovs-vsctl", "list", "interface", nic).Output()

	scanner := bufio.NewScanner(bytes.NewReader(out))
	for scanner.Scan() {
		line := strings.Fields(scanner.Text())
		if len(line) > 0 {
			if line[0] == "ingress_policing_rate:" {
				return line[1]
			}
		}
	}
	return ""
}

func UpdateNetwork(iface, vlan string) {
	log.Check(log.FatalLevel, "Setting OVS port", exec.Command("ovs-vsctl", "set", "port", iface, "tag="+vlan).Run())
}

func ConfigureOVS(iface string) {
	// check: get port name from mac address
	log.Check(log.FatalLevel, "Deleting OVS port", exec.Command("ovs-vsctl", "--if-exists", "del-port", "br-int", iface).Run())
}
