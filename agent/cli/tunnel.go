package lib

import (
	"bufio"
	"fmt"
	"io/ioutil"
	"net"
	"os"
	"os/exec"
	"strconv"
	"strings"
	"syscall"
	"time"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/log"
)

func Tunnel(socket, timeout, tmp string) {
	var tunsrv string
	var args []string
	// temporary workaround
	global := false
	if len(tmp) != 0 {
		global = true
	}
	if socket == "" || timeout == "" {
		log.Error("Please specify socket and timeout")
	}

	if len(strings.Split(socket, ":")) == 1 {
		socket = socket + ":22"
	}

	if global {
		cdn, err := net.LookupIP(config.Cdn.Url)
		tunsrv = cdn[0].String()
		log.Check(log.ErrorLevel, "Resolving nearest tunnel node address", err)
		args = []string{timeout, "ssh", "-i", config.Agent.AppPrefix + "etc/ssh.pem", "-N", "-p", "8022", "-R", "0:" + socket, "-o", "StrictHostKeyChecking=no", "tunnel@" + tunsrv}
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
		args = []string{timeout, "ssh", "-N", "-R", "0:" + socket, "-o", "StrictHostKeyChecking=no", "ubuntu@" + tunsrv}
	}

	cmd := exec.Command("timeout", args...)
	stderr, _ := cmd.StderrPipe()
	log.Check(log.FatalLevel, "Creating SSH tunnel to "+socket, cmd.Start())
	r := bufio.NewReader(stderr)
	line, _, err := r.ReadLine()
	log.Check(log.FatalLevel, "Reading tunnel output pipe", err)
	for i := 0; err == nil && i < 10; i++ {
		if strings.Contains(string(line), "Allocated port") {
			port := strings.Fields(string(line))
			tun := "local"
			if global {
				tun = "global"
				fmt.Println(tunsrv + ":" + port[2])
			} else {
				fmt.Println(port[2])
			}
			tout, err := strconv.Atoi(timeout)
			log.Check(log.ErrorLevel, "Converting timeout to int", err)
			tunAdd("ssh-tunnels", tunsrv+":"+port[2]+" "+socket+" "+strconv.Itoa(int(time.Now().Unix())+tout)+" "+strconv.Itoa(cmd.Process.Pid)+" "+tun)
			return
		}
		time.Sleep(1 * time.Second)
		line, _, err = r.ReadLine()
	}
	log.Error("Cannot get tunnel port")
}

func tunAdd(file, line string) {
	f, err := os.OpenFile(config.Agent.DataPrefix+"var/subutai-network/"+file, os.O_APPEND|os.O_WRONLY|os.O_CREATE, 0600)
	log.Check(log.ErrorLevel, "Writing tunnel list", err)
	defer f.Close()

	if _, err = f.WriteString(line + "\n"); err != nil {
		log.Error("Appending new line to list")
	}
}

func cleanup(pid string) {
	f, err := os.Open(config.Agent.DataPrefix + "var/subutai-network/ssh-tunnels")
	log.Check(log.WarnLevel, "Opening tunnel list", err)
	defer f.Close()

	tmp, err := os.Create(config.Agent.DataPrefix + "var/subutai-network/ssh-tunnels.new")
	log.Check(log.ErrorLevel, "Creating new list", err)
	tmp.Close()

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		row := strings.Split(scanner.Text(), " ")
		if len(row) == 5 {
			ttl, err := strconv.Atoi(row[2])
			log.Check(log.ErrorLevel, "Converting timeout to int", err)
			if ttl > int(time.Now().Unix()) && row[3] != pid {
				tunAdd("ssh-tunnels.new", scanner.Text())
			}
		}
	}
	log.Check(log.ErrorLevel, "Replacing old list",
		os.Rename(config.Agent.DataPrefix+"var/subutai-network/ssh-tunnels.new", config.Agent.DataPrefix+"var/subutai-network/ssh-tunnels"))
}

func TunList(restore bool) {
	f, err := os.Open(config.Agent.DataPrefix + "var/subutai-network/ssh-tunnels")
	log.Check(log.WarnLevel, "Opening tunnel list", err)
	defer f.Close()

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		row := strings.Split(scanner.Text(), " ")
		if len(row) == 5 {
			ttl, err := strconv.Atoi(row[2])
			log.Check(log.ErrorLevel, "Checking tunnel "+row[1]+" ttl", err)
			if ttl > int(time.Now().Unix()) {
				f, err := ioutil.ReadFile("/proc/" + row[3] + "/cmdline")
				if err == nil && strings.Contains(string(f), row[1]) && !restore {
					fmt.Println(row[0] + " " + row[1] + " " + row[2])
				} else if restore && !(err == nil && strings.Contains(string(f), row[1])) {
					if row[4] == "global" {
						Tunnel(row[1], strconv.Itoa(ttl-int(time.Now().Unix())), "-g")
					} else {
						Tunnel(row[1], strconv.Itoa(ttl-int(time.Now().Unix())), "")
					}
					cleanup(row[3])
				}
			} else {
				cleanup("")
			}
		}
	}
	log.Check(log.WarnLevel, "Scanning tunnel list", scanner.Err())
}

func TunDel(socket string) {
	f, err := os.Open(config.Agent.DataPrefix + "var/subutai-network/ssh-tunnels")
	log.Check(log.WarnLevel, "Opening tunnel list", err)
	defer f.Close()

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		row := strings.Split(scanner.Text(), " ")
		if len(row) == 5 && row[1] == socket {
			cleanup(row[3])
			pid, err := strconv.Atoi(row[3])
			log.Check(log.FatalLevel, "Converting pid to int", err)
			pgid, err := syscall.Getpgid(pid)
			log.Check(log.FatalLevel, "Getting process group id", err)
			log.Check(log.FatalLevel, "Killing tunnel process", syscall.Kill(-pgid, 15))
		}
	}
	log.Check(log.WarnLevel, "Scanning tunnel list", scanner.Err())
}
