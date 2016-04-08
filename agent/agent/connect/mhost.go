package connect

import (
	"crypto/tls"
	"io/ioutil"
	"net/http"
	"strconv"
	"time"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/gpg"
	"github.com/subutai-io/base/agent/log"
)

type mhost struct {
	Ipv4 string
	Url  string
	Port string
}

var instance *mhost

//return existing instance
//if dne create new one
func Instance() *mhost {
	if instance == nil {
		instance = new(mhost)
		instance.Url = config.Management.RestPublicKey
		instance.Ipv4 = config.Management.Host
		instance.Port = config.Management.Port
		instance.Url = config.Management.RestToken
	}
	return instance
}

func (m *mhost) GetKey() *Key {
	tr := &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true}}
	client := &http.Client{Transport: tr, Timeout: time.Second * 5}
	url := m.URL()
	resp, err := client.Get(url)
	if log.Check(log.WarnLevel, "Getting Management host Public Key", err) {
		return nil
	}
	defer resp.Body.Close()

	if resp.StatusCode != 200 {
		log.Warn("Failed to fetch PK from Management Server. Status Code " + strconv.Itoa(resp.StatusCode) + " url " + url)
		return nil
	}

	key, err := ioutil.ReadAll(resp.Body)
	log.Check(log.WarnLevel, "Getting MH Key", err)
	target := new(Key)
	target.Key = string(key)

	return target
}

func (m *mhost) URL() string {
	url := "https://" + m.Ipv4 + ":" + m.Port + config.Management.RestPublicKey + "?sptoken="
	token := gpg.GetToken()
	if len(token) == 168 {
		return url + token
	}
	return url
}
