package lib

import (
	"fmt"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/fs"
)

// LxcQuota sets quotas for containers
func LxcQuota(name, res, size string) {
	switch res {
	case "networkRate":
		fmt.Println(container.QuotaNet(name, size))
	case "diskRootfs":
		fmt.Println(fs.Quota(name+"/rootfs", size))
	case "diskHome":
		fmt.Println(fs.Quota(name+"/home", size))
	case "diskVar":
		fmt.Println(fs.Quota(name+"/var", size))
	case "diskOpt":
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
