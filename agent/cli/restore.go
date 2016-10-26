package cli

import (
	"os"
	"os/exec"
	"path"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"github.com/subutai-io/base/agent/config"
	lxcContainer "github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/fs"
	"github.com/subutai-io/base/agent/lib/template"
	"github.com/subutai-io/base/agent/log"

	"github.com/pivotal-golang/archiver/extractor"
)

// RestoreContainer restores a Subutai container to a snapshot at a specified timestamp if such a backup archive is available.
func RestoreContainer(container, date, newContainer string) {
	const backupDir = "/mnt/backups/"

	if lxcContainer.IsContainer(newContainer) {
		log.Fatal("Container " + newContainer + " is already exist!")
	}

	currentDT := strconv.Itoa(int(time.Now().Unix()))
	tmpUnpackDir := config.Agent.LxcPrefix + "tmpdir/unpacking_" + currentDT + "/"

	// tmpUnpackDir := backupDir + "tmpdir/unpacking_" + container + "_" + currentDT + "/"
	log.Check(log.FatalLevel, "Create UnPack tmp dir: "+tmpUnpackDir,
		os.MkdirAll(tmpUnpackDir, 0755))

	newContainerTmpDir := tmpUnpackDir + newContainer + "/"

	// making dir to newContainer
	log.Check(log.FatalLevel, "Create tmp dir for extract",
		os.MkdirAll(tmpUnpackDir+"/"+newContainer, 0755))

	flist, _ := filepath.Glob(backupDir + "*.tar.gz")
	tarball, _ := filepath.Glob(backupDir + container + "_" + date + "*.tar.gz")

	if len(tarball) == 0 {
		log.Fatal("Backup file not found: " + backupDir + container + "_" + date + "*.tar.gz")
	}

	if !strings.Contains(tarball[0], "Full") {
		// get files for unpack
		flist = append(flist[:position(flist, tarball[0])+1])
		flist = append(flist[position(flist, "Full"):])
	} else {
		flist = tarball
	}

	if !strings.Contains(flist[0], "Full") {
		log.Fatal("Cannot find Full Backup")
	}

	// UNPACKING tarballs
	for _, file := range flist {
		log.Check(log.WarnLevel, "Remove unpacked deltas dir",
			os.RemoveAll(tmpUnpackDir+container))

		log.Debug("unpacking " + file)
		unpack(file, tmpUnpackDir+container)
		deltas, _ := filepath.Glob(tmpUnpackDir + container + "/*.delta")

		// install deltas
		for _, deltaFile := range deltas {
			deltaName := strings.Replace(path.Base(deltaFile), ".delta", "", -1)
			parent := (newContainerTmpDir + deltaName + "@parent")
			dst := newContainerTmpDir
			// if strings.Contains(file, "Full") {
			// 	parent = dst
			// }
			fs.Receive(parent, dst, "unpacking_"+currentDT+"/"+container+"/"+path.Base(deltaFile),
				!strings.Contains(file, "Full"))

			fs.SubvolumeDestroy(newContainerTmpDir + deltaName + "@parent")
			log.Check(log.DebugLevel, "Rename unpacked subvolume to @parent "+newContainerTmpDir+deltaName+" -> "+newContainerTmpDir+deltaName+"@parent",
				exec.Command("mv",
					newContainerTmpDir+deltaName,
					newContainerTmpDir+deltaName+"@parent").Run())
		}
	}

	// create NewContainer subvolume
	fs.SubvolumeCreate(config.Agent.LxcPrefix + newContainer)

	// move volumes
	volumes, _ := filepath.Glob(newContainerTmpDir + "/*")

	for _, volume := range volumes {
		fs.SetVolReadOnly(volume, false)
		volumeName := path.Base(volume)
		volumeName = strings.Replace(volumeName, "@parent", "", -1)
		log.Check(log.DebugLevel, "Move "+volumeName+" volume to "+config.Agent.LxcPrefix+newContainer+"/"+volumeName,
			exec.Command("mv", volume, config.Agent.LxcPrefix+newContainer+"/"+volumeName).Run())
	}

	// restore meta files
	log.Check(log.FatalLevel, "Restore meta files",
		exec.Command("rsync", "-av", tmpUnpackDir+container+"/meta/", config.Agent.LxcPrefix+newContainer).Run())

	// clean
	log.Check(log.WarnLevel, "Remove unpacked deltas dir",
		os.RemoveAll(tmpUnpackDir))

	// changing newcontainer mac
	lxcContainer.SetContainerConf(newContainer, [][]string{
		{"lxc.network.hwaddr", template.Mac()},
	})

	// changing newcontainer config
	lxcContainer.SetContainerConf(newContainer, [][]string{
		{"lxc.network.veth.pair", strings.Replace(lxcContainer.GetConfigItem(config.Agent.LxcPrefix+newContainer+"/config", "lxc.network.hwaddr"), ":", "", -1)},
		{"lxc.network.script.up", config.Agent.AppPrefix + "bin/create_ovs_interface"},
		{"lxc.rootfs", config.Agent.LxcPrefix + newContainer + "/rootfs"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + newContainer + "/home home none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + newContainer + "/opt opt none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + newContainer + "/var var none bind,rw 0 0"},
		{"lxc.utsname", newContainer},
		{"lxc.mount", config.Agent.LxcPrefix + newContainer + "/fstab"},
	})

}

// position returns index of string from "slice" which contains "value"
func position(slice []string, value string) int {
	for p, v := range slice {
		if strings.Contains(v, value) {
			return p
		}
	}
	return -1
}

// Unpack extract passed archive to directory
func unpack(archive, dir string) {
	tgz := extractor.NewTgz()
	tgz.Extract(archive, dir)
}
