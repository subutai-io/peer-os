package lib

import (
	"os"
	"os/exec"
	"path"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"github.com/subutai-io/Subutai/agent/config"
	"github.com/subutai-io/Subutai/agent/lib/fs"
	"github.com/subutai-io/Subutai/agent/log"

	"github.com/pivotal-golang/archiver/extractor"
)

func RestoreContainer(container, date, newContainer string) {
	const backupDir = "/mnt/backups/"

	currentDT := strconv.Itoa(int(time.Now().Unix()))
	tmpUnpackDir := config.Agent.LxcPrefix + "lxc-data/tmpdir/unpacking_" + currentDT + "/"
	newContainerTmpDir := tmpUnpackDir + newContainer + "/"

	// making dir to newContainer
	log.Check(log.FatalLevel, "Create tmp dir for extract",
		os.MkdirAll(tmpUnpackDir+"/"+newContainer, 0755))

	// subutai restore container date newcontainer
	// argsWithoutProg := os.Args[1:]
	// var flist []string
	flist, _ := filepath.Glob(backupDir + "*.tar.gz")
	tarball, _ := filepath.Glob(backupDir + container + "_" + date + "*.tar.gz")
	log.Info(backupDir + container + "_" + date + "*.tar.gz")
	if len(tarball) == 0 {
		log.Fatal("Backup file for found: " + backupDir + container + "_" + date + "*.tar.gz")
	}

	if !strings.Contains(tarball[0], "Full") {
		// get files for unpack
		flist = append(flist[:Position(flist, tarball[0])+1])
		flist = append(flist[Position(flist, "Full"):])
	} else {
		flist = tarball
	}

	log.Info(strings.Join(flist, ", "))
	if !strings.Contains(flist[0], "Full") {
		log.Fatal("Cannot find Full Backup")
	}

	// UNPACKING tarballs
	for _, file := range flist {
		log.Check(log.WarnLevel, "Remove unpacked deltas dir",
			os.RemoveAll(tmpUnpackDir+container))

		log.Info("unpacking " + file)
		Unpack(file, tmpUnpackDir+container)
		deltas, _ := filepath.Glob(tmpUnpackDir + container + "/*.delta")

		// install deltas
		for _, deltaFile := range deltas {
			deltaName := strings.Replace(path.Base(deltaFile), ".delta", "", -1)
			parent := (newContainerTmpDir + container + "-" + deltaName + "@parent")
			dst := (newContainerTmpDir)
			if strings.Contains(file, "Full") {
				parent = dst
			}
			log.Info(parent + ";" + dst + ";" + "unpacking_" + currentDT + "/" + container + "/" + path.Base(deltaFile))
			fs.Receive(parent, dst, "unpacking_"+currentDT+"/"+container+"/"+path.Base(deltaFile),
				!strings.Contains(file, "Full"))

			fs.SubvolumeDestroy(newContainerTmpDir + container + "-" + deltaName + "@parent")
			log.Check(log.DebugLevel, "Rename unpacked subvolume to @parent "+newContainerTmpDir+container+"-"+deltaName+"@today"+" -> "+newContainerTmpDir+container+"-"+deltaName+"@parent",
				exec.Command("mv",
					newContainerTmpDir+container+"-"+deltaName+"@today",
					newContainerTmpDir+container+"-"+deltaName+"@parent").Run())
		}

	}

	// move volumes
	volumes, _ := filepath.Glob(newContainerTmpDir + "/*")
	log.Info(strings.Join(volumes, ", "))

	for _, volume := range volumes {
		fs.SetVolReadOnly(volume, false)

		switch {
		case strings.Contains(volume, "rootfs"):
			log.Check(log.FatalLevel, "Create Container dir",
				os.MkdirAll(config.Agent.LxcPrefix+newContainer, 0755))
			log.Check(log.DebugLevel, "Move rootfs volume to "+config.Agent.LxcPrefix+newContainer,
				exec.Command("mv", volume, config.Agent.LxcPrefix+newContainer+"/rootfs").Run())
		case strings.Contains(volume, "opt"):
			log.Check(log.DebugLevel, "Move opt volume to "+config.Agent.LxcPrefix+newContainer,
				exec.Command("mv", volume, config.Agent.LxcPrefix+"/lxc/"+newContainer+"-opt").Run())
		case strings.Contains(volume, "home"):
			log.Check(log.DebugLevel, "Move home volume to "+config.Agent.LxcPrefix+newContainer,
				exec.Command("mv", volume, config.Agent.LxcPrefix+"/lxc-data/"+newContainer+"-home").Run())
		case strings.Contains(volume, "var"):
			log.Check(log.DebugLevel, "Move car volume to "+config.Agent.LxcPrefix+newContainer,
				exec.Command("mv", volume, config.Agent.LxcPrefix+"/lxc-data/"+newContainer+"-var").Run())

		}
	}
	// restore meta files
	log.Check(log.FatalLevel, "Restore meta files",
		exec.Command("rsync", "-av", tmpUnpackDir+container+"/meta/", config.Agent.LxcPrefix+newContainer).Run())

	// clean
	log.Check(log.WarnLevel, "Remove unpacked deltas dir",
		os.RemoveAll(tmpUnpackDir))

}

func Position(slice []string, value string) int {
	for p, v := range slice {
		if strings.Contains(v, value) {
			return p
		}
	}
	return -1
}

func Unpack(archive, dir string) {
	tgz := extractor.NewTgz()
	tgz.Extract(archive, dir)
}
