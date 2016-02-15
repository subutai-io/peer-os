package lib

import (
	"bufio"
	"io/ioutil"
	"os"
	"os/exec"
	"path"
	"path/filepath"
	"reflect"
	"strconv"
	"strings"
	"time"

	"github.com/subutai-io/Subutai/agent/config"
	"github.com/subutai-io/Subutai/agent/lib/fs"
	"github.com/subutai-io/Subutai/agent/lib/template"
	"github.com/subutai-io/Subutai/agent/log"
)

func BackupContainer(container string, full bool) {
	const backupDir = "/mnt/backups/"
	var changelog []string

	currentDT := strconv.Itoa(int(time.Now().Unix()))
	if full {
		currentDT = currentDT + "_Full"
	}

	// for easy removal of backup directory
	if _, err := os.Stat(backupDir + container); os.IsNotExist(err) {
		fs.SubvolumeCreate(backupDir + container)
	}

	lastSnapshotDir := GetLastSnapshotDir(currentDT, backupDir+container)
	log.Debug("last snapshot dir: " + lastSnapshotDir)

	if !full && lastSnapshotDir == "" {
		log.Fatal("Last backup not found or corrupted. Try make full backup.")
	}

	containerSnapshotDir := backupDir + container + "/" + currentDT
	log.Check(log.FatalLevel, "Create dir for snapshots: "+containerSnapshotDir,
		os.MkdirAll(containerSnapshotDir, 0755))

	tmpBackupDir := backupDir + "tmpdir/" + container + "_" + currentDT + "/"
	log.Check(log.FatalLevel, "Create Backup tmp dir: "+tmpBackupDir,
		os.MkdirAll(tmpBackupDir, 0755))

	tarballName := backupDir + container + "_" + currentDT + ".tar.gz"
	changelogName := backupDir + container + "_" + currentDT + "_changelog.txt"

	for _, subvol := range GetContainerMountPoints(container) {
		subvolBase := path.Base(subvol)

		subvolBaseMountpointPath := "/" + subvolBase
		if subvolBase == "rootfs" {
			subvolBaseMountpointPath = ""
		}

		containerSnapshotName := containerSnapshotDir + "/" + subvolBase

		fs.SubvolumeClone(subvol, containerSnapshotName)
		fs.SetVolReadOnly(containerSnapshotName, true)

		if full {
			fs.Send(containerSnapshotDir+"/"+subvolBase, containerSnapshotDir+"/"+subvolBase,
				tmpBackupDir+string(subvolBase)+".delta")
		} else {
			fs.Send(lastSnapshotDir+"/"+subvolBase, containerSnapshotDir+"/"+subvolBase,
				tmpBackupDir+string(subvolBase)+".delta")
		}

		if lastSnapshotDir != "" {
			changelog = append(changelog, GetModifiedList(containerSnapshotDir+"/"+subvolBase+"/",
				lastSnapshotDir+"/"+subvolBase+"/",
				subvolBaseMountpointPath+"/")...)

		}
	}

	log.Check(log.FatalLevel, "Copy meta files",
		exec.Command("rsync", "-av", `--exclude`, `/rootfs`, config.Agent.LxcPrefix+container+"/", tmpBackupDir+"meta").Run())

	log.Check(log.FatalLevel, "Create Changelog file on tmpdir",
		ioutil.WriteFile(changelogName, []byte(strings.Join(changelog, "\n")), 0644))

	template.Tar(tmpBackupDir, tarballName)

	log.Check(log.FatalLevel, "Remove tmpdir", os.RemoveAll(backupDir+"/tmpdir"))
}

func GetContainerMountPoints(container string) []string {
	var mountPoints []string

	configPath := config.Agent.LxcPrefix + container + "/config"

	file, err := os.Open(configPath)
	if err != nil {
		log.Error("Cannot open Container Config " + configPath)
	}
	log.Check(log.FatalLevel, "Opening container config file", err)
	defer file.Close()

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		if scanner.Text() != "" && (string(scanner.Text()[0])) != "#" {
			if s := strings.Split(scanner.Text(), " = "); s[0] == "lxc.rootfs" {
				mountPoints = append(mountPoints, strings.Split(s[1], " ")[0])
			}
			if s := strings.Split(scanner.Text(), " = "); s[0] == "lxc.mount.entry" {
				mountPoints = append(mountPoints, strings.Split(s[1], " ")[0])
			}
		}
	}

	return mountPoints
}

func GetModifiedList(td, ytd, rdir string) []string {
	var list []string

	data, err := exec.Command("rsync", "-avun", `--delete`, `--out-format="%i %n %L"`, td, ytd).Output()
	log.Check(log.WarnLevel, "Generate list of changed files", err)

	lines := strings.Split(string(data), "\n")
	for _, l := range lines {
		l = strings.Replace(l, `"`, "", -1)
		if len(l) == 0 {
			continue
		}
		if strings.Contains(l, `sending incremental file list`) {
			continue
		}
		if strings.Contains(l, `sent`) && strings.Contains(l, `bytes`) && strings.Contains(l, `received`) {
			continue
		}
		if strings.Contains(l, `total size`) && strings.Contains(l, `speedup is`) && strings.Contains(l, `(DRY RUN)`) {
			continue
		}
		line := strings.Fields(l)
		if strings.Contains(line[0], `*deleting`) {
			list = append(list, "deleted "+rdir+strings.Join(line[1:], " "))
			continue
		}
		if strings.Contains(line[0], `+++++++++`) {
			list = append(list, "added "+rdir+strings.Join(line[1:], " "))
			continue
		}

		if string(line[0][3]) == `s` || string(line[0][4]) == `t` {
			list = append(list, "modified "+rdir+strings.Join(line[1:], " "))
			continue
		}
	}

	return list
}

// there make some checks of dir
func GetLastSnapshotDir(currentDT, path string) string {
	dirs, _ := filepath.Glob(path + "/*")
	dirs_orig := []string{path + "/home", path + "/opt", path + "/rootfs", path + "/var"}

	if reflect.DeepEqual(dirs, dirs_orig) {
		if len(dirs) != 0 {
			return dirs[len(dirs)-1]
		}

	}

	return ""
}
