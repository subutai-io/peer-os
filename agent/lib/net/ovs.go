package net

import (
	"bufio"
	"bytes"
	"net"
	"os/exec"
	"strconv"
	"strings"

	"github.com/subutai-io/base/agent/log"
)

// RateLimit sets throughput limits for container's network interfaces if "quota" is specified
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

// GetIp returns IP address that should be used for host access
func GetIp() string {
	out, err := exec.Command("ovs-vsctl", "list-ports", "wan").Output()
	log.Check(log.ErrorLevel, "Getting WAN ports", err)

	scanner := bufio.NewScanner(bytes.NewReader(out))
	iface := "wan"
	for scanner.Scan() {
		if scanner.Text() == "eth1" {
			iface = "eth2"
			break
		}
	}

	if nic, err := net.InterfaceByName(iface); err == nil {
		addrs, err := nic.Addrs()
		log.Check(log.ErrorLevel, "Getting interface addresses", err)
		if len(addrs) > 0 {
			if ipnet, ok := addrs[0].(*net.IPNet); ok {
				if ipnet.IP.To4() != nil {
					return ipnet.IP.String()
				}
			}
		}
	}
	return "null"
}
