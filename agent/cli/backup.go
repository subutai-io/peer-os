package lib

import (
	"bufio"
	"io/ioutil"
	"os"
	"os/exec"
	"path"
	"strconv"
	"strings"
	"subutai/config"
	"subutai/lib/fs"
	"subutai/lib/template"
	"subutai/log"
	"time"
)

func BackupContainer(container string, full bool) {
	const backupDir = "/mnt/backups/"
	var changelog []string

	currentDT := strconv.Itoa(int(time.Now().Unix()))
	if full {
		currentDT = currentDT + "_Full"
	}

	tmpBackupDir := config.Agent.LxcPrefix + "lxc-data/tmpdir/" + container + "_" + currentDT + "/"
	tarballName := backupDir + container + "_" + currentDT + ".tar.gz"
	changelogName := backupDir + container + "_" + currentDT + "_changelog.txt"

	for _, subv := range GetContainerMountPoints(container) {
		subvBase := path.Base(subv) // ubuntu64efd4c3be6345f3a0deb57e3c7016be-home

		subvBaseMountpoint := ""
		if slice := strings.Split(subvBase, "-"); len(slice) > 1 {
			subvBaseMountpoint = slice[len(slice)-1]
		} else {
			subvBaseMountpoint = strings.Join(slice, "")
		}

		subvBaseMountpointPath := "/" + subvBaseMountpoint // /home

		if subvBase == "rootfs" {
			subvBase = container + "-" + subvBase // ubuntu64efd4c3be6345f3a0deb57e3c7016be-rootfs
			subvBaseMountpointPath = ""
		}

		log.Check(log.FatalLevel, "Create Backup tmp dir",
			os.MkdirAll(tmpBackupDir, 0755))

		// log.Check(log.DebugLevel, "btrfs"+"subvolume"+"delete"+config.Agent.BackupPrefix+subvBase+"@yesterday",
		// 	exec.Command("btrfs", "subvolume", "delete", config.Agent.BackupPrefix+subvBase+"@yesterday").Run())
		fs.SubvolumeDestroy(backupDir + subvBase + "@yesterday")

		log.Check(log.DebugLevel, "Rename @today snapshot to @yesterday - "+subv,
			exec.Command("mv", backupDir+subvBase+"@today", backupDir+subvBase+"@yesterday").Run())

		// log.Check(log.FatalLevel, "btrfs"+"subvolume"+"snapshot"+"-r"+string(subv)+config.Agent.BackupPrefix+subvBase+"@today",
		// 	exec.Command("btrfs", "subvolume", "snapshot", "-r", string(subv), config.Agent.BackupPrefix+subvBase+"@today").Run())
		fs.SubvolumeClone(string(subv), backupDir+subvBase+"@today")
		fs.SetVolReadOnly(backupDir+subvBase+"@today", true)

		if full {
			// log.Check(log.FatalLevel, "btrfs"+"send"+"-f"+tmpBackupDir+string(subvBaseMountpoint)+config.Agent.BackupPrefix+subvBase+"@today",
			// 	exec.Command("btrfs", "send", "-f", tmpBackupDir+string(subvBaseMountpoint), config.Agent.BackupPrefix+subvBase+"@today").Run())
			fs.Send(backupDir+subvBase+"@today", backupDir+subvBase+"@today",
				tmpBackupDir+string(subvBaseMountpoint)+".delta")
		} else {
			// log.Check(log.FatalLevel, "btrfs"+"send"+"-f"+tmpBackupDir+string(subvBaseMountpoint)+"-p"+config.Agent.BackupPrefix+subvBase+"@yesterday"+config.Agent.BackupPrefix+subvBase+"@today",
			// 	exec.Command("btrfs", "send", "-f", tmpBackupDir+string(subvBaseMountpoint), "-p", config.Agent.BackupPrefix+subvBase+"@yesterday", config.Agent.BackupPrefix+subvBase+"@today").Run())
			fs.Send(backupDir+subvBase+"@yesterday", backupDir+subvBase+"@today",
				tmpBackupDir+string(subvBaseMountpoint)+".delta")
		}

		changelog = append(changelog, GetModifiedList(backupDir+subvBase+"@today/", backupDir+subvBase+"@yesterday/", subvBaseMountpointPath+"/")...)
	}

	log.Check(log.FatalLevel, "Copy meta files",
		exec.Command("rsync", "-av", `--exclude`, `/rootfs`, config.Agent.LxcPrefix+container+"/", tmpBackupDir+"meta").Run())
	// if full don't generate changelog
	if !full {
		log.Check(log.FatalLevel, "Create Changelog file on tmpdir",
			ioutil.WriteFile(changelogName, []byte(strings.Join(changelog, "\n")), 0644))
	}

	template.Tar(tmpBackupDir, tarballName)

	log.Check(log.FatalLevel, "Remove tmpdir", os.RemoveAll(tmpBackupDir))
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
