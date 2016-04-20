package connect

import (
	"bytes"
	"crypto/tls"
	"encoding/json"
	"io/ioutil"
	"net/http"
	"os"
	"os/exec"
	"runtime"
	"strconv"
	"strings"
	"time"

	"github.com/subutai-io/base/agent/agent/container"
	"github.com/subutai-io/base/agent/agent/utils"
	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/gpg"
	"github.com/subutai-io/base/agent/log"
)

type rHost struct {
	UUID       string                `json:"id"`
	Hostname   string                `json:"hostname"`
	Pk         string                `json:"publicKey"`
	Cert       string                `json:"cert"`
	Secret     string                `json:"secret"`
	Ifaces     []utils.Iface         `json:"interfaces"`
	Arch       string                `json:"arch"`
	Containers []container.Container `json:"hostInfos"`
}

func Connect(user, pass string) {
	log.Debug("Connecting to " + config.Management.Host + ":" + config.Management.Port)
	hostname, _ := os.Hostname()

	rh, err := json.Marshal(rHost{
		Hostname:   hostname,
		Secret:     pass,
		Pk:         gpg.GetPk(user),
		UUID:       gpg.GetFingerprint(user),
		Arch:       strings.ToUpper(runtime.GOARCH),
		Cert:       utils.PublicCert(),
		Ifaces:     utils.GetInterfaces(),
		Containers: container.GetActiveContainers(true),
	})
	log.Check(log.WarnLevel, "Marshal Resource host json: "+string(rh), err)

	url := "https://" + config.Management.Host + ":" + config.Management.Port + config.Management.RestPublicKey + "?sptoken=" + gpg.GetToken()
	pk := getKey(url)
	if len(pk) == 0 {
		return
	}
	gpg.ImportPk(pk)

	config.Management.GpgUser = extractKeyID(pk)

	client := &http.Client{Transport: &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true}}}
	resp, err := client.Post(url, "text/plain",
		bytes.NewBuffer([]byte(gpg.EncryptWrapper(user, config.Management.GpgUser, string(rh)))))

	if !log.Check(log.WarnLevel, "POSTing request to "+url, err) {
		resp.Body.Close()
	}
}

func getKey(url string) string {
	tr := &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true}}
	client := &http.Client{Transport: tr, Timeout: time.Second * 5}
	resp, err := client.Get(url)
	if log.Check(log.WarnLevel, "Getting Management host Public Key", err) {
		return ""
	}
	defer resp.Body.Close()

	if resp.StatusCode == 200 {
		key, _ := ioutil.ReadAll(resp.Body)
		return string(key)
	}

	log.Warn("Failed to fetch PK from Management Server. Status Code " + strconv.Itoa(resp.StatusCode) + " url " + url)
	return ""
}

func extractKeyID(k string) string {
	command := exec.Command("gpg")
	stdin, err := command.StdinPipe()
	stdin.Write([]byte(k))
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
