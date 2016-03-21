package connect

import (
	"bytes"
	"crypto/tls"
	"net/http"

	"github.com/subutai-io/base/agent/lib/gpg"
	"github.com/subutai-io/base/agent/log"
)

func Connect(host, port, user, pass string) {
	log.Debug("Connecting to " + host + ":" + port)

	mgn := Instance()
	mgn.Ipv4 = host
	mgn.Port = port

	rh := NewRH()
	rh.Pk = gpg.GetPk(user)
	rh.UUID = gpg.GetFingerprint(user)
	rh.Secret = pass

	log.Info(rh.Json())

	enMsg := ""
	pk := mgn.GetKey()
	if pk == nil {
		return
	}
	pk.Store()
	enMsg = gpg.EncryptWrapper(user, pk.Id, rh.Json())

	tr := &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true}}
	client := &http.Client{Transport: tr}

	resp, err := client.Post(mgn.URL(), "text/plain", bytes.NewBuffer([]byte(enMsg)))
	if !log.Check(log.WarnLevel, "POSTing request to "+mgn.URL(), err) {
		resp.Body.Close()
	}
}
