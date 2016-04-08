package lib

import (
	"crypto/md5"
	"fmt"
	"io"
	"io/ioutil"
	"net/http"
	"os"
	"runtime"
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
	lock lockfile.Lockfile
)

type templ struct {
	name    string
	file    string
	version string
	branch  string
	id      string
	hash    string
}

func templId(t templ, arch string, kurjun *http.Client) string {
	url := config.Cdn.Kurjun + "/template/info?name=" + t.name + "&version=" + t.version + "-" + t.branch + "&type=text"
	if len(t.branch) == 0 || t.name != "management" {
		url = config.Cdn.Kurjun + "/template/info?name=" + t.name + "&version=" + t.version + "&type=text"
	}
	response, err := kurjun.Get(url)
	log.Debug("Retrieving id, get: " + url)
	if err == nil && response.StatusCode == 204 {
		log.Warn("Cannot get template with specified version, trying without version")
		response, err = kurjun.Get(config.Cdn.Kurjun + "/template/info?name=" + t.name + "&type=text")
	}
	if log.Check(log.WarnLevel, "Getting kurjun response", err) || response.StatusCode != 200 {
		return ""
	}

	hash, err := ioutil.ReadAll(response.Body)
	response.Body.Close()
	if log.Check(log.WarnLevel, "Reading response body", err) {
		return ""
	}

	log.Debug("Template id: " + string(hash))
	return string(hash)
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

func checkLocal(t templ) bool {
	var response string
	files, _ := ioutil.ReadDir(config.Agent.LxcPrefix + "tmpdir")
	for _, f := range files {
		if t.file == f.Name() {
			if len(t.hash) == 0 {
				fmt.Print("Cannot check md5 of local archive. Trust anyway? (y/n)")
				_, err := fmt.Scanln(&response)
				log.Check(log.FatalLevel, "Reading input", err)
				if response == "y" {
					return true
				}
			}
			if t.hash == md5sum(config.Agent.LxcPrefix+"tmpdir/"+f.Name()) {
				return true
			}
		}
	}
	return false
}

func download(t templ, kurjun *http.Client) bool {
	out, err := os.Create(config.Agent.LxcPrefix + "tmpdir/" + t.file)
	log.Check(log.FatalLevel, "Creating file "+t.file, err)
	defer out.Close()
	response, err := kurjun.Get(config.Cdn.Kurjun + "/template/get?id=" + t.id)
	log.Check(log.FatalLevel, "Getting "+config.Cdn.Kurjun+"/template/get?id="+t.id, err)
	defer response.Body.Close()

	bar := pb.New(int(response.ContentLength)).SetUnits(pb.U_BYTES)
	bar.Start()
	rd := bar.NewProxyReader(response.Body)

	_, err = io.Copy(out, rd)
	log.Check(log.FatalLevel, "Writing file "+t.file, err)
	if t.hash == md5sum(config.Agent.LxcPrefix+"tmpdir/"+t.file) {
		return true
	}
	log.Error("Failed to check MD5 after download. Please check your connection and try again.")
	return false
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

func LxcImport(name, version, token string) {
	if container.IsContainer(name) && name == "management" && len(token) > 1 {
		gpg.ExchageAndEncrypt("management", token)
		return
	}

	log.Info("Importing " + name)
	for !lockSubutai(name + ".import") {
		time.Sleep(time.Second * 1)
	}
	defer unlockSubutai()

	if container.IsContainer(name) {
		log.Info(name + " instance exist")
		return
	}

	var t templ

	t.name = name
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

	kurjun := config.CheckKurjun()
	t.id = templId(t, runtime.GOARCH, kurjun)
	if len(strings.Split(t.id, ".")) > 1 {
		t.hash = strings.Split(t.id, ".")[1]
	}

	if !checkLocal(t) && !download(t, kurjun) {
		log.Error(t.name + " template not found")
	}

	log.Info("Unpacking template " + t.name)
	tgz := extractor.NewTgz()
	templdir := config.Agent.LxcPrefix + "tmpdir/" + t.name
	tgz.Extract(config.Agent.LxcPrefix+"tmpdir/"+t.file, templdir)
	parent := container.GetConfigItem(templdir+"/config", "subutai.parent")
	if parent != "" && parent != t.name && !container.IsTemplate(parent) {
		log.Info("Parent template required: " + parent)
		LxcImport(parent, "", token)
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
