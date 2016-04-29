package lib

import (
	"bufio"
	"fmt"
	"net"
	"os"
	"os/exec"
	"strconv"
	"strings"
	"time"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/log"
)

func Tunnel(dst, timeout, dstport, tmp string) {
	// temporary workaround
	global := false
	if len(tmp) != 0 {
		global = true
	}
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
		args = []string{timeout, "ssh", "-i", config.Agent.AppPrefix + "etc/ssh.pem", "-N", "-p", "8022", "-R", "0:" + dst + ":" + dstport, "-o", "StrictHostKeyChecking=no", "tunnel@" + tunsrv}
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
		args = []string{timeout, "ssh", "-N", "-R", "0:" + dst + ":" + dstport, "-o", "StrictHostKeyChecking=no", "ubuntu@" + tunsrv}
	}

	cmd := exec.Command("timeout", args...)
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
			tout, err := strconv.Atoi(timeout)
			log.Check(log.ErrorLevel, "Converting timeout to int", err)
			tunAdd("ssh-tunnels", tunsrv+":"+port[2]+" "+dst+":"+dstport+" "+strconv.Itoa(int(time.Now().Unix())+tout)+" "+strconv.Itoa(cmd.Process.Pid))
			return
		}
	}
	log.Error("Cannot get tunnel port")
}

func tunAdd(file, line string) {
	f, err := os.OpenFile(config.Agent.DataPrefix+"var/subutai-network/"+file, os.O_APPEND|os.O_WRONLY|os.O_CREATE, 0600)
	log.Check(log.ErrorLevel, "Writing tunnels list", err)
	defer f.Close()

	if _, err = f.WriteString(line + "\n"); err != nil {
		log.Error("Appending new line to list")
	}
}

func cleanup() {
	f, err := os.Open(config.Agent.DataPrefix + "var/subutai-network/ssh-tunnels")
	log.Check(log.WarnLevel, "Opening tunnels list", err)
	defer f.Close()

	tmp, err := os.Create(config.Agent.DataPrefix + "var/subutai-network/ssh-tunnels.new")
	log.Check(log.ErrorLevel, "Creating new list", err)
	tmp.Close()

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		row := strings.Split(scanner.Text(), " ")
		if len(row) == 4 {
			ttl, err := strconv.Atoi(row[2])
			log.Check(log.ErrorLevel, "Converting timeout to int", err)
			if ttl > int(time.Now().Unix()) {
				tunAdd("ssh-tunnels.new", scanner.Text())
			}
		}
	}
	log.Check(log.ErrorLevel, "Replacing old list",
		os.Rename(config.Agent.DataPrefix+"var/subutai-network/ssh-tunnels.new", config.Agent.DataPrefix+"var/subutai-network/ssh-tunnels"))
}

func TunList() {
	f, err := os.Open(config.Agent.DataPrefix + "var/subutai-network/ssh-tunnels")
	log.Check(log.WarnLevel, "Opening tunnels list", err)
	defer f.Close()

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		row := strings.Split(scanner.Text(), " ")
		if len(row) == 4 {
			ttl, err := strconv.Atoi(row[2])
			log.Check(log.ErrorLevel, "Converting timeout to int", err)
			if ttl > int(time.Now().Unix()) {
				f, err := os.Open("/proc/" + row[3] + "/cmdline")
				defer f.Close()
				if err == nil {
					scanner := bufio.NewScanner(f)
					for scanner.Scan() {
						if strings.Contains(scanner.Text(), row[1]) {
							fmt.Println(row[0] + " " + row[1])
						}
					}
				} else {
					log.Debug("Dead tunnel: " + row[0] + " " + row[1])
				}
				log.Check(log.ErrorLevel, "Reading proc info", scanner.Err())
			} else {
				cleanup()
			}
		}
	}
	log.Check(log.WarnLevel, "Scanning packages list", scanner.Err())
}
