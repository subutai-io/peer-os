package lib

import (
	"io/ioutil"
	"os"
	"os/exec"
	"path/filepath"
	"strings"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/fs"
	"github.com/subutai-io/base/agent/lib/net"
	"github.com/subutai-io/base/agent/log"
)

// LxcPromote promotes the given container name.
func LxcPromote(name string) {
	checkSanity(name)

	// check: start container if it is not running already
	if container.State(name) != "RUNNING" {
		LxcStart(name)
		// log.Info("Container " + name + " is started")
	}

	// check: write package list to packages
	pkgCmdResult, _ := container.AttachExec(name, []string{"timeout", "60", "dpkg", "-l"})
	strCmdRes := strings.Join(pkgCmdResult, "\n")
	log.Check(log.FatalLevel, "Write packages",
		ioutil.WriteFile(config.Agent.LxcPrefix+name+"/packages",
			[]byte(strCmdRes), 0755))
	if container.State(name) == "RUNNING" {
		container.Stop(name)
	}
	net.RestoreDefaultConf(name)

	cleanupFS(config.Agent.LxcPrefix+name+"/rootfs/.git", 0000)
	cleanupFS(config.Agent.LxcPrefix+name+"/var/log/", 0775)
	cleanupFS(config.Agent.LxcPrefix+name+"/var/cache", 0775)
	cleanupFS(config.Agent.LxcPrefix+name+"/var/lib/apt/lists/", 0000)

	makeDiff(name)

	container.ResetNet(name)
	fs.ReadOnly(name, true)
	log.Info(name + " promoted")
}

func clearFile(path string, f os.FileInfo, err error) error {
	if !f.IsDir() {
		ioutil.WriteFile(path, []byte{}, 0775)
	}
	return nil
}

func cleanupFS(path string, perm os.FileMode) {
	if perm == 0000 {
		os.RemoveAll(path)
	} else {
		filepath.Walk(path, clearFile)
	}
}

func makeDiff(name string) {
	parent := container.GetParent(name)
	if parent == name || len(parent) < 1 {
		return
	}
	os.MkdirAll(config.Agent.LxcPrefix+name+"/diff", 0600)
	execDiff(config.Agent.LxcPrefix+parent+"/rootfs", config.Agent.LxcPrefix+name+"/rootfs", config.Agent.LxcPrefix+name+"/diff/rootfs.diff")
	execDiff(config.Agent.LxcPrefix+parent+"/home", config.Agent.LxcPrefix+name+"/home", config.Agent.LxcPrefix+name+"/diff/home.diff")
	execDiff(config.Agent.LxcPrefix+parent+"/opt", config.Agent.LxcPrefix+name+"/opt", config.Agent.LxcPrefix+name+"/diff/opt.diff")
	execDiff(config.Agent.LxcPrefix+parent+"/var", config.Agent.LxcPrefix+name+"/var", config.Agent.LxcPrefix+name+"/diff/var.diff")
}

func execDiff(dir1, dir2, output string) {
	var out []byte
	out, _ = exec.Command("diff", "-Nur", dir1, dir2).Output()
	err := ioutil.WriteFile(output, out, 0600)
	log.Check(log.FatalLevel, "Writing diff to file"+output, err)
}

func checkSanity(name string) {
	// check: if name exists
	if !container.IsContainer(name) {
		log.Error("Container " + name + " does not exist")
	}

	// check: if name is template
	if container.IsTemplate(name) {
		log.Error("Template " + name + " already exists")
	}
	// check: remove default gateway

	parent := container.GetParent(name)
	if parent == name || len(parent) < 1 {
		return
	}
	if !container.IsTemplate(container.GetParent(name)) {
		log.Error("Parent template " + container.GetParent(name) + " not found")
	}
}
