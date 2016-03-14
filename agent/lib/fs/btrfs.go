package fs

import (
	"bufio"
	"bytes"
	"os"
	"os/exec"
	"strconv"
	"strings"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/log"
)

func IsSubvolumeReadonly(path string) bool {
	out, _ := exec.Command("btrfs", "property", "get", "-ts", path).Output()
	if strings.Contains(string(out), "true") {
		return true
	}
	return false
}

func SubvolumeCreate(dst string) {
	if id(dst) == "" {
		out, err := exec.Command("btrfs", "subvolume", "create", dst).CombinedOutput()
		log.Check(log.FatalLevel, "Creating subvolume "+dst+": "+string(out), err)
	}
}

func SubvolumeClone(src, dst string) {
	out, err := exec.Command("btrfs", "subvolume", "snapshot", src, dst).CombinedOutput()
	log.Check(log.FatalLevel, "Creating snapshot: "+string(out), err)
}

func SubvolumeDestroy(path string) {
	nestedvol, err := exec.Command("btrfs", "subvolume", "list", "-o", path).Output()
	log.Check(log.DebugLevel, "Getting nested subvolumes in "+path, err)
	scanner := bufio.NewScanner(bytes.NewReader(nestedvol))
	for scanner.Scan() {
		line := strings.Fields(scanner.Text())
		if len(line) > 8 {
			SubvolumeDestroy(GetBtrfsRoot() + line[8])
		}
	}
	qgroupDestroy(path)
	out, err := exec.Command("btrfs", "subvolume", "delete", path).CombinedOutput()
	log.Check(log.DebugLevel, "Destroying subvolume "+path+": "+string(out), err)
}

func qgroupDestroy(path string) {
	index := id(path)
	out, err := exec.Command("btrfs", "qgroup", "destroy", index, config.Agent.LxcPrefix).CombinedOutput()
	log.Check(log.DebugLevel, "Destroying qgroup "+path+" "+index+": "+string(out), err)
}

// NEED REFACTORING
func id(path string) string {
	path = strings.Replace(path, config.Agent.LxcPrefix, "", -1)
	out, _ := exec.Command("btrfs", "subvolume", "list", config.Agent.LxcPrefix).Output()
	scanner := bufio.NewScanner(bytes.NewReader(out))
	for scanner.Scan() {
		line := strings.Fields(scanner.Text())
		if len(line) > 8 {
			if strings.HasSuffix(line[8], path) {
				return line[1]
			}
		}
	}
	return ""
}

func Receive(src, dst, delta string, parent bool) {
	args := []string{"receive", "-p", src, dst}
	if !parent {
		args = []string{"receive", dst}
	}
	log.Debug(strings.Join(args, " "))
	receive := exec.Command("btrfs", args...)
	input, err := os.Open(config.Agent.LxcPrefix + "lxc-data/tmpdir/" + delta)
	receive.Stdin = input
	log.Check(log.FatalLevel, "Opening delta "+delta, err)
	out, err := receive.CombinedOutput()
	log.Check(log.FatalLevel, "Receiving delta "+delta+": "+string(out), err)
}

func Send(src, dst, delta string) {
	newdelta, err := os.Create(delta)
	log.Check(log.FatalLevel, "Creating delta "+delta, err)
	args := []string{"send", "-p", src, dst}
	if src == dst {
		args = []string{"send", dst}
	}
	send := exec.Command("btrfs", args...)
	send.Stdout = newdelta
	log.Check(log.FatalLevel, "Sending delta "+delta, send.Run())
}

func ReadOnly(container string, flag bool) {
	for _, path := range []string{container + "/rootfs/", container + "/opt", container + "/var", container + "/home"} {
		arg := []string{"property", "set", "-ts", config.Agent.LxcPrefix + path, "ro", strconv.FormatBool(flag)}
		out, err := exec.Command("btrfs", arg...).CombinedOutput()
		log.Check(log.FatalLevel, "Setting readonly: "+strconv.FormatBool(flag)+": "+string(out), err)
	}
}

func SetVolReadOnly(subvol string, flag bool) {
	arg := []string{"property", "set", "-ts", subvol, "ro", strconv.FormatBool(flag)}
	out, err := exec.Command("btrfs", arg...).CombinedOutput()
	log.Check(log.FatalLevel, "Setting readonly: "+strconv.FormatBool(flag)+": "+string(out), err)
}

func Stat(path, index string, raw bool) string {
	var row = map[string]int{
		"quota": 3,
		"usage": 2,
	}

	args := []string{"qgroup", "show", "-r", config.Agent.LxcPrefix}
	if raw {
		args = []string{"qgroup", "show", "-r", "--raw", config.Agent.LxcPrefix}
	}
	out, err := exec.Command("btrfs", args...).Output()
	log.Check(log.FatalLevel, "Getting btrfs stats", err)
	ind := id(path)
	scanner := bufio.NewScanner(bytes.NewReader(out))
	for scanner.Scan() {
		line := strings.Fields(scanner.Text())
		if len(line) > 3 {
			if line[0] == "0/"+ind {
				return line[row[index]]
			}
		}
	}
	return ""
}

func DiskQuota(path string, size ...string) string {
	parent := id(path)
	exec.Command("btrfs", "qgroup", "create", "1/"+parent, config.Agent.LxcPrefix+path).Run()
	for _, subvol := range []string{"/rootfs", "/opt", "/var", "/home"} {
		index := id(path + subvol)
		exec.Command("btrfs", "qgroup", "assign", "0/"+index, "1/"+parent, config.Agent.LxcPrefix+path).Run()
	}
	if size != nil {
		exec.Command("btrfs", "qgroup", "limit", size[0]+"G", "1/"+parent, config.Agent.LxcPrefix+path).Run()
	}
	return Stat(path, "quota", false)
}

func Quota(path string, size ...string) string {
	if size != nil {
		exec.Command("btrfs", "qgroup", "limit", size[0]+"G", config.Agent.LxcPrefix+path).Run()
	}
	return Stat(path, "quota", false)
}

func GetContainerUUID(contanierName string) string {
	var uuid string
	result, err := exec.Command("btrfs", "subvolume", "list", "-u", config.Agent.LxcPrefix).CombinedOutput()
	if err != nil {
		log.Error("btrfs command execute", err.Error())
	}
	resArr := strings.Split(string(result), "\n")
	for _, r := range resArr {
		if strings.Contains(r, contanierName+"/rootfs") {
			rArr := strings.Fields(r)
			uuid = rArr[8]
		}

	}
	return uuid
}

func GetChildren(uuid string) []string {
	var child []string
	result, err := exec.Command("btrfs", "subvolume", "list", "-q", config.Agent.LxcPrefix).CombinedOutput()
	if err != nil {
		log.Error("btrfs -q command execute", err.Error())
	}
	resultArr := strings.Split(string(result), "\n")
	for _, v := range resultArr {
		if strings.Contains(v, uuid) {
			vArr := strings.Fields(v)
			child = append(child, vArr[10])
		}
	}
	return child
}

// GetBtrfsRoot	returns BTRFS root
func GetBtrfsRoot() string {
	data, err := exec.Command("findmnt", "-nT", config.Agent.LxcPrefix).Output()
	log.Check(log.FatalLevel, "Find btrfs mount point", err)

	line := strings.Fields(string(data))
	return (line[0] + "/")
}
