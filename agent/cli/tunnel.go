package lib

import (
	"bufio"
	"fmt"
	"net"
	"os/exec"
	"strings"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/log"
)

func Tunnel(dst, timeout, dstport string, global bool) {
	var tunsrv string
	var args []string
	if dst == "" || timeout == "" {
		log.Error("Please specify destination IP and timeout")
	}

	if len(dstport) == 0 {
		dstport = "22"
	}

	if global {
		cdn, err := net.LookupIP(config.Cdn.Url)
		tunsrv = cdn[0].String()
		log.Check(log.ErrorLevel, "Resolving nearest tunnel node address", err)
		args = []string{"-i", config.Agent.AppPrefix + "etc/ssh.pem", "-N", "-p", "8022", "-f", "-R", "0:" + dst + ":" + dstport, "-o", "StrictHostKeyChecking=no", "tunnel@" + tunsrv, "sleep", timeout}
	} else {
		wan, err := net.InterfaceByName("wan")
		log.Check(log.ErrorLevel, "Getting WAN interface info", err)
		wanIP, err := wan.Addrs()
		log.Check(log.ErrorLevel, "Getting WAN interface addresses", err)
		if len(wanIP) > 0 {
			ip := strings.Split(wanIP[0].String(), "/")
			if len(ip) > 0 {
				tunsrv = ip[0]
			}
		}
		args = []string{"-N", "-f", "-R", "0:" + dst + ":" + dstport, "-o", "StrictHostKeyChecking=no", "ubuntu@" + tunsrv, "sleep", timeout}
	}

	cmd := exec.Command("ssh", args...)
	stderr, _ := cmd.StderrPipe()
	log.Check(log.FatalLevel, "Creating SSH tunnel to "+dst, cmd.Start())
	r := bufio.NewReader(stderr)
	i := 0
	for line, _, err := r.ReadLine(); err == nil && i < 20; i++ {
		log.Check(log.FatalLevel, "Reading SSH pipe", err)
		if strings.Contains(string(line), "Allocated port") {
			port := strings.Fields(string(line))
			if global {
				fmt.Println(tunsrv + ":" + port[2])
			} else {
				fmt.Println(port[2])
			}
			return
		}
		line, _, err = r.ReadLine()
	}
	log.Error("Cannot get tunnel port")
}
