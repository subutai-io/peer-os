package connect

import (
	"io/ioutil"
	"os"
	"os/exec"
	"strings"

	"github.com/subutai-io/base/agent/lib/gpg"
	"github.com/subutai-io/base/agent/log"
)

type Key struct {
	Id  string `json:"-"`
	Key string `json:"key"`
}

func (k *Key) String() string {
	return k.Key
}

//add public key to local keyring
//call Get() before calling Store()
func (k *Key) Store() string {
	err := k.Write("epub.key")
	log.Check(log.WarnLevel, "Adding Public Key(PK) to local keyring", err)

	status := gpg.ImportPk("epub.key")
	os.Remove("epub.key")
	return status
}

func (k *Key) ExtractKeyID() string {
	command := exec.Command("gpg")
	stdin, err := command.StdinPipe()
	stdin.Write([]byte(k.String()))
	stdin.Close()
	out, err := command.Output()
	log.Check(log.WarnLevel, "Extracting ID from Key", err)

	if line := strings.Fields(string(out)); len(line) > 1 {
		if key := strings.Split(line[1], "/"); len(key) > 1 {
			return key[1]
		}
	}
	return ""
}

//write pubkey to temp file
func (k *Key) Write(file string) error {
	err := ioutil.WriteFile(file, []byte(k.String()), 0644)
	if log.Check(log.WarnLevel, "Writing Pubkey to temp file", err) {
		return err
	}
	return nil
}

func (k *Key) Remove(file string) error {
	err := os.Remove(file)
	if log.Check(log.WarnLevel, "Removing file"+file, err) {
		return err
	}
	return nil
}
