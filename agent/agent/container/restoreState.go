package container

import (
	"os"
	"os/exec"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/log"
)

var (
	contsStatus map[string]int
)

func init() {
	contsStatus = make(map[string]int)
}

func ContainersRestoreState(pool []Container) {
	for _, cont := range pool {
		var start, stop bool

		switch contsStatus[cont.Name] {
		case 100:
		case 5:
			{
				log.Debug("Failed to START sontainer " + cont.Name + " after 5 attempts")
				contsStatus[cont.Name] = 100
			}
		case -5:
			{
				log.Debug("Failed to STOP sontainer " + cont.Name + " after 5 attempts")
				contsStatus[cont.Name] = 100
			}
		case 10:
			{
				log.Debug(".start and .stop files exist on " + cont.Name + " cont ")
				contsStatus[cont.Name] = 100
			}
		default:
			{
				if _, err := os.Stat(config.Agent.LxcPrefix + cont.Name + "/.start"); err == nil {
					start = true
				}
				if _, err := os.Stat(config.Agent.LxcPrefix + cont.Name + "/.stop"); err == nil {
					stop = true
				}
				if start && stop {
					contsStatus[cont.Name] = 10
					break
				}
				switch {
				case start && cont.Status != "RUNNING":
					{
						log.Debug("Trying start " + cont.Name)
						exec.Command("subutai", "start", cont.Name).Run()
						contsStatus[cont.Name]++
					}
				case stop && cont.Status != "STOPPED":
					{
						log.Debug("Trying stop " + cont.Name)
						exec.Command("subutai", "stop", cont.Name).Run()
						contsStatus[cont.Name]--

					}
				default:
					contsStatus[cont.Name] = 0
				}
			}
		}
	}
}
