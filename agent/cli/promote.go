package lib

import (
	"io/ioutil"
	"os"
	"os/exec"
	"strings"
	"subutai/config"
	"subutai/lib/container"
	"subutai/lib/fs"
	"subutai/lib/net"
	"subutai/log"
	"syscall"
)

// LxcPromote promotes the given container name.
func LxcPromote(name string) {
	checkSanity(name)

	// check: start container if it is not running already
	if container.State(name) != "RUNNING" {
		container.Start(name)
		// log.Info("Container " + name + " is started")
	}
	// check: write package list to packages
	pkgCmmdResult, errResult := container.AttachExec(name, []string{"dpkg", "-l"})
	if errResult != nil {
		log.Error("There is no result from dpkg -l command")
	}
	strCmdRes := strings.Join(pkgCmmdResult, "\n")
	log.Check(log.FatalLevel, "Write packages",
		ioutil.WriteFile(config.Agent.LxcPrefix+name+"/packages",
			[]byte(strCmdRes), 0755))
	if container.State(name) == "RUNNING" {
		container.Stop(name)
	}
	net.RemoveDefaultGW(name)

	cleanupFS(config.Agent.LxcPrefix+name+"/rootfs/.git", 0000)
	cleanupFS(config.Agent.LxcPrefix+"lxc-data/"+name+"-var/log/", 0775)
	cleanupFS(config.Agent.LxcPrefix+"lxc-data/"+name+"-var/cache", 0775)
	cleanupFS(config.Agent.LxcPrefix+"lxc-data/"+name+"-var/lib/apt/lists/", 0775)

	makeDiff(name)

	iface := container.GetConfigItem(config.Agent.LxcPrefix+name+"/config", "lxc.network.veth.pair")
	net.ConfigureOVS(iface)
	container.ResetNet(name)
	fs.ReadOnly(name, true)
	log.Info(name + " promoted")
}

func cleanupFS(path string, perm os.FileMode) {
	if perm == 0000 {
		os.RemoveAll(path)
	} else {
		fi, _ := os.Stat(path)
		uid := fi.Sys().(*syscall.Stat_t).Uid
		gid := fi.Sys().(*syscall.Stat_t).Gid
		os.RemoveAll(path)
		os.MkdirAll(path, perm)
		os.Chown(path, int(uid), int(gid))
	}
}

func makeDiff(name string) {
	parent := container.GetParent(name)
	if parent == name || len(parent) < 1 {
		return
	}
	os.MkdirAll(config.Agent.LxcPrefix+name+"/diff", 0600)
	execDiff(config.Agent.LxcPrefix+parent+"/rootfs", config.Agent.LxcPrefix+name+"/rootfs", config.Agent.LxcPrefix+name+"/diff/rootfs.diff")
	execDiff(config.Agent.LxcPrefix+"lxc/"+parent+"-opt", config.Agent.LxcPrefix+"lxc/"+name+"-opt", config.Agent.LxcPrefix+name+"/diff/opt.diff")
	execDiff(config.Agent.LxcPrefix+"lxc-data/"+parent+"-var", config.Agent.LxcPrefix+"lxc-data/"+name+"-var", config.Agent.LxcPrefix+name+"/diff/var.diff")
	execDiff(config.Agent.LxcPrefix+"lxc-data/"+parent+"-home", config.Agent.LxcPrefix+"lxc-data/"+name+"-home", config.Agent.LxcPrefix+name+"/diff/home.diff")
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
