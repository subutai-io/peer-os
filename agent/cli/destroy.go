package lib

import (
	"subutai/lib/container"
	"subutai/lib/template"
	"subutai/log"
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
