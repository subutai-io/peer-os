package agent

import (
	"encoding/json"
	"io/ioutil"
	"net/http"
	"net/url"
	"os"
	"runtime"
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
	Containers []container.Container `json:"containers,omitempty"`
	Alert      []alert.Load          `json:"alert,omitempty"`
}

var (
	lastHeartbeat     []byte
	mutex             sync.Mutex
	fingerprint       string
	hostname          string
	client            *http.Client
	instanceType      string
	instanceArch      string
	lastHeartbeatTime time.Time
	pool              []container.Container
)

func initAgent() {
	// move .gnupg dir to app home
	os.Setenv("GNUPGHOME", config.Agent.DataPrefix+".gnupg")

	instanceType = utils.InstanceType()
	instanceArch = strings.ToUpper(runtime.GOARCH)
	client = tlsConfig()
}

func Start(c *cli.Context) {
	http.HandleFunc("/trigger", trigger)
	http.HandleFunc("/ping", ping)
	http.HandleFunc("/heartbeat", heartbeatCall)
	go http.ListenAndServe(":7070", nil)

	initAgent()
	go lib.Collect()
	go connectionMonitor()
	go alert.AlertProcessing()

	for {
		if heartbeat() {
			time.Sleep(30 * time.Second)
		} else {
			time.Sleep(5 * time.Second)
		}
		container.ContainersRestoreState(pool)
	}
}

func connectionMonitor() {
	for {
		hostname, _ = os.Hostname()
		if fingerprint == "" || config.Management.GpgUser == "" {
			fingerprint = gpg.GetFingerprint(hostname + "@subutai.io")
			connect.Connect(config.Management.Host, config.Management.Port, config.Agent.GpgUser, config.Management.Secret)
		} else {
			resp, err := client.Get("https://" + config.Management.Host + ":8444/rest/v1/agent/check/" + fingerprint)
			if err == nil && resp.StatusCode == http.StatusOK {
				resp.Body.Close()
				log.Debug("Connection monitor check - success")
			} else {
				log.Debug("Connection monitor check - failed")
				connect.Connect(config.Management.Host, config.Management.Port, config.Agent.GpgUser, config.Management.Secret)
				lastHeartbeat = []byte{}
				go heartbeat()
			}
		}

		time.Sleep(time.Second * 10)
	}
}

func heartbeat() bool {
	mutex.Lock()
	defer mutex.Unlock()
	if len(lastHeartbeat) > 0 && time.Since(lastHeartbeatTime) < time.Second*5 {
		return false
	}

	pool = container.GetActiveContainers(false)
	beat := Heartbeat{
		Type:       "HEARTBEAT",
		Hostname:   hostname,
		Id:         fingerprint,
		Arch:       instanceArch,
		Instance:   instanceType,
		Containers: pool,
		Interfaces: utils.GetInterfaces(),
		Alert:      alert.CurrentAlerts(pool),
	}
	res := Response{Beat: beat}
	jbeat, _ := json.Marshal(&res)

	lastHeartbeatTime = time.Now()
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
		log.Debug(resp.Status)
		resp.Body.Close()
		if resp.StatusCode == http.StatusAccepted {
			return true
		}
	}
	lastHeartbeat = []byte{}
	return false
}

func execute(rsp executer.EncRequest) {
	var req executer.Request
	var md, contName, pub, keyring, payload string
	var err error

	if rsp.HostId == fingerprint {
		md = gpg.DecryptWrapper(rsp.Request)
	} else {
		contName = nameById(rsp.HostId)
		if contName == "" {
			lastHeartbeat = []byte{}
			heartbeat()
			contName = nameById(rsp.HostId)
			if contName == "" {
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
	if rsp.HostId == fingerprint {
		go executer.ExecHost(req.Request, sOut)
	} else {
		go executer.AttachContainer(contName, req.Request, sOut)
	}

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
				go response(message)
			} else {
				sOut = nil
			}
		}
	}
	go heartbeat()
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
	return &http.Client{Transport: transport, Timeout: time.Second * 30}
}

func response(msg []byte) {
	resp, err := client.PostForm("https://"+config.Management.Host+":8444/rest/v1/agent/response", url.Values{"response": {string(msg)}})
	if !log.Check(log.WarnLevel, "Sending response "+string(msg), err) {
		resp.Body.Close()
		if resp.StatusCode == http.StatusAccepted {
			return
		}
	}
	time.Sleep(time.Second * 5)
	go response(msg)

}

func command() {
	var rsp []executer.EncRequest

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
			go execute(request)
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
		go command()
	} else {
		rw.WriteHeader(http.StatusForbidden)
	}
}

func heartbeatCall(rw http.ResponseWriter, request *http.Request) {
	if request.Method == http.MethodGet && strings.Split(request.RemoteAddr, ":")[0] == config.Management.Host {
		rw.WriteHeader(http.StatusOK)
		lastHeartbeat = []byte{}
		heartbeat()
	} else {
		rw.WriteHeader(http.StatusForbidden)
	}
}

func nameById(id string) string {
	for _, c := range pool {
		if c.Id == id {
			return c.Name
		}
	}
	return ""
}
