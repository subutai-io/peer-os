package lib

import (
	"encoding/json"
	"io"
	"io/ioutil"
	"net/http"
	"os"
	"os/exec"
	"strconv"
	"strings"
	"time"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/log"
)

type update struct {
	id        string
	name      string
	timestamp int
}

func getList(kurjun *http.Client) (list []map[string]interface{}) {
	resp, err := kurjun.Get(config.Management.Kurjun + "/file/list")
	log.Check(log.FatalLevel, "GET: "+config.Management.Kurjun+"/file/list", err)
	defer resp.Body.Close()
	jsonlist, err := ioutil.ReadAll(resp.Body)
	log.Check(log.FatalLevel, "Reading response", err)
	log.Check(log.FatalLevel, "Parsing file list", json.Unmarshal(jsonlist, &list))
	return
}

func Update(name string, check bool) {
	switch name {
	case "rh":
		if !lockSubutai("rh.update") {
			log.Error("Another update process is already running")
		}
		defer unlockSubutai()

		kurjun := config.CheckKurjun()
		var lcl int
		var rmt update
		var date int64

		f, err := ioutil.ReadFile(config.Agent.AppPrefix + "/meta/package.yaml")
		if !log.Check(log.DebugLevel, "Reading file package.yaml", err) {
			lines := strings.Split(string(f), "\n")
			for _, v := range lines {
				if strings.HasPrefix(v, "version: ") {
					if version := strings.Split(strings.TrimPrefix(v, "version: "), "-"); len(version) > 1 {
						lcl, err = strconv.Atoi(version[1])
						log.Check(log.FatalLevel, "Converting timestamp to int", err)
					}
				}
			}
		}

		for _, v := range getList(kurjun) {
			item := v["name"].(string)
			if strings.HasPrefix(item, "subutai") && strings.HasSuffix(item, ".snap") {
				if version := strings.Split(strings.Trim(item, "subutai_ _amd64.snap"), "-"); len(version) > 1 {
					tmp, err := strconv.Atoi(version[1])
					log.Check(log.FatalLevel, "Converting timestamp to int", err)
					if tmp > rmt.timestamp {
						rmt.id = v["id"].(string)
						rmt.name = item
						rmt.timestamp = tmp
						date, err = strconv.ParseInt(version[1], 10, 64)
						log.Check(log.FatalLevel, "Getting update info", err)
					}
				}
			}
		}

		if lcl >= rmt.timestamp {
			log.Info("No update is available")
			os.Exit(1)
		} else if check {
			log.Info("Update from " + time.Unix(date, 0).String() + " is avalable")
			os.Exit(0)
		}

		log.Info("Updating Resource host")
		file, err := os.Create("/tmp/" + rmt.name)
		log.Check(log.FatalLevel, "Creating update file", err)
		defer file.Close()
		resp, err := kurjun.Get(config.Management.Kurjun + "/file/get?id=" + rmt.id)
		log.Check(log.FatalLevel, "GET: "+config.Management.Kurjun+"/file/get?id="+rmt.id, err)
		defer resp.Body.Close()
		_, err = io.Copy(file, resp.Body)
		log.Check(log.FatalLevel, "Writing response to file", err)

		log.Check(log.FatalLevel, "Installing update /tmp/"+rmt.name,
			exec.Command("snappy", "install", "--allow-unauthenticated", "/tmp/"+rmt.name).Run())
		log.Check(log.FatalLevel, "Removing update file /tmp/"+rmt.name, os.Remove("/tmp/"+rmt.name))

	default:
		if !container.IsContainer(name) {
			log.Error(name + " - no such instance")
		}
		_, err := container.AttachExec(name, []string{"apt-get", "update", "-y", "--force-yes", "-o", "Acquire::http::Timeout=5", "-qq"})
		log.Check(log.FatalLevel, "Updating apt index", err)
		output, err := container.AttachExec(name, []string{"apt-get", "upgrade", "-y", "--force-yes", "-o", "Acquire::http::Timeout=5", "-s", "-qq"})
		log.Check(log.FatalLevel, "Checking for available updade", err)
		if len(output) == 0 {
			log.Info("No update is available")
			os.Exit(1)
		} else if check {
			log.Info("Update is avalable")
			os.Exit(0)
		}
		_, err = container.AttachExec(name, []string{"apt-get", "upgrade", "-y", "--force-yes", "-o", "Acquire::http::Timeout=5", "-qq"})
		log.Check(log.FatalLevel, "Updating container", err)
	}
}
