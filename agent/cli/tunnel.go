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
	ovs "github.com/subutai-io/base/agent/lib/net"
	"github.com/subutai-io/base/agent/log"
)

// The tunnel feature is based on SSH tunnels and works in combination with Subutai Helpers and serves as an easy solution for bypassing NATs.
// In Subutai, tunnels are used to access the SS management server's web UI from the Hub, and open direct connection to containers, etc.
// There are two types of channels - local (default), which is created from destination address to host and global (-g flag), from destination to Subutai Helper node.
// Tunnels may also be set to be permanent (default) or temporary (ttl in seconds). The default destination port is 22.
// Subutai tunnels have a continuous state checking mechanism which keeps opened tunnels alive and closes outdated tunnels to keep the system network connections clean.
// This mechanism may re-create a tunnel if it was dropped unintentionally (system reboot, network interruption, etc.), but newly created tunnels will have different "entrance" address.

// TunAdd adds tunnel to specified network socket
func TunAdd(socket, timeout string, global bool) {
	if len(socket) == 0 {
		log.Error("Please specify socket")
	}

	if len(strings.Split(socket, ":")) == 1 {
		socket = socket + ":22"
	}

	args, tunsrv := getArgs(global, socket)
	cmd := exec.Command("ssh", args...)
	if len(timeout) > 0 {
		args = append([]string{timeout, "ssh"}, args...)
		cmd = exec.Command("timeout", args...)
	}

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
			}
			fmt.Println(tunsrv + ":" + port[2])
			if len(timeout) > 0 {
				tout, err := strconv.Atoi(timeout)
				log.Check(log.ErrorLevel, "Converting timeout to int", err)
				addToList("ssh-tunnels", tunsrv+":"+port[2]+" "+socket+" "+strconv.Itoa(int(time.Now().Unix())+tout)+" "+strconv.Itoa(cmd.Process.Pid)+" "+tun)
			} else {
				addToList("ssh-tunnels", tunsrv+":"+port[2]+" "+socket+" -1 "+strconv.Itoa(cmd.Process.Pid)+" "+tun)
			}
			return
		}
		time.Sleep(1 * time.Second)
		line, _, err = r.ReadLine()
	}
	log.Error("Cannot get tunnel port")
}

// TunList performs tunnel check and shows "alive" tunnels
func TunList() {
	TunCheck()
	f := getList()
	defer f.Close()
	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		row := strings.Split(scanner.Text(), " ")
		if len(row) == 5 {
			fmt.Println(row[0] + " " + row[1] + " " + row[2])
		}
	}
	log.Check(log.WarnLevel, "Scanning tunnel list", scanner.Err())
}

// TunDel removes tunnel entry from list and kills running tunnel process
func TunDel(socket string, pid ...string) {
	f := getList()
	defer f.Close()

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		row := strings.Split(scanner.Text(), " ")
		if len(row) == 5 && row[1] == socket && (len(pid) == 0 || (len(pid[0]) != 0 && row[3] == pid[0])) {
			delEntry(row[3])
			f, err := ioutil.ReadFile("/proc/" + row[3] + "/cmdline")
			if err == nil && strings.Contains(string(f), row[1]) {
				pid, err := strconv.Atoi(row[3])
				log.Check(log.FatalLevel, "Converting pid to int", err)
				pgid, err := syscall.Getpgid(pid)
				log.Check(log.FatalLevel, "Getting process group id", err)
				log.Check(log.FatalLevel, "Killing tunnel process", syscall.Kill(-pgid, 15))
			}
		}
	}
	log.Check(log.WarnLevel, "Scanning tunnel list", scanner.Err())
}

// TunCheck reads list, checks tunnel ttl, its state and then adds or removes required tunnels
func TunCheck() {
	f := getList()
	defer f.Close()
	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		row := strings.Split(scanner.Text(), " ")
		if len(row) == 5 {
			ttl, err := strconv.Atoi(row[2])
			log.Check(log.ErrorLevel, "Checking tunnel "+row[1]+" ttl", err)
			if ttl <= int(time.Now().Unix()) && ttl != -1 {
				TunDel(row[1], row[3])
			} else if !tunOpen(row[0], row[1]) {
				TunDel(row[1], row[3])
				newttl := ""
				if ttl-int(time.Now().Unix()) > 0 {
					newttl = strconv.Itoa(ttl - int(time.Now().Unix()))
				}
				if row[4] == "global" {
					TunAdd(row[1], newttl, true)
				} else {
					TunAdd(row[1], newttl, false)
				}
			}
		}
	}
	log.Check(log.WarnLevel, "Scanning tunnel list", scanner.Err())
}

// getList returns file with tunnels list
func getList() *os.File {
	f, err := os.Open(config.Agent.DataPrefix + "var/subutai-network/ssh-tunnels")
	if os.IsNotExist(err) {
		f, err = os.Create(config.Agent.DataPrefix + "var/subutai-network/ssh-tunnels")
		log.Check(log.FatalLevel, "Creating tunnel list", err)
	} else if err != nil {
		log.Error("Opening tunnel list " + err.Error())
	}
	return f
}

// getArgs builds command line to execute in system
func getArgs(global bool, socket string) ([]string, string) {
	var tunsrv string
	var args []string
	if global {
		cdn, err := net.LookupIP(config.CDN.URL)
		log.Check(log.ErrorLevel, "Resolving nearest tunnel node address", err)
		tunsrv = cdn[0].String()
		args = []string{"-i", config.Agent.AppPrefix + "etc/ssh.pem", "-N", "-p", "8022", "-R", "0:" + socket, "-o", "StrictHostKeyChecking=no", "tunnel@" + tunsrv}
	} else {
		tunsrv = ovs.GetIp()
		args = []string{"-N", "-R", "0:" + socket, "-o", "StrictHostKeyChecking=no", "ubuntu@" + tunsrv}
	}
	return args, tunsrv
}

// addToList appends new tunnel entry to tunnels list
func addToList(file, line string) {
	f, err := os.OpenFile(config.Agent.DataPrefix+"var/subutai-network/"+file, os.O_APPEND|os.O_WRONLY|os.O_CREATE, 0600)
	log.Check(log.ErrorLevel, "Writing tunnel list", err)
	defer f.Close()

	if _, err = f.WriteString(line + "\n"); err != nil {
		log.Error("Appending new line to list")
	}
}

// delEntry removes tunnel entry from list in file
func delEntry(pid string) {
	f := getList()
	defer f.Close()

	tmp, err := os.Create(config.Agent.DataPrefix + "var/subutai-network/ssh-tunnels.new")
	log.Check(log.ErrorLevel, "Creating new list", err)
	tmp.Close()

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		row := strings.Split(scanner.Text(), " ")
		if len(row) == 5 && row[3] != pid {
			addToList("ssh-tunnels.new", scanner.Text())
		}
	}
	log.Check(log.ErrorLevel, "Replacing old list",
		os.Rename(config.Agent.DataPrefix+"var/subutai-network/ssh-tunnels.new", config.Agent.DataPrefix+"var/subutai-network/ssh-tunnels"))
}

// tunOpen checks tunnel sockets state to define if tunnel is alive
func tunOpen(remote, local string) bool {
	if _, err := net.DialTimeout("tcp", local, time.Second*1); err != nil {
		log.Debug("Local socket connectivity problem")
		return true
	} else if _, err := net.DialTimeout("tcp", remote, time.Second*2); err != nil {
		log.Debug("Remote socket connectivity problem")
		return false
	}
	return true
}
