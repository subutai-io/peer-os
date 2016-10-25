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

	"github.com/subutai-io/base/agent/config"
	lxcContainer "github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/fs"
	"github.com/subutai-io/base/agent/lib/template"
	"github.com/subutai-io/base/agent/log"
)

// BackupContainer takes a snapshots of each container's volume and stores it in the /mnt/backups/container_name/datetime/ directory.
// A full backup creates a delta-file of each BTRFS subvolume. An incremental backup (default) creates a delta-file with the difference of changes between the current and last snapshots.
// All deltas are compressed to archives in /mnt/backups/ directory (container_datetime.tar.gz or container_datetime_Full.tar.gz for full backup).
// A changelog file can be found next to backups archive (container_datetime_changelog.txt or container_datetime_Full_changelog.txt) which contains a list of changes made between two backups.
func BackupContainer(container string, full, stop bool) {
	const backupDir = "/mnt/backups/"
	var changelog []string

	if !lxcContainer.IsContainer(container) {
		log.Fatal("Container " + container + " not found!")
	}

	if _, err := os.Stat(config.Agent.LxcPrefix + container + "/.backup"); err == nil {
		log.Fatal("Backup of container " + container + " already running")
	} else {
		f, err := os.Create(config.Agent.LxcPrefix + container + "/.backup")
		log.Check(log.WarnLevel, "Creating .backup file to "+container+" container", err)
		defer f.Close()
	}

	currentDT := strconv.Itoa(int(time.Now().Unix()))
	if full {
		currentDT = currentDT + "_Full"
	}

	// create backupDir
	if _, err := os.Stat(backupDir + container); os.IsNotExist(err) {
		os.MkdirAll(backupDir, 0755)
	}

	// for easy removal of backup directory
	if _, err := os.Stat(backupDir + container); os.IsNotExist(err) {
		fs.SubvolumeCreate(backupDir + container)
	}

	lastSnapshotDir := GetLastSnapshotDir(currentDT, backupDir+container)
	log.Debug("last snapshot dir: " + lastSnapshotDir)

	if !full && lastSnapshotDir == "" {
		log.Check(log.WarnLevel, "Deleting .backup file to "+container+" container", os.Remove(config.Agent.LxcPrefix+container+"/.backup"))
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

	if stop {
		switch lxcContainer.State(container) {
		case "STOPPED":
			stop = false
		case "RUNNING":
			lxcContainer.Stop(container)
		}
	}

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
				tmpBackupDir+subvolBase+".delta")
		} else {
			fs.Send(lastSnapshotDir+"/"+subvolBase, containerSnapshotDir+"/"+subvolBase,
				tmpBackupDir+subvolBase+".delta")
		}

		if lastSnapshotDir != "" {
			changelog = append(changelog, GetModifiedList(containerSnapshotDir+"/"+subvolBase+"/",
				lastSnapshotDir+"/"+subvolBase+"/",
				subvolBaseMountpointPath+"/")...)

		}
	}

	log.Check(log.FatalLevel, "Copy meta files",
		exec.Command("rsync", "-av", `--exclude`, `/rootfs`, `--exclude`, `/home`, `--exclude`, `/opt`, `--exclude`, `/var`, config.Agent.LxcPrefix+container+"/", tmpBackupDir+"meta").Run())

	log.Check(log.FatalLevel, "Create Changelog file on tmpdir",
		ioutil.WriteFile(changelogName, []byte(strings.Join(changelog, "\n")), 0644))

	if stop {
		lxcContainer.Start(container)
	}

	template.Tar(tmpBackupDir, tarballName)

	log.Check(log.WarnLevel, "Remove tmpdir", os.RemoveAll(backupDir+"/tmpdir"))
	log.Check(log.WarnLevel, "Deleting .backup file to "+container+" container", os.Remove(config.Agent.LxcPrefix+container+"/.backup"))
}

// GetContainerMountPoints returns array of paths to all containers mountpoints
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

// GetModifiedList generates a list of changed files for backup changelog
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

// GetLastSnapshotDir returns a path to latest snapshot directory
func GetLastSnapshotDir(currentDT, path string) string {
	lastSnapshot := ""

	dirs, _ := filepath.Glob(path + "/*")
	if len(dirs) == 0 {
		return ""
	}

	lastSnapshot = dirs[len(dirs)-1]

	dirs_last, _ := filepath.Glob(lastSnapshot + "/*")
	dirs_orig := []string{lastSnapshot + "/home", lastSnapshot + "/opt", lastSnapshot + "/rootfs", lastSnapshot + "/var"}

	// check last snapshot dir
	if !reflect.DeepEqual(dirs_last, dirs_orig) {
		return ""
	}

	return lastSnapshot
}
