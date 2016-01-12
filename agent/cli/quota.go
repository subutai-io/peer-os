package lib

import (
	"fmt"
	"subutai/lib/container"
	"subutai/lib/fs"
)

// LxcQuota sets quotas for containers
func LxcQuota(name, res, size, max string) {
	switch res {
	case "networkRate":
		fmt.Println(container.QuotaNet(name, size))
	case "diskRootfs":
		fmt.Println(fs.Quota(name+"/rootfs", size))
	case "diskHome":
		fmt.Println(fs.Quota("lxc-data/"+name+"-home", size))
	case "diskVar":
		fmt.Println(fs.Quota("lxc-data/"+name+"-var", size))
	case "diskOpt":
		fmt.Println(fs.Quota("lxc/"+name+"-opt", size))
	case "cpuset":
		fmt.Println(container.QuotaCPUset(name, size))
	case "ram":
		fmt.Println(container.QuotaRAM(name, size))
	case "cpu":
		fmt.Println(container.QuotaCPU(name, size))
	}
	return
}
