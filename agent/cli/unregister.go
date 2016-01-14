package lib

import (
	"errors"
	"net/http"
	"os/exec"
	"subutai/config"
	"subutai/lib/fs"
	"subutai/lib/template"
	"subutai/log"
)

func LxcUnregister(containerName string) {

	log.Info(containerName + " unregister process started.")
	// check: sanity
	log.Check(log.FatalLevel, "check availability: ", checkUnregisterAvailability(containerName))
	// check: delete from git --> git needs to ne replaced.
	fs.ReadOnly(containerName, false)
	log.Check(log.FatalLevel, "unregister git del", exec.Command("git",
		"--git-dir="+config.Agent.LxcPrefix+"master/rootfs/.git", "--work-tree="+config.Agent.LxcPrefix+"master/rootfs/",
		"push", "origin", "--delete", containerName).Run())
	fs.ReadOnly(containerName, true)
	log.Info(containerName + " is removed from git repo")
	// check: delete and unregister from rest end // $template/$version/remove
	resp, err := http.Get(config.Management.Kurjun + containerName + "/" + config.Misc.Version + "/remove")
	if err != nil {
		log.Error("delete git from rest: " + err.Error())
	}
	defer resp.Body.Close()
	if resp.StatusCode != 200 {
		log.Error("could not able to unregister from rest end")
	}
	log.Info("deleted from rest end")
	// check: remove lock file
	// lockfolder := cfg.Lxcpath.Lockfolder
	// log.Check(log.FatalLevel, "remove lock file: ", os.Remove(lockfolder+"/."+containerName))
	log.Info(containerName + " unregister finished")
}

func checkUnregisterAvailability(containerName string) error {
	if !template.IsRegistered(containerName) {
		return errors.New(containerName + " is not registered as template.")
	}

	resp, err := http.Get(config.Management.Kurjun + containerName + "/" + config.Misc.Version + "/children")
	if err != nil {
		log.Error("http get children: " + err.Error())
	}
	defer resp.Body.Close()
	if resp.StatusCode == 200 {
		return errors.New(containerName + " has a child templates.")
	}
	resp, err = http.Get(config.Management.Kurjun + containerName + "/" + config.Misc.Version + "/is-used-on-fai")
	defer resp.Body.Close()
	if err != nil {
		log.Error("http in use check: " + err.Error())
	}
	if resp.StatusCode == 200 {
		return errors.New(containerName + " is in use")
	}
	return nil
}
