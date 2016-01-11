package lib

import (
	"bufio"
	"fmt"
	"os/exec"
	"strings"
	"subutai/log"
)

func SshTunnel(remoteip, timeout string) {
	if remoteip == "" || timeout == "" {
		log.Error("Please specify container IP and timeout")
	}
	cmd := exec.Command("ssh", "-f", "-R", "0:"+remoteip+":22", "-o", "StrictHostKeyChecking=no", "ubuntu@localhost", "sleep", timeout)
	stderr, _ := cmd.StderrPipe()
	log.Check(log.FatalLevel, "Creating SSH tunnel to "+remoteip, cmd.Start())
	r := bufio.NewReader(stderr)
	i := 0
	for line, _, err := r.ReadLine(); err == nil && i < 20; i++ {
		log.Check(log.FatalLevel, "Reading SSH pipe", err)
		if strings.Contains(string(line), "Allocated port") {
			port := strings.Fields(string(line))
			fmt.Println(port[2])
			return
		}
		line, _, err = r.ReadLine()
	}
	log.Error("Cannot parse tunnel port")
}
