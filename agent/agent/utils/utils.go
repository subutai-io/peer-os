package utils

import (
	"bufio"
	"bytes"
	"io/ioutil"
	"net"
	"strings"
	"subutai/config"
	"subutai/log"
	"time"
)

type Iface struct {
	InterfaceName string `json:"interfaceName"`
	Ip            string `json:"ip"`
	Mac           string `json:"mac"`
}

func GetInterfaces() []Iface {
	n_ifaces, err := net.Interfaces()
	log.Check(log.WarnLevel, "Getting network interfaces", err)

	l_ifaces := []Iface{}
	for _, ifac := range n_ifaces {
		if ifac.Name == "lo0" || ifac.Name == "lo" {
			continue
		}
		inter := new(Iface)
		inter.InterfaceName = ifac.Name
		inter.Mac = ifac.HardwareAddr.String()

		addrs, err := ifac.Addrs()
		log.Check(log.WarnLevel, "Getting network addresses", err)
		var ip net.IP
		var ipv4 string
		for _, addr := range addrs {
			switch v := addr.(type) {
			case *net.IPNet:
				ip = v.IP
				ipv4 = ip.To4().String()
				if ipv4 != "<nil>" {
					inter.Ip = ipv4
					l_ifaces = append(l_ifaces, *inter)
				}
			case *net.IPAddr:
				ip = v.IP
			}
		}

	}
	return l_ifaces
}

func SendIntermediateChunks(scanner *bufio.Scanner, timeout, c_size int, ch chan<- []byte) {
	var buffer bytes.Buffer
	end_time := time.Now().Add(time.Duration(timeout) * time.Second)
	for scanner.Scan() {
		buffer.WriteString(scanner.Text() + "\n")
		if buffer.Len() >= c_size {
			ch <- buffer.Bytes()
		} else if end_time.Unix()-time.Now().Unix() <= 0 {
			ch <- buffer.Bytes()
			close(ch)
		}
	}
	close(ch)
}

func PublicCert() string {
	pemCerts, err := ioutil.ReadFile(config.Agent.DataPrefix + "ssl/cert.pem")
	if log.Check(log.WarnLevel, "Checking cert.pem file", err) {
		return ""
	}
	return string(pemCerts)
}

func InstanceType() string {
	uuid, err := ioutil.ReadFile("/sys/hypervisor/uuid")
	if !log.Check(log.DebugLevel, "Checking if AWS ec2 by reading /sys/hypervisor/uuid", err) {
		if strings.HasPrefix(string(uuid), "ec2") {
			return "EC2"
		}
	}
	return "LOCAL"
}
