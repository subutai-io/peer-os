package p2p

import (
	"fmt"
	"github.com/subutai-io/base/agent/log"
	"os/exec"
)

func Create(interfaceName, localPeepIPAddr, hash, key, ttl string) {
	if localPeepIPAddr == "dhcp" {
		log.Check(log.FatalLevel, "Creating p2p interface", exec.Command("p2p", "start", "-key", key, "-dev", interfaceName, "-ttl", ttl, "-hash", hash).Run())
	} else {
		log.Check(log.FatalLevel, "Creating p2p interface", exec.Command("p2p", "start", "-key", key, "-dev", interfaceName, "-ip", localPeepIPAddr, "-ttl", ttl, "-hash", hash).Run())
	}
}

func Remove(hash string) {
	log.Check(log.WarnLevel, "Removing p2p interface", exec.Command("p2p", "stop", "-hash", hash).Run())
}

func UpdateKey(hash, newkey, ttl string) {
	err := exec.Command("p2p", "set", "-key", newkey, "-ttl", ttl, "-hash", hash).Run()
	log.Check(log.FatalLevel, "Updating p2p key: ", err)
}

func Peers(hash string) {
	out, err := exec.Command("p2p", "show", hash).Output()
	log.Check(log.FatalLevel, "Getting list of p2p participants", err)
	fmt.Println(string(out))
}
