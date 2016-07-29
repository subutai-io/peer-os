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
	Md5Sum  string   `json:"md5Sum"`
	Name    string   `json:"name"`
	Owner   []string `json:"owner"`
	Version string   `json:"version"`
}

func ifUpdateable(installed int) string {
	var update []snap

	tr := &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true}}
	client := &http.Client{Transport: tr}
	if !config.Cdn.Allowinsecure {
		client = &http.Client{}
	}
	resp, err := client.Get("https://" + config.Cdn.Url + ":" + config.Cdn.Sslport + "/kurjun/rest/file/info?name=subutai_")
	log.Check(log.FatalLevel, "GET: https://"+config.Cdn.Url+":"+config.Cdn.Sslport+"/kurjun/rest/file/info?name=subutai_", err)
	defer resp.Body.Close()
	js, err := ioutil.ReadAll(resp.Body)
	log.Check(log.FatalLevel, "Reading response", err)
	log.Check(log.FatalLevel, "Parsing file list", json.Unmarshal(js, &update))

	hash := ""
	for _, v := range update {
		available, err := strconv.Atoi(v.Version)
		log.Check(log.ErrorLevel, "Matching update, "+v.Version, err)
		if strings.HasSuffix(v.Name, ".snap") && installed < available && ourCI(v.Owner) && strings.Contains(v.Name, config.Template.Arch) {
			if len(config.Template.Branch) != 0 && strings.Contains(v.Name, config.Template.Branch) {
				log.Debug("Found newer snap: " + v.Name + ", " + v.Version)
				installed = available
				hash = v.Md5Sum
			} else if len(config.Template.Branch) == 0 {
				log.Debug("Found newer snap: " + v.Name + ", " + v.Version)
				installed = available
				hash = v.Md5Sum
			}
		}
	}

	log.Debug("Latest version: " + strconv.Itoa(installed))
	return hash
}

func ourCI(owners []string) bool {
	for _, v := range owners {
		if v == "jenkins" {
			return true
		}
	}
	return false
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

		installed, err := strconv.Atoi(getInstalled())
		log.Check(log.FatalLevel, "Converting installed package timestamp to int", err)

		newsnap := ifUpdateable(installed)
		if len(newsnap) == 0 {
			log.Info("No update is available")
			os.Exit(1)
		} else if check {
			log.Info("Update is available")
			os.Exit(0)
		}
		upgradeRh(newsnap)

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
