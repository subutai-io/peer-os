package p2p

import (
	"bufio"
	"fmt"
	"github.com/subutai-io/Subutai/agent/config"
	"github.com/subutai-io/Subutai/agent/log"
	"io/ioutil"
	"os"
	"os/exec"
	"strings"
)

func p2pFile(line string) {
	path := config.Agent.DataPrefix + "/var/subutai-network/"
	file := path + "p2p.txt"
	if _, err := os.Stat(path); os.IsNotExist(err) {
		log.Check(log.FatalLevel, "create "+path+" folder", os.MkdirAll(path, 0755))
	}
	if _, err := os.Stat(file); os.IsNotExist(err) {
		_, err = os.Create(file)
		log.Check(log.FatalLevel, "Creating "+file, err)
	}

	f, err := os.OpenFile(file, os.O_APPEND|os.O_WRONLY, 0600)
	log.Check(log.FatalLevel, "Opening file for append "+file, err)
	defer f.Close()
	_, err = f.WriteString(line + "\n")
	log.Check(log.FatalLevel, "Opening file for append "+file, err)
}

func Create(interfaceName, localPeepIPAddr, hash, key, ttl string) {
	p2pFile(interfaceName + " " + localPeepIPAddr + " " + key + " " + ttl + " " + hash)
	log.Check(log.FatalLevel, "Creating p2p interface", exec.Command("p2p", "-start", "-key", key, "-dev", interfaceName, "-ip", localPeepIPAddr, "-ttl", ttl, "-hash", hash).Run())
}

func Print() {
	fmt.Println("Interface\tLocalPeerIP\tHash")

	file, err := os.Open(config.Agent.DataPrefix + "/var/subutai-network/p2p.txt")
	if log.Check(log.DebugLevel, "Opening p2p.txt", err) {
		return
	}
	scanner := bufio.NewScanner(bufio.NewReader(file))
	for scanner.Scan() {
		line := strings.Fields(scanner.Text())
		if len(line) > 4 {
			fmt.Println(line[0] + "\t" + line[1] + "\t" + line[4])
		}
	}
	file.Close()
}

func Remove(hash string) {
	if log.Check(log.WarnLevel, "Removing p2p interface", exec.Command("p2p", "-stop", "-hash", hash).Run()) {
		return
	}

	file, err := os.Open(config.Agent.DataPrefix + "/var/subutai-network/p2p.txt")
	if log.Check(log.WarnLevel, "Opening p2p.txt", err) {
		return
	}
	scanner := bufio.NewScanner(bufio.NewReader(file))
	newconf := ""
	for scanner.Scan() {
		line := scanner.Text()
		if !strings.HasSuffix(line, hash) {
			newconf = newconf + line + "\n"
		}
	}
	file.Close()
	log.Check(log.FatalLevel, "Removing p2p tunnel", ioutil.WriteFile(config.Agent.DataPrefix+"/var/subutai-network/p2p.txt", []byte(newconf), 0644))
}

func UpdateKey(hash, newkey, ttl string) {
	err := exec.Command("p2p", "-add-key", "-key", newkey, "-ttl", ttl, "-hash", hash).Run()
	log.Check(log.FatalLevel, "Updating p2p key: ", err)
}

func Peers(hash string) {
	out, err := exec.Command("p2p", "-show", hash).Output()
	log.Check(log.FatalLevel, "Getting list of p2p participants", err)
	fmt.Println(string(out))
}
