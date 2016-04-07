package lib

import (
	"fmt"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/fs"
	"github.com/subutai-io/base/agent/log"
)

// LxcQuota sets quotas for containers
func LxcQuota(name, res, size string) {
	switch res {
	case "network":
		fmt.Println(container.QuotaNet(name, size))
	case "rootfs":
		fmt.Println(fs.Quota(name+"/rootfs", size))
	case "home":
		fmt.Println(fs.Quota(name+"/home", size))
	case "var":
		fmt.Println(fs.Quota(name+"/var", size))
	case "opt":
		fmt.Println(fs.Quota(name+"/opt", size))
	case "disk":
		fmt.Println(fs.DiskQuota(name, size))
	case "cpuset":
		fmt.Println(container.QuotaCPUset(name, size))
	case "ram":
		fmt.Println(container.QuotaRAM(name, size))
	case "cpu":
		fmt.Println(container.QuotaCPU(name, size))
	}
	return
}

// QuotaThreshold sets threshold for quota alerts
func QuotaThreshold(name, resource, size string) {
	if resource == "rootfs" || resource == "var" || resource == "opt" || resource == "home" {
		container.SetContainerConf(name, [][]string{{"subutai.alert.disk." + resource, size}})
		return
	} else if resource == "cpu" || resource == "ram" {
		container.SetContainerConf(name, [][]string{{"subutai.alert." + resource, size}})
		return
	}
	log.Fatal("Failed to set threshold for " + resource)
}
