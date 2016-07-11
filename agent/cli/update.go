package lib

import (
	"crypto/tls"
	"encoding/json"
	"io"
	"io/ioutil"
	"net/http"
	"os"
	"os/exec"
	"strconv"
	"strings"

	"github.com/cheggaaa/pb"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/log"
)

type snap struct {
	Version string `json:"version"`
	Hash    string `json:"md5Sum"`
}

// func getAvailable(name string) string {
// 	var update snap
// 	tr := &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true}}
// 	client := &http.Client{Transport: tr}
// 	if !config.Cdn.Allowinsecure {
// 		client = &http.Client{}
// 	}
// 	resp, err := client.Get("https://" + config.Cdn.Url + ":" + config.Cdn.Sslport + "/kurjun/rest/file/info?name=" + name)
// 	log.Check(log.FatalLevel, "GET: https://"+config.Cdn.Url+":"+config.Cdn.Sslport+"/kurjun/rest/file/info?name="+name, err)
// 	defer resp.Body.Close()
// 	js, err := ioutil.ReadAll(resp.Body)
// 	log.Check(log.FatalLevel, "Reading response", err)
// 	log.Check(log.FatalLevel, "Parsing file list", json.Unmarshal(js, &update))
// 	log.Debug("Available: " + update.Version)
// 	return update.Version
// }

//Temporary function until gorjun cache will be fixed
func ifUpdateable(installed int) string {
	var update snap
	tr := &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true}}
	client := &http.Client{Transport: tr}
	if !config.Cdn.Allowinsecure {
		client = &http.Client{}
	}
	packet := "subutai_" + config.Template.Version + "_" + config.Template.Arch + ".snap"
	if len(config.Template.Branch) != 0 {
		packet = "subutai_" + config.Template.Version + "_" + config.Template.Arch + "-" + config.Template.Branch + ".snap"
	}
	resp, err := client.Get("https://" + config.Cdn.Url + ":" + config.Cdn.Sslport + "/kurjun/rest/file/info?name=" + packet)
	log.Check(log.FatalLevel, "GET: https://"+config.Cdn.Url+":"+config.Cdn.Sslport+"/kurjun/rest/file/info?name="+packet, err)
	defer resp.Body.Close()
	js, err := ioutil.ReadAll(resp.Body)
	log.Check(log.FatalLevel, "Reading response", err)
	log.Check(log.FatalLevel, "Parsing file list", json.Unmarshal(js, &update))
	log.Debug("Available: " + update.Version)
	available, err := strconv.Atoi(update.Version)
	log.Check(log.ErrorLevel, "Converting available package timestamp to int", err)
	if installed >= available {
		return ""
	} else {
		return update.Hash
	}

}

func getInstalled() string {
	f, err := ioutil.ReadFile(config.Agent.AppPrefix + "/meta/package.yaml")
	if !log.Check(log.DebugLevel, "Reading file package.yaml", err) {
		lines := strings.Split(string(f), "\n")
		for _, v := range lines {
			if strings.HasPrefix(v, "version: ") {
				if version := strings.Split(strings.TrimPrefix(v, "version: "), "-"); len(version) > 1 {
					log.Debug("Installed: " + version[1])
					return version[1]
				}
			}
		}
	}
	return "0"
}

func upgradeRh(packet string) {
	log.Info("Updating Resource host")
	file, err := os.Create("/tmp/" + packet)
	log.Check(log.FatalLevel, "Creating update file", err)
	defer file.Close()
	tr := &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true}}
	client := &http.Client{Transport: tr}
	if !config.Cdn.Allowinsecure {
		client = &http.Client{}
	}
	resp, err := client.Get("https://" + config.Cdn.Url + ":" + config.Cdn.Sslport + "/kurjun/rest/file/get?id=" + packet)
	log.Check(log.FatalLevel, "GET: https://"+config.Cdn.Url+":"+config.Cdn.Sslport+"/kurjun/rest/file/get?id="+packet, err)
	defer resp.Body.Close()
	log.Info("Downloading snap package")
	bar := pb.New(int(resp.ContentLength)).SetUnits(pb.U_BYTES)
	bar.Start()
	rd := bar.NewProxyReader(resp.Body)

	_, err = io.Copy(file, rd)
	log.Check(log.FatalLevel, "Writing response to file", err)

	log.Check(log.FatalLevel, "Installing update /tmp/"+packet,
		exec.Command("snappy", "install", "--allow-unauthenticated", "/tmp/"+packet).Run())
	log.Check(log.FatalLevel, "Removing update file /tmp/"+packet, os.Remove("/tmp/"+packet))

}

func Update(name string, check bool) {
	if !lockSubutai(name + ".update") {
		log.Error("Another update process is already running")
	}
	defer unlockSubutai()
	switch name {
	case "rh":
		// packet := "subutai_" + config.Template.Version + "_" + config.Template.Arch + ".snap"
		// if len(config.Template.Branch) != 0 {
		// 	packet = "subutai_" + config.Template.Version + "_" + config.Template.Arch + "-" + config.Template.Branch + ".snap"
		// }

		installed, err := strconv.Atoi(getInstalled())
		log.Check(log.FatalLevel, "Converting installed package timestamp to int", err)
		// available, err := strconv.Atoi(getAvailable(packet))
		// log.Check(log.FatalLevel, "Converting available package timestamp to int", err)

		//Temporary workaround until gorjun cache will be fixed
		newsnap := ifUpdateable(installed)
		if len(newsnap) == 0 {
			log.Info("No update is available")
			os.Exit(1)
		} else if check {
			log.Info("Update is available")
			os.Exit(0)
		}
		upgradeRh(newsnap)

		// if installed >= available {
		// 	log.Info("No update is available")
		// 	os.Exit(1)
		// } else if check {
		// 	log.Info("Update is available")
		// 	os.Exit(0)
		// }

		// upgradeRh(packet)

	default:
		if !container.IsContainer(name) {
			log.Error("no such instance \"" + name + "\"")
		}
		_, err := container.AttachExec(name, []string{"apt-get", "-qq", "update", "-y", "--force-yes", "-o", "Acquire::http::Timeout=5"})
		log.Check(log.FatalLevel, "Updating apt index", err)
		output, err := container.AttachExec(name, []string{"apt-get", "-qq", "upgrade", "-y", "--force-yes", "-o", "Acquire::http::Timeout=5", "-s"})
		log.Check(log.FatalLevel, "Checking for available update", err)
		if len(output) == 0 {
			log.Info("No update is available")
			os.Exit(1)
		} else if check {
			log.Info("Update is available")
			os.Exit(0)
		}
		_, err = container.AttachExec(name, []string{"apt-get", "-qq", "upgrade", "-y", "--force-yes", "-o", "Acquire::http::Timeout=5"})
		log.Check(log.FatalLevel, "Updating container", err)
	}
}
