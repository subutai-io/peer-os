package lib

import (
	"github.com/subutai-io/Subutai/agent/config"
	"github.com/subutai-io/Subutai/agent/lib/container"
	"github.com/subutai-io/Subutai/agent/log"
	"os"
)

//LxcRename renames the container
func LxcRename(src, dst string) {
	container.Stop(src)

	err := os.Rename(config.Agent.LxcPrefix+src, config.Agent.LxcPrefix+dst)
	log.Check(log.FatalLevel, "Renaming container "+src, err)

	err = os.Rename(config.Agent.LxcPrefix+src+"/opt", config.Agent.LxcPrefix+dst+"/opt")
	log.Check(log.FatalLevel, "Renaming opt for container "+src, err)

	err = os.Rename(config.Agent.LxcPrefix+src+"/home", config.Agent.LxcPrefix+dst+"/home")
	log.Check(log.FatalLevel, "Renaming home for container "+src, err)

	err = os.Rename(config.Agent.LxcPrefix+src+"/var", config.Agent.LxcPrefix+dst+"/var")
	log.Check(log.FatalLevel, "Renaming var for container "+src, err)

	container.SetContainerConf(dst, [][]string{
		{"lxc.utsname", dst},
		{"subutai.git.branch", dst},
		{"lxc.mount", config.Agent.LxcPrefix + dst + "/fstab"},
		{"lxc.rootfs", config.Agent.LxcPrefix + dst + "/rootfs"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + dst + "/opt  opt none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + dst + "/home  opt none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + dst + "/var  opt none bind,rw 0 0"},
	})

	container.Start(dst)
}
