package lib

import (
	"crypto/md5"
	"encoding/json"
	"fmt"
	"io"
	"io/ioutil"
	"net/http"
	"os"
	"strings"
	"time"

	"github.com/cheggaaa/pb"
	"github.com/nightlyone/lockfile"
	"github.com/pivotal-golang/archiver/extractor"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/gpg"
	"github.com/subutai-io/base/agent/lib/template"
	"github.com/subutai-io/base/agent/log"
)

var (
	lock   lockfile.Lockfile
	owners = []string{"subutai", "jenkins", "docker", ""}
)

type progress struct {
	Total int `json:"total"`
	Done  int `json:"done"`
}

type templ struct {
	name      string
	file      string
	version   string
	branch    string
	id        string
	owner     []string
	signature map[string]string
}

type metainfo struct {
	ID    string            `json:"id"`
	Name  string            `json:"name"`
	Owner []string          `json:"owner"`
	File  string            `json:"filename"`
	Signs map[string]string `json:"signature"`
}

func templId(t *templ, kurjun *http.Client, token string) {
	var meta metainfo

	url := config.Cdn.Kurjun + "/template/info?name=" + t.name + "&token=" + token
	if t.name == "management" && len(t.branch) != 0 {
		url = config.Cdn.Kurjun + "/template/info?name=" + t.name + "&version=" + t.version + "-" + t.branch + "&token=" + token
	} else if t.name == "management" {
		url = config.Cdn.Kurjun + "/template/info?name=" + t.name + "&version=" + t.version + "&token=" + token
	}

	response, err := kurjun.Get(url)
	defer response.Body.Close()
	log.Debug("Retrieving id, get: " + url)

	if err == nil && response.StatusCode == 404 && t.name == "management" {
		log.Warn("Requested management version not found, getting latest available")
		response, err = kurjun.Get(config.Cdn.Kurjun + "/template/info?name=" + t.name + "&version=" + t.branch + "&token=" + token)
	}
	if log.Check(log.WarnLevel, "Getting kurjun response", err) || response.StatusCode != 200 {
		return
	}

	defer response.Body.Close()
	body, err := ioutil.ReadAll(response.Body)

	if log.Check(log.WarnLevel, "Parsing response body", json.Unmarshal(body, &meta)) {
		return
	}

	if t.name != meta.Name {
		log.Info("Found: " + t.name + " -> " + meta.Name)
		t.name = meta.Name
	}
	t.id = meta.ID
	t.file = meta.File
	t.signature = meta.Signs
}

func md5sum(filePath string) string {
	file, err := os.Open(filePath)
	if err != nil {
		return ""
	}
	defer file.Close()

	hash := md5.New()
	if _, err := io.Copy(hash, file); err != nil {
		return ""
	}
	return fmt.Sprintf("%x", hash.Sum(nil))
}

func checkLocal(t *templ) bool {
	var response string
	files, _ := ioutil.ReadDir(config.Agent.LxcPrefix + "tmpdir")
	for _, f := range files {
		if strings.HasPrefix(f.Name(), t.name+"-subutai-template") {
			if len(t.id) == 0 {
				fmt.Print("Cannot verify local template. Trust anyway? (y/n)")
				_, err := fmt.Scanln(&response)
				log.Check(log.FatalLevel, "Reading input", err)
				if response == "y" {
					t.file = f.Name()
					return true
				}
				return false
			}
			if id := strings.Split(t.id, "."); len(id) > 0 && id[len(id)-1] == md5sum(config.Agent.LxcPrefix+"tmpdir/"+f.Name()) {
				return true
			}
		}
	}
	return false
}

func download(t templ, kurjun *http.Client, token string, torrent bool) bool {
	if len(t.id) == 0 {
		return false
	}
	out, err := os.Create(config.Agent.LxcPrefix + "tmpdir/" + t.file)
	log.Check(log.FatalLevel, "Creating file "+t.file, err)
	defer out.Close()

	url := config.Cdn.Kurjun + "/template/download?id=" + t.id

	if torrent {
		url = "http://" + config.Management.Host + ":8338/kurjun/rest/template/download?id=" + t.id
	} else if len(t.owner) > 0 {
		url = config.Cdn.Kurjun + "/template/" + t.owner[0] + "/" + t.file
	}
	response, err := kurjun.Get(url)
	log.Check(log.FatalLevel, "Getting "+url, err)

	if torrent && response.StatusCode == http.StatusAccepted {
		var bar *pb.ProgressBar
		for ; response.StatusCode == http.StatusAccepted; response, _ = kurjun.Get(url) {
			body, err := ioutil.ReadAll(response.Body)
			response.Body.Close()
			log.Check(log.WarnLevel, "Reading response from "+url, err)
			var t progress
			log.Check(log.WarnLevel, "Parsing response body", json.Unmarshal(body, &t))
			if bar == nil {
				bar = pb.New(t.Total).SetUnits(pb.U_BYTES)
				bar.Start()
			}
			bar.Set(t.Done)
			time.Sleep(time.Second)
		}
		bar.Update()
		_, err = io.Copy(out, response.Body)
		response.Body.Close()
	} else {
		defer response.Body.Close()
		bar := pb.New(int(response.ContentLength)).SetUnits(pb.U_BYTES)
		bar.Start()
		rd := bar.NewProxyReader(response.Body)
		_, err = io.Copy(out, rd)

		for c := 0; err != nil && c < 5; _, err = io.Copy(out, rd) {
			log.Info("Download interrupted, retrying")
			time.Sleep(3 * time.Second)
			c++

			//Repeating GET request to CDN, while need to continue interrupted download
			out, err = os.Create(config.Agent.LxcPrefix + "tmpdir/" + t.file)
			log.Check(log.FatalLevel, "Creating file "+t.file, err)
			defer out.Close()
			response, err = kurjun.Get(url)
			log.Check(log.FatalLevel, "Getting "+url, err)
			defer response.Body.Close()
			bar = pb.New(int(response.ContentLength)).SetUnits(pb.U_BYTES)
			bar.Start()
			rd = bar.NewProxyReader(response.Body)
		}
		log.Check(log.FatalLevel, "Writing response body to file", err)
	}

	if id := strings.Split(t.id, "."); len(id) > 0 && id[len(id)-1] == md5sum(config.Agent.LxcPrefix+"tmpdir/"+t.file) {
		return true
	}
	return false
}

func idToName(id string, kurjun *http.Client, token string) string {
	var meta metainfo

	//Since only kurjun knows template's ID, we cannot define if we have template already installed in system by ID as we do it by name, so unreachable kurjun in this case is a deadend for us
	//To omit this issue we should add ID into template config and use this ID as a "primary key" to any request
	response, err := kurjun.Get(config.Cdn.Kurjun + "/template/info?id=" + id + "&token=" + token)
	log.Check(log.ErrorLevel, "Getting kurjun response", err)
	defer response.Body.Close()

	body, err := ioutil.ReadAll(response.Body)

	if string(body) == "Not found" {
		log.Error("Template with id \"" + id + "\" not found")
	}
	log.Check(log.ErrorLevel, "Parsing response body", json.Unmarshal(body, &meta))

	return meta.Name
}

func lockSubutai(file string) bool {
	lock, err := lockfile.New("/var/run/lock/subutai." + file)
	if log.Check(log.DebugLevel, "Init lock "+file, err) {
		return false
	}

	err = lock.TryLock()
	if log.Check(log.DebugLevel, "Locking file "+file, err) {
		return false
	}

	return true
}

func unlockSubutai() {
	lock.Unlock()
}

func LxcImport(name, version, token string, torrent bool) {
	var kurjun *http.Client

	if container.IsContainer(name) && name == "management" && len(token) > 1 {
		gpg.ExchageAndEncrypt("management", token)
		return
	}

	if id := strings.Split(name, "id:"); len(id) > 1 {
		kurjun, _ = config.CheckKurjun()
		name = idToName(id[1], kurjun, token)
	}

	var t templ

	t.name = name
	if line := strings.Split(t.name, "/"); len(line) > 1 {
		t.name = line[1]
		t.owner = append(t.owner, line[0])
	}

	log.Info("Importing " + name)
	for !lockSubutai(t.name + ".import") {
		time.Sleep(time.Second * 1)
	}
	defer unlockSubutai()

	if container.IsContainer(t.name) {
		log.Info(t.name + " instance exist")
		return
	}

	t.version = config.Template.Version
	t.branch = config.Template.Branch
	if len(version) != 0 {
		if strings.Contains(version, "-") {
			verstr := strings.Split(version, "-")
			if len(verstr) == 2 {
				t.version = verstr[0]
				t.branch = verstr[1]
			} else {
				log.Error("Invalid version")
			}
		} else {
			if strings.Contains(version, ".") {
				t.version = version
				t.branch = config.Template.Branch
			} else {
				t.version = config.Template.Version
				t.branch = version
			}
		}
	}

	log.Info("Version: " + t.version + ", branch: " + t.branch)

	if t.branch == "" || t.name != "management" {
		t.file = t.name + "-subutai-template_" + t.version + "_" + config.Template.Arch + ".tar.gz"
	} else {
		t.file = t.name + "-subutai-template_" + t.version + "-" + t.branch + "_" + config.Template.Arch + ".tar.gz"
	}

	if kurjun == nil {
		kurjun, _ = config.CheckKurjun()
	}
	if kurjun != nil {
		templId(&t, kurjun, token)
	} else {
		log.Info("Trying to import from local storage")
	}

	if len(t.id) != 0 && len(t.signature) == 0 {
		log.Error("Template is not signed")
	}

	for owner, signature := range t.signature {
		// if v.Author == "public" || v.Author == "subutai" || v.Author == "jenkins" {
		signedhash := gpg.VerifySignature(gpg.KurjunUserPK(owner), signature)
		if t.id != signedhash {
			log.Error("Signature does not match with template id")
		}
		log.Info("Template's owner signature verified")
		log.Debug("Signature belongs to " + owner)
		break
		// }
	}

	if !checkLocal(&t) {
		log.Info("Downloading " + t.name)
		downloaded := false
		if len(t.owner) == 0 {
			for _, owner := range owners {
				if t.owner = []string{owner}; len(owner) == 0 {
					t.owner = []string{}
				}
				if download(t, kurjun, token, torrent) {
					downloaded = true
					break
				}
			}
		}
		if !downloaded && !download(t, kurjun, token, torrent) {
			log.Error("Failed to download or verify template " + t.name)
		}
		log.Info("File integrity verified")
	}

	time.Sleep(time.Millisecond * 200) // Added sleep to prevent output collision with progress bar.

	log.Info("Unpacking template " + t.name)
	log.Debug(config.Agent.LxcPrefix + "tmpdir/ " + t.file + " to " + t.name)
	tgz := extractor.NewTgz()
	templdir := config.Agent.LxcPrefix + "tmpdir/" + t.name
	log.Check(log.FatalLevel, "Extracting tgz", tgz.Extract(config.Agent.LxcPrefix+"tmpdir/"+t.file, templdir))
	parent := container.GetConfigItem(templdir+"/config", "subutai.parent")
	if parent != "" && parent != t.name && !container.IsTemplate(parent) {
		log.Info("Parent template required: " + parent)
		LxcImport(parent, "", token, torrent)
	}

	log.Info("Installing template " + t.name)
	template.Install(parent, t.name)
	// TODO following lines kept for back compatibility with old templates, should be deleted when all templates will be replaced.
	os.Rename(config.Agent.LxcPrefix+t.name+"/"+t.name+"-home", config.Agent.LxcPrefix+t.name+"/home")
	os.Rename(config.Agent.LxcPrefix+t.name+"/"+t.name+"-var", config.Agent.LxcPrefix+t.name+"/var")
	os.Rename(config.Agent.LxcPrefix+t.name+"/"+t.name+"-opt", config.Agent.LxcPrefix+t.name+"/opt")
	log.Check(log.FatalLevel, "Removing temp dir "+templdir, os.RemoveAll(templdir))

	if t.name == "management" {
		template.MngInit()
		return
	}

	container.SetContainerConf(t.name, [][]string{
		{"lxc.rootfs", config.Agent.LxcPrefix + t.name + "/rootfs"},
		{"lxc.mount", config.Agent.LxcPrefix + t.name + "/fstab"},
		{"lxc.hook.pre-start", ""},
		{"lxc.include", config.Agent.AppPrefix + "share/lxc/config/ubuntu.common.conf"},
		{"lxc.include", config.Agent.AppPrefix + "share/lxc/config/ubuntu.userns.conf"},
		{"subutai.config.path", config.Agent.AppPrefix + "etc"},
		{"lxc.network.script.up", config.Agent.AppPrefix + "bin/create_ovs_interface"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + t.name + "/home home none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + t.name + "/opt opt none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + t.name + "/var var none bind,rw 0 0"},
	})
}
