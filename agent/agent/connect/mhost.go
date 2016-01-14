package connect

import (
	"crypto/tls"
	"io/ioutil"
	"net/http"
	"subutai/config"
	"subutai/log"
	"time"
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

func (m *mhost) GetToken() string {
	log.Debug("Mhost is " + m.Ipv4 + ":" + m.Port)

	tr := &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true}}
	client := &http.Client{Transport: tr}
	resp, err := client.Get("https://" + m.Ipv4 + ":" + m.Port + m.Url + "?username=" + config.Management.Login + "&password=" + config.Management.Password)
	if log.Check(log.WarnLevel, "Getting security token", err) {
		return ""
	}
	defer resp.Body.Close()

	token, _ := ioutil.ReadAll(resp.Body)
	return string(token)
}

func (m *mhost) GetKey() *Key {
	tr := &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true}}
	client := &http.Client{Transport: tr}
	resp, err := client.Get(m.URL())
	log.Check(log.WarnLevel, "Getting Management host Public Key", err)
	defer resp.Body.Close()

	if resp.StatusCode != 200 {
		log.Warn("Failed to fetch PK from Management Server. Status Code " + string(resp.StatusCode) + " url " + m.URL())
		return nil
	}

	key, err := ioutil.ReadAll(resp.Body)
	log.Check(log.WarnLevel, "Getting MH Key", err)
	target := new(Key)
	target.Key = string(key)

	return target
}

func (m *mhost) URL() string {
	token := m.GetToken()
	for len(token) != 168 {
		time.Sleep(time.Second * 5)
		token = m.GetToken()
	}
	url := "https://" + m.Ipv4 + ":" + m.Port + config.Management.RestPublicKey + "?sptoken=" + token
	return url
}
