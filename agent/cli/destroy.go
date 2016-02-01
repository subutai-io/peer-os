package lib

import (
	"github.com/subutai-io/Subutai/agent/lib/container"
	"github.com/subutai-io/Subutai/agent/lib/template"
	"github.com/subutai-io/Subutai/agent/log"
)

func LxcDestroy(c string) {
	if !container.IsContainer(c) {
		log.Error(c + " doesn't exist")
	}
	container.Destroy(c)

	if c == "management" {
		template.MngDel()
	}
}
