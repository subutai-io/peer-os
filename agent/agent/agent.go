package agent

import (
	"encoding/json"
	"io/ioutil"
	"net/http"
	"net/url"
	"os"
	"runtime"
	// "strconv"
	"strings"
	"sync"
	"time"

	"github.com/codegangsta/cli"

	"github.com/subutai-io/base/agent/agent/alert"
	"github.com/subutai-io/base/agent/agent/connect"
	"github.com/subutai-io/base/agent/agent/container"
	"github.com/subutai-io/base/agent/agent/executer"
	"github.com/subutai-io/base/agent/agent/utils"
	"github.com/subutai-io/base/agent/cli"
	"github.com/subutai-io/base/agent/config"
	cont "github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/lib/gpg"
	"github.com/subutai-io/base/agent/log"
)

type Response struct {
	Beat Heartbeat `json:"response"`
}

type Heartbeat struct {
	Type       string                `json:"type"`
	Hostname   string                `json:"hostname"`
	Id         string                `json:"id"`
	Arch       string                `json:"arch"`
	Instance   string                `json:"instance"`
	Interfaces []utils.Iface         `json:"interfaces,omitempty"`
	Containers []container.Container `json:"containers"`
	Alert      []alert.Load          `json:"alert, omitempty"`
}

var (
	lastHeartbeat []byte
	mutex         sync.Mutex
)

func initAgent() {
	// move .gnupg dir to app home
	os.Setenv("GNUPGHOME", config.Agent.DataPrefix+".gnupg")

	if cont.State("management") == "STOPPED" {
		cont.Start("management")
	}
	container.PoolInstance()
	Instance()
}

func Start(c *cli.Context) {
	http.HandleFunc("/trigger", trigger)
	http.HandleFunc("/ping", ping)
	go http.ListenAndServe(":7070", nil)

	go container.ContainersRestoreState()
	go lib.Collect()
	initAgent()

	for {
		// log.Debug("NumGoroutine " + strconv.Itoa(runtime.NumGoroutine()))
		Instance()
		if heartbeat() {
			time.Sleep(30 * time.Second)
		} else {
			time.Sleep(5 * time.Second)
		}
	}
}

func heartbeat() bool {
	mutex.Lock()
	defer mutex.Unlock()

	client := tlsConfig()
	hostname, _ := os.Hostname()
	instancetype := utils.InstanceType()
	fingerprint := gpg.GetFingerprint(hostname + "@subutai.io")

	list := container.GetActiveContainers(false)

	beat := Heartbeat{
		Type:       "HEARTBEAT",
		Hostname:   hostname,
		Id:         fingerprint,
		Arch:       strings.ToUpper(runtime.GOARCH),
		Instance:   instancetype,
		Containers: list,
		Interfaces: utils.GetInterfaces(),
		Alert:      alert.Alert(list),
	}
	res := Response{Beat: beat}
	jbeat, _ := json.Marshal(&res)

	if string(jbeat) == string(lastHeartbeat) {
		return true
	}
	lastHeartbeat = jbeat

	message, err := json.Marshal(map[string]string{
		"hostId":   fingerprint,
		"response": gpg.EncryptWrapper(config.Agent.GpgUser, config.Management.GpgUser, string(jbeat)),
	})
	log.Check(log.WarnLevel, "Marshal response json", err)

	resp, err := client.PostForm("https://"+config.Management.Host+":8444/rest/v1/agent/heartbeat", url.Values{"heartbeat": {string(message)}})
	if !log.Check(log.WarnLevel, "Sending heartbeat: "+string(jbeat), err) {
		resp.Body.Close()
	} else {
		connect.Connect(config.Management.Host, config.Management.Port, config.Agent.GpgUser, config.Management.Secret)
		return false
	}
	return true
}

func execute(rsp executer.EncRequest) {
	var req executer.Request
	var md, contName, pub, keyring, payload string
	var flag bool
	var err error

	hostname, _ := os.Hostname()
	fingerprint := gpg.GetFingerprint(hostname + "@subutai.io")

	if rsp.HostId == fingerprint {
		flag = true
		md = gpg.DecryptWrapper(rsp.Request)
	} else {
		flag = false
		contName, err = container.PoolInstance().GetTargetHostName(rsp.HostId)
		counter := 0
		for err != nil {
			contName, err = container.PoolInstance().GetTargetHostName(rsp.HostId)
			time.Sleep(time.Second * 3)
			if counter = counter + 1; counter == 100 {
				log.Warn("Container wait timeout: " + contName)
				return
			}
		}

		pub = config.Agent.LxcPrefix + contName + "/public.pub"
		keyring = config.Agent.LxcPrefix + contName + "/secret.sec"
		log.Info("Getting public keyring", "keyring", keyring)
		md = gpg.DecryptNoDefaultKeyring(rsp.Request, keyring, pub)
	}

	i := strings.Index(md, "{")
	j := strings.LastIndex(md, "}") + 1
	if i > j && i > 0 {
		log.Warn("Error getting JSON request")
		return
	}
	request := md[i:j]

	err = json.Unmarshal([]byte(request), &req.Request)
	log.Check(log.WarnLevel, "Decrypting request", err)

	//create channels for stdout and stderr
	sOut := make(chan executer.ResponseOptions)
	req.Execute(flag, sOut)

	for sOut != nil {
		select {
		case elem, ok := <-sOut:
			if ok {
				resp := executer.Response{ResponseOpts: elem}
				jsonR, err := json.Marshal(resp)
				log.Check(log.WarnLevel, "Marshal response", err)
				if rsp.HostId == fingerprint {
					payload = gpg.EncryptWrapper(config.Agent.GpgUser, config.Management.GpgUser, string(jsonR))
				} else {
					payload = gpg.EncryptWrapperNoDefaultKeyring(contName, config.Management.GpgUser, string(jsonR), pub, keyring)
				}
				message, err := json.Marshal(map[string]string{
					"hostId":   elem.Id,
					"response": payload,
				})
				log.Check(log.WarnLevel, "Marshal response json "+elem.CommandId, err)
				response(message)
			} else {
				sOut = nil
			}
		}
	}
	heartbeat()
}

func tlsConfig() *http.Client {
	tlsconfig := newTLSConfig()
	for tlsconfig == nil || len(tlsconfig.Certificates[0].Certificate) == 0 {
		time.Sleep(time.Second * 2)
		for utils.PublicCert() == "" {
			x509generate()
		}
		tlsconfig = newTLSConfig()
	}

	transport := &http.Transport{TLSClientConfig: tlsconfig}
	client := &http.Client{Transport: transport}
	return client
}

func response(msg []byte) {
	client := tlsConfig()
	resp, err := client.PostForm("https://"+config.Management.Host+":8444/rest/v1/agent/response", url.Values{"response": {string(msg)}})
	if !log.Check(log.WarnLevel, "Sending response "+string(msg), err) {
		resp.Body.Close()
	}
}

func command() {
	var rsp []executer.EncRequest
	client := tlsConfig()
	hostname, _ := os.Hostname()
	fingerprint := gpg.GetFingerprint(hostname + "@subutai.io")

	resp, err := client.Get("https://" + config.Management.Host + ":8444/rest/v1/agent/requests/" + fingerprint)
	if log.Check(log.WarnLevel, "Getting requests", err) {
		return
	}
	defer resp.Body.Close()
	if resp.StatusCode == http.StatusNoContent {
		return
	}

	data, err := ioutil.ReadAll(resp.Body)
	if !log.Check(log.WarnLevel, "Reading body", err) {
		log.Check(log.WarnLevel, "Unmarshal payload", json.Unmarshal(data, &rsp))
		for _, request := range rsp {
			execute(request)
		}
	}
}

func ping(rw http.ResponseWriter, request *http.Request) {
	if request.Method == http.MethodGet && strings.Split(request.RemoteAddr, ":")[0] == config.Management.Host {
		rw.WriteHeader(http.StatusOK)
	} else {
		rw.WriteHeader(http.StatusForbidden)
	}
}

func trigger(rw http.ResponseWriter, request *http.Request) {
	if request.Method == http.MethodPost && strings.Split(request.RemoteAddr, ":")[0] == config.Management.Host {
		rw.WriteHeader(http.StatusAccepted)
		command()
	} else {
		rw.WriteHeader(http.StatusForbidden)
	}
}
