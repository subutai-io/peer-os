package connect

import (
	"bufio"
	"bytes"
	"io/ioutil"
	"os"
	"os/exec"
	"strings"
	"subutai/agent/utils"
	"subutai/log"
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

	status := utils.ImportPk("epub.key")
	os.Remove("epub.key")
	k.Id = ExtractKeyId(status)
	log.Debug("Found KeyID: " + k.Id)

	return status
}

func (k *Key) ExtractKeyEmail() string {
	command := exec.Command("gpg")
	stdin, err := command.StdinPipe()
	stdin.Write([]byte(k.String()))
	stdin.Close()

	out, err := command.Output()
	log.Check(log.WarnLevel, "Extracting Email from Key", err)

	scanner := bufio.NewScanner(bytes.NewReader(out))
	for scanner.Scan() {
		if strings.Contains(scanner.Text(), "uid") {
			line := strings.Fields(scanner.Text())
			if len(line) > 1 {
				email := line[len(line)-1]
				return email
			}
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

func ExtractKeyId(s string) string {
	arr := strings.Split(s, " ")
	for i, word := range arr {
		if word == "key" {
			return arr[i+1][0 : len(arr[i+1])-1]
		}
	}
	return ""
}
