package lib

import (
	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/log"
	"os"
)

// LxcRename renames a Subutai container impacting filesystem paths, configuration values, etc.
func LxcRename(src, dst string) {
	run := false
	if container.State(src) == "RUNNING" {
		run = true
		container.Stop(src)
	}

	err := os.Rename(config.Agent.LxcPrefix+src, config.Agent.LxcPrefix+dst)
	log.Check(log.FatalLevel, "Renaming container "+src, err)

	container.SetContainerConf(dst, [][]string{
		{"lxc.utsname", dst},
		{"subutai.git.branch", dst},
		{"lxc.mount", config.Agent.LxcPrefix + dst + "/fstab"},
		{"lxc.rootfs", config.Agent.LxcPrefix + dst + "/rootfs"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + dst + "/opt  opt none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + dst + "/home  opt none bind,rw 0 0"},
		{"lxc.mount.entry", config.Agent.LxcPrefix + dst + "/var  opt none bind,rw 0 0"},
	})

	if run {
		container.Start(dst)
	}

	log.Info("Container " + src + " successfully renamed to " + dst)
}
