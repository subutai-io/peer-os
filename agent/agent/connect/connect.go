package connect

import (
	"bytes"
	"crypto/tls"
	"net/http"
	"os"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/gpg"
	"github.com/subutai-io/base/agent/log"
)

func Connect(host, port, user, pass string) {
	log.Debug("Connecting to " + host + ":" + port)

	mng := Instance()
	mng.Ipv4 = host
	mng.Port = port

	rh := NewRH()
	rh.Pk = gpg.GetPk(user)
	rh.UUID = gpg.GetFingerprint(user)
	rh.Secret = pass

	log.Info(rh.Json())

	pk := mng.GetKey()
	if pk == nil {
		return
	}
	pk.Store()

	hostname, _ := os.Hostname()
	config.Agent.GpgUser = hostname + "@subutai.io"
	config.Management.GpgUser = pk.ExtractKeyID()

	enMsg := gpg.EncryptWrapper(user, config.Management.GpgUser, rh.Json())

	client := &http.Client{Transport: &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true}}}

	resp, err := client.Post(mng.URL(), "text/plain", bytes.NewBuffer([]byte(enMsg)))
	if !log.Check(log.WarnLevel, "POSTing request to "+mng.URL(), err) {
		resp.Body.Close()
	}
}
