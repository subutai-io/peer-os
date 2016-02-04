package lib

import (
	"github.com/subutai-io/Subutai/agent/lib/container"
	"github.com/subutai-io/Subutai/agent/lib/template"
	"github.com/subutai-io/Subutai/agent/log"
)

func LxcDestroy(name string) {
	if !container.IsContainer(name) {
		log.Error(name + " doesn't exist")
	}
	container.Destroy(name)

	if name == "management" {
		template.MngDel()
	}
}
