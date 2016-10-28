package cli

import (
	"bufio"
	"io/ioutil"
	"os"
	"os/exec"
	"strings"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/log"
)

// LxcHostname command changes container configs to apply a new name for the container. Used for internal SS purposes.
func LxcHostname(c, name string) {
	if !container.IsContainer(c) || container.IsTemplate(c) {
		log.Error(c + " is not an container")
		return
	}

	err := ioutil.WriteFile(config.Agent.LxcPrefix+c+"/rootfs/etc/hostname", []byte(name), 0644)
	log.Check(log.FatalLevel, "Replacing /etc/hostname for "+c, err)

	file, err := os.Open(config.Agent.LxcPrefix + c + "/rootfs/etc/hosts")
	log.Check(log.FatalLevel, "Reading /etc/hosts for "+c, err)
	scanner := bufio.NewScanner(bufio.NewReader(file))

	var hosts string
	for scanner.Scan() {
		if strings.HasPrefix(scanner.Text(), "127.0.1.1") {
			hosts = hosts + "127.0.1.1\t" + name + "\n"
		} else {
			hosts = hosts + scanner.Text() + "\n"
		}
	}
	file.Close()
	err = ioutil.WriteFile(config.Agent.LxcPrefix+c+"/rootfs/etc/hosts", []byte(hosts), 0644)
	log.Check(log.FatalLevel, "Fixing /etc/hosts for "+c, err)

	if container.State(c) == "RUNNING" {
		container.AttachExec(c, []string{"/bin/hostname", name})
	}

}

// Hostname sets the hostname of host
func Hostname(name string) {
	_, err := exec.Command("hostnamectl", "set-hostname", name).CombinedOutput()
	log.Check(log.FatalLevel, "Setting host hostname", err)
}
