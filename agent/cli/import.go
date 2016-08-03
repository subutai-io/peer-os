package lib

import (
	"bytes"
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
	"golang.org/x/crypto/openpgp"
	"golang.org/x/crypto/openpgp/clearsign"

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
	owner   []string
	signa   signature
}

type metainfo struct {
	ID     string    `json:"id"`
	Owner  []string  `json:"owner"`
	Md5Sum string    `json:"md5Sum"`
	Signs  signature `json:"signature"`
}

type signature []struct {
	Author string
	Sign   string
}

func templId(t *templ, kurjun *http.Client) {
	var meta metainfo

	url := config.Cdn.Kurjun + "/template/info?name=" + t.name
	if t.name == "management" && len(t.branch) != 0 {
		url = config.Cdn.Kurjun + "/template/info?name=" + t.name + "&version=" + t.version + "-" + t.branch
	} else if t.name == "management" {
		url = config.Cdn.Kurjun + "/template/info?name=" + t.name + "&version=" + t.version
	}
	response, err := kurjun.Get(url)
	defer response.Body.Close()
	log.Debug("Retrieving id, get: " + url)

	if err == nil && response.StatusCode == 204 && t.name == "management" {
		log.Warn("Cannot get management with specified version, trying without version")
		response, err = kurjun.Get(config.Cdn.Kurjun + "/template/info?name=" + t.name)
	}
	if log.Check(log.WarnLevel, "Getting kurjun response", err) || response.StatusCode != 200 {
		return
	}

	defer response.Body.Close()
	body, err := ioutil.ReadAll(response.Body)

	if log.Check(log.WarnLevel, "Parsing response body", json.Unmarshal(body, &meta)) {
		return
	}

	t.id = meta.ID
	t.owner = meta.Owner
	t.signa = meta.Signs
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
			if len(t.id) == 0 {
				fmt.Print("Cannot verify local template. Trust anyway? (y/n)")
				_, err := fmt.Scanln(&response)
				log.Check(log.FatalLevel, "Reading input", err)
				if response == "y" {
					return true
				}
				return false
			}
			if t.id == md5sum(config.Agent.LxcPrefix+"tmpdir/"+f.Name()) {
				return true
			}
		}
	}
	return false
}

func download(t templ, kurjun *http.Client) bool {
	if len(t.id) == 0 {
		return false
	}
	out, err := os.Create(config.Agent.LxcPrefix + "tmpdir/" + t.file)
	log.Check(log.FatalLevel, "Creating file "+t.file, err)
	defer out.Close()
	log.Info("Downloading " + t.name)

	response, err := kurjun.Get(config.Cdn.Kurjun + "/template/download?id=" + t.id)
	log.Check(log.FatalLevel, "Getting "+config.Cdn.Kurjun+"/template/download?id="+t.id, err)
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
		response, err = kurjun.Get(config.Cdn.Kurjun + "/template/download?id=" + t.id)
		log.Check(log.FatalLevel, "Getting "+config.Cdn.Kurjun+"/template/download?id="+t.id, err)
		defer response.Body.Close()
		bar = pb.New(int(response.ContentLength)).SetUnits(pb.U_BYTES)
		bar.Start()
		rd = bar.NewProxyReader(response.Body)
	}

	log.Check(log.FatalLevel, "Writing response body to file", err)

	if id := strings.Split(t.id, "."); len(id) > 0 && id[len(id)-1] == md5sum(config.Agent.LxcPrefix+"tmpdir/"+t.file) {
		return true
	}
	log.Error("Failed to check MD5 after download. Please check your connection and try again.")
	return false
}

func getOwnerKey(owner string) string {
	response, err := http.Get("https://" + config.Cdn.Url + ":" + config.Cdn.Sslport + "/kurjun/rest/auth/key?user=" + owner)
	log.Check(log.FatalLevel, "Getting owner public key", err)
	defer response.Body.Close()
	key, err := ioutil.ReadAll(response.Body)
	log.Check(log.FatalLevel, "Reading key body", err)
	return string(key)
}

func verifySignature(key, signature string) string {
	entity, err := openpgp.ReadArmoredKeyRing(bytes.NewBufferString(key))
	log.Check(log.WarnLevel, "Reading user public key", err)

	if block, _ := clearsign.Decode([]byte(signature)); block != nil {
		_, err = openpgp.CheckDetachedSignature(entity, bytes.NewBuffer(block.Bytes), block.ArmoredSignature.Body)
		if log.Check(log.ErrorLevel, "Checking signature", err) {
			return ""
		}
		return string(block.Bytes)
	}
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
	templId(&t, kurjun)

	if len(t.id) != 0 && len(t.signa) == 0 {
		log.Warn("Template is not signed")
	}
	for _, v := range t.signa {
		// if v.Author == "public" || v.Author == "subutai" || v.Author == "jenkins" {
		signedhash := verifySignature(getOwnerKey(v.Author), v.Sign)
		if t.id != signedhash {
			log.Error("Signature does not match with template hash")
		}
		log.Info("Digital signature verification succeeded, owner and template integrity are valid")
		break
		// }
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
