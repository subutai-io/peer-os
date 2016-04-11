package lib

import (
	"fmt"
	"strconv"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/fs"
	"github.com/subutai-io/base/agent/log"
)

// LxcQuota sets quotas for containers
func LxcQuota(name, res, size, threshold string) {
	if len(threshold) > 0 {
		setQuotaThreshold(name, res, threshold)
	}
	quota := "0"
	alert := getQuotaThreshold(name, res)
	switch res {
	case "network":
		quota = container.QuotaNet(name, size)
	case "rootfs", "home", "var", "opt":
		quota = fs.Quota(name+"/"+res, size)
		if quota == "none" {
			quota = "0"
		}
	case "disk":
		quota = fs.DiskQuota(name, size)
	case "cpuset":
		quota = container.QuotaCPUset(name, size)
	case "ram":
		quota = strconv.Itoa(container.QuotaRAM(name, size))
	case "cpu":
		quota = strconv.Itoa(container.QuotaCPU(name, size))
	}
	fmt.Println(`{"quota":` + quota + `, "threshold":` + alert + `}`)

}

// setQuotaThreshold sets threshold for quota alerts
func setQuotaThreshold(name, resource, size string) {
	if resource == "rootfs" || resource == "var" || resource == "opt" || resource == "home" {
		container.SetContainerConf(name, [][]string{{"subutai.alert.disk." + resource, size}})
		return
	} else if resource == "cpu" || resource == "ram" {
		container.SetContainerConf(name, [][]string{{"subutai.alert." + resource, size}})
		return
	}
	log.Fatal("Failed to set threshold for " + resource)
}

// getQuotaThreshold gets threshold of quota alerts
func getQuotaThreshold(name, resource string) string {
	res := "subutai.alert.disk." + resource
	if resource == "cpu" || resource == "ram" {
		res = "subutai.alert." + resource
	}
	if size := container.GetConfigItem(config.Agent.LxcPrefix+name+"/config", res); len(size) > 0 {
		return size
	}
	return "0"
}
