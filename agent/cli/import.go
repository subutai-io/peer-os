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

func templId(templ, arch, version string, kurjun *http.Client) string {
	url := config.Management.Kurjun + "/template/info?name=" + templ + "&version=" + version + "&type=text"
	if version == "stable" || len(version) == 0 {
		url = config.Management.Kurjun + "/template/info?name=" + templ + "&type=text"
	}
	response, err := kurjun.Get(url)
	log.Debug(config.Management.Kurjun + "/template/info?name=" + templ + "&type=text")
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

func checkLocal(templ, md5, arch string) string {
	var response string
	files, _ := ioutil.ReadDir(config.Agent.LxcPrefix + "tmpdir")
	for _, f := range files {
		file := strings.Split(f.Name(), "-subutai-template_")
		if len(file) == 2 && file[0] == templ && strings.Contains(file[1], arch) {
			if len(md5) == 0 {
				fmt.Print("Cannot check md5 of local template. Trust anyway? (y/n)")
				_, err := fmt.Scanln(&response)
				log.Check(log.FatalLevel, "Reading input", err)
				if response == "y" {
					return config.Agent.LxcPrefix + "tmpdir/" + f.Name()
				}
			}
			if md5 == md5sum(config.Agent.LxcPrefix+"tmpdir/"+f.Name()) {
				return config.Agent.LxcPrefix + "tmpdir/" + f.Name()
			}
		}
	}
	return ""
}

func download(file, id string, kurjun *http.Client) string {
	out, err := os.Create(config.Agent.LxcPrefix + "tmpdir/" + file)
	log.Check(log.FatalLevel, "Creating file "+file, err)
	defer out.Close()
	response, err := kurjun.Get(config.Management.Kurjun + "/template/get?id=" + id)
	log.Check(log.FatalLevel, "Getting "+config.Management.Kurjun+"/template/get?id="+id, err)
	defer response.Body.Close()

	bar := pb.New(int(response.ContentLength)).SetUnits(pb.U_BYTES)
	bar.Start()
	rd := bar.NewProxyReader(response.Body)

	_, err = io.Copy(out, rd)
	log.Check(log.FatalLevel, "Writing file "+file, err)
	if strings.Split(id, ".")[1] == md5sum(config.Agent.LxcPrefix+"tmpdir/"+file) {
		return config.Agent.LxcPrefix + "tmpdir/" + file
	}
	log.Error("Failed to check MD5 after download. Please check your connection and try again.")
	return ""
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

func LxcImport(templ, version, token string) {
	log.Info("Importing " + templ)
	for !lockSubutai(templ + ".import") {
		time.Sleep(time.Second * 1)
	}
	defer unlockSubutai()

	if container.IsContainer(templ) && templ == "management" && len(token) > 1 {
		gpg.ExchageAndEncrypt("management", token)
		return
	}

	if container.IsContainer(templ) {
		log.Info(templ + " container exist")
		return
	}

	fullname := templ + "-subutai-template_" + config.Template.Version + "_" + config.Template.Arch + ".tar.gz"
	// if len(token) == 0 {
	// token = gpg.GetToken()
	// }
	if len(version) == 0 && templ == "management" {
		version = config.Management.Version
	}
	kurjun := config.CheckKurjun()
	id := templId(templ, runtime.GOARCH, version, kurjun)
	md5 := ""
	if len(strings.Split(id, ".")) > 1 {
		md5 = strings.Split(id, ".")[1]
	}

	archive := checkLocal(templ, md5, runtime.GOARCH)
	if len(archive) == 0 && len(md5) != 0 {
		log.Info("Downloading " + templ)
		archive = download(fullname, id, kurjun)
	} else if len(archive) == 0 && len(md5) == 0 {
		log.Error(templ + " " + version + " template not found")
	}

	log.Info("Unpacking template " + templ)
	tgz := extractor.NewTgz()
	tgz.Extract(archive, config.Agent.LxcPrefix+"tmpdir/"+templ)
	templdir := config.Agent.LxcPrefix + "tmpdir/" + templ
	parent := container.GetConfigItem(templdir+"/config", "subutai.parent")
	if parent != "" && parent != templ && !container.IsTemplate(parent) {
		log.Info("Parent template required: " + parent)
		LxcImport(parent, "stable", token)
	}

	log.Info("Installing template " + templ)
	template.Install(parent, templ)
	// TODO following lines kept for back compatibility with old templates, should be deleted when all templates will be replaced.
	os.Rename(config.Agent.LxcPrefix+templ+"/"+templ+"-home", config.Agent.LxcPrefix+templ+"/home")
	os.Rename(config.Agent.LxcPrefix+templ+"/"+templ+"-var", config.Agent.LxcPrefix+templ+"/var")
	os.Rename(config.Agent.LxcPrefix+templ+"/"+templ+"-opt", config.Agent.LxcPrefix+templ+"/opt")
	log.Check(log.FatalLevel, "Removing temp dir "+templdir, os.RemoveAll(templdir))

	if templ == "management" {
		template.MngInit()
		return
	}

	container.SetContainerConf(templ, [][]string{
		{"lxc.rootfs", config.Agent.LxcPrefix + templ + "/rootfs"},
		{"lxc.mount", config.Agent.LxcPrefix + templ + "/fstab"},
		{"lxc.hook.pre-start", ""},
		{"lxc.include", config.Agent.AppPrefix + "share/lxc/config/ubuntu.common.conf"},
		{"lxc.include", config.Agent.AppPrefix + "share/lxc/config/ubuntu.userns.conf"},
		{"subutai.config.path", config.Agent.AppPrefix + "etc"},
		{"lxc.network.script.up", config.Agent.AppPrefix + "bin/create_ovs_interface"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + templ + "/home home none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + templ + "/opt opt none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + templ + "/var var none bind,rw 0 0"},
	})
}
