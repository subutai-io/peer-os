package container

import (
	"bufio"
	"io/ioutil"
	"os"
	"strings"
	"subutai/config"
	"subutai/log"
)

func SetContainerConf(container string, conf [][]string) {
	for _, cc := range conf {
		log.Debug(cc[0] + " : " + cc[1])
	}
	confPath := config.Agent.LxcPrefix + container + "/config"
	newconf := ""

	file, err := os.Open(confPath)
	log.Check(log.FatalLevel, "Opening container config "+confPath, err)
	scanner := bufio.NewScanner(bufio.NewReader(file))

	for scanner.Scan() {
		newline := scanner.Text() + "\n"
		for i := 0; i < len(conf); i++ {
			line := strings.Split(scanner.Text(), "=")
			if len(line) > 1 && strings.Trim(line[0], " ") == conf[i][0] {
				if newline = ""; len(conf[i][1]) > 0 {
					newline = conf[i][0] + " = " + conf[i][1] + "\n"
				}
				conf = append(conf[:i], conf[i+1:]...)
				break
			}
		}
		newconf = newconf + newline
	}
	file.Close()

	for i := range conf {
		if conf[i][1] != "" {
			if conf[i][0] == "lxc.network.veth.pair" {
				conf[i][1] = strings.Replace(GetConfigItem(confPath, "lxc.network.hwaddr"), ":", "", -1)
			}
			newconf = newconf + conf[i][0] + " = " + conf[i][1] + "\n"
		}
	}

	log.Check(log.FatalLevel, "Writing container config "+confPath, ioutil.WriteFile(confPath, []byte(newconf), 0644))
}

func GetConfigItem(path, item string) string {
	config, _ := os.Open(path)
	defer config.Close()
	scanner := bufio.NewScanner(config)
	for scanner.Scan() {
		line := strings.Split(scanner.Text(), "=")
		if strings.Trim(line[0], " ") == item {
			return strings.Trim(line[1], " ")
		}
	}
	return ""
}
