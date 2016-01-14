package lib

import (
	"subutai/lib/container"
	"subutai/log"
)

func LxcDestroy(c string) {
	if !container.IsContainer(c) {
		log.Error(c + " doesn't exist")
	}
	container.Destroy(c)
}
