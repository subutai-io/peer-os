package lib

import (
	"encoding/json"
	"fmt"
	"github.com/subutai-io/Subutai/agent/log"
	"os/exec"
	"strings"
)

type batchLine struct {
	Action string   `json:"action"`
	Args   []string `json:"args"`
}
type outputLine struct {
	Output   string `json:"output"`
	ExitCode string `json:"exitcode"`
}

func Batch(data string) {
	var jsonBlob = []byte(data)
	var list []batchLine
	err := json.Unmarshal(jsonBlob, &list)
	log.Check(log.ErrorLevel, "Unmarshal JSON", err)

	var output []outputLine
	var cmdout outputLine

	for _, item := range list {
		args := append([]string{item.Action}, item.Args...)
		out, err := exec.Command("subutai", args...).CombinedOutput()
		cmdout.ExitCode = "0"
		cmdout.Output = string(out)
		if err != nil {
			exitcode := strings.Fields(err.Error())
			cmdout.ExitCode = exitcode[len(exitcode)-1]
			output = append(output, cmdout)
			break
		}
		output = append(output, cmdout)
	}

	result, err := json.Marshal(output)
	fmt.Println(string(result))
}
