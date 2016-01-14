package lib

import (
	"io/ioutil"
	"os"
	"strings"
	"subutai/config"
	"subutai/lib/container"
	"subutai/log"
)

// LxcConfig add or delete config item from config file
func LxcConfig(contName, operation, key, value string) {
	// log.Info(contName + " " + operation + " " + key + " " + value)
	switch operation {
	case "add":
		addValue(contName, key, value)
		break
	case "del":
		delValue(contName, key, value)
		break
	case "":
		displayConfig(contName)
		break
	}
}

func displayConfig(containerName string) {
	fp, err := ioutil.ReadFile(config.Agent.LxcPrefix + containerName + "/config")
	log.Check(log.FatalLevel, "Reading config", err)
	// log.Info("Config:")
	log.Info("\n" + string(fp) + "\n")
}
func addValue(containerName, key, value string) {
	chechKeyValue(key, value)
	f := config.Agent.LxcPrefix + containerName + "/config"
	if container.GetConfigItem(f, key) == "" { // add it.

		fp, err := os.OpenFile(f, os.O_WRONLY|os.O_APPEND, 0644)
		log.Check(log.FatalLevel, "Openning config", err)

		_, err = fp.WriteString(key + " = " + value)
		log.Check(log.FatalLevel, "Writing string", err)

		log.Info(key + " added")
		fp.Close()

	} else { // replace it.
		fp, err := ioutil.ReadFile(f)
		log.Check(log.FatalLevel, "Reading config", err)

		fpLines := strings.Split(string(fp), "\n")
		for i, v := range fpLines {
			if strings.Contains(v, key) {
				fpLines[i] = key + " = " + value
			}
		}
		fpLinesSTR := strings.Join(fpLines, "\n")
		log.Check(log.FatalLevel, "Writing string", ioutil.WriteFile(f, []byte(fpLinesSTR), 0644))
		log.Info(key + " replaced")
	}
}

func delValue(containerName, key, value string) {
	if len(key) == 0 {
		log.Error("No key provided")
	}
	f := config.Agent.LxcPrefix + containerName + "/config"
	if container.GetConfigItem(f, key) == "" {
		log.Error("No such item found")
	} else {
		fp, err := ioutil.ReadFile(f)
		log.Check(log.FatalLevel, "Reading config", err)

		fpLines := strings.Split(string(fp), "\n")
		for i, v := range fpLines {
			if strings.Contains(v, key) {
				fpLines[i] = ""
			}
		}
		fpLinesSTR := strings.Join(fpLines, "\n")
		log.Check(log.FatalLevel, "Writing config", ioutil.WriteFile(f, []byte(fpLinesSTR), 0755))
		log.Info(key + " deleted")
	}
}

func chechKeyValue(key, value string) {
	if len(key) == 0 {
		log.Error("no key provided")
	}
	if len(value) == 0 {
		log.Error("no value provided")
	}
}
