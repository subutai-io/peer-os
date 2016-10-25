package lib

import (
	"encoding/json"
	"fmt"
	"os/exec"
	"strings"

	"github.com/subutai-io/base/agent/log"
)

type batchLine struct {
	Action string   `json:"action"`
	Args   []string `json:"args"`
}
type outputLine struct {
	Output   string `json:"output"`
	ExitCode string `json:"exitcode"`
}

// Batch binding provides a mechanism to perform several Subutai commands in the container in batch,
// passed in a single JSON message. Initially, the purpose of this command was internal for SS <-> Agent communication,
// yet it may be invoked manually from the CLI.
// The response from a batch command returns a JSON array with each element representing the results (response) from each command (request) in the batch:
// the positions of responses correlate with the request position in the array
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
