package agent

import (
	"encoding/json"
	mqtt "git.eclipse.org/gitroot/paho/org.eclipse.paho.mqtt.golang.git"
	"github.com/codegangsta/cli"
	"github.com/subutai-io/Subutai/agent/agent/alert"
	"github.com/subutai-io/Subutai/agent/agent/container"
	"github.com/subutai-io/Subutai/agent/agent/executer"
	"github.com/subutai-io/Subutai/agent/agent/utils"
	"github.com/subutai-io/Subutai/agent/config"
	cont "github.com/subutai-io/Subutai/agent/lib/container"
	"github.com/subutai-io/Subutai/agent/lib/gpg"
	"github.com/subutai-io/Subutai/agent/log"
	"os"
	"runtime"
	"strings"
	"time"
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

func initAgent() {
	if cont.State("management") == "STOPPED" {
		cont.Start("management")
	}
	container.PoolInstance()
	Instance()
}

var requestHandler mqtt.MessageHandler = func(client *mqtt.Client, msg mqtt.Message) {
	go func() {
		var rsp executer.EncRequest
		var req executer.Request
		var md, contName, pub, keyring, payload string
		var flag bool
		var err error
		log.Check(log.WarnLevel, "Unmarshal payload", json.Unmarshal(msg.Payload(), &rsp))

		email, _ := os.Hostname()
		fingerprint := gpg.GetFingerprint(email + "@subutai.io")
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
			log.Info("Getting puyblic keyring", "keyring", keyring)
			md = gpg.DecryptNoDefaultKeyring(rsp.Request, keyring, pub)
		}
		log.Debug(md)
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
					response := executer.Response{ResponseOpts: elem}
					jsonR, err := json.Marshal(response)
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
					log.Check(log.WarnLevel, "Marshal response json", err)
					client.Publish(config.Broker.ResponseTopic, 0, false, message)
				} else {
					sOut = nil
				}
			}
		}
	}()
}

func Start(c *cli.Context) {
	initAgent()
	opts := InitClientOptions(c.String("server"), c.String("port"), c.String("user"), c.String("secret"))
	hostname, _ := os.Hostname()
	instancetype := utils.InstanceType()

	myuuid := gpg.GetFingerprint(c.String("user"))
	client := mqtt.NewClient(opts)

	token := client.Connect()
	for token.Wait(); log.Check(log.WarnLevel, "Connecting to MQTT Broker", token.Error()); {
		token = client.Connect()
		time.Sleep(time.Second * 5)
		InitClientOptions(c.String("server"), c.String("port"), c.String("user"), c.String("secret"))
	}

	for {
		if token := client.Subscribe(myuuid, 0, requestHandler); !token.WaitTimeout(time.Second * 10) {
			log.Warn("Waiting MQTT client token failed - timeout")
			if token.Error() != nil {
				log.Warn("Waiting MQTT client token failed with error: " + token.Error().Error())
			}
			InitClientOptions(c.String("server"), c.String("port"), c.String("user"), c.String("secret"))
			token = client.Connect()
		}
		Instance()
		beat := Heartbeat{
			Type:       "HEARTBEAT",
			Hostname:   hostname,
			Id:         myuuid,
			Arch:       strings.ToUpper(runtime.GOARCH),
			Instance:   instancetype,
			Containers: container.GetActiveContainers(false),
			Interfaces: utils.GetInterfaces(),
			Alert:      alert.Alert(),
		}
		res := Response{Beat: beat}

		jbeat, _ := json.Marshal(&res)
		log.Debug(string(jbeat))

		message, err := json.Marshal(map[string]string{
			"hostId":   myuuid,
			"response": gpg.EncryptWrapper(config.Agent.GpgUser, config.Management.GpgUser, string(jbeat)),
		})
		log.Check(log.WarnLevel, "Marshal response json", err)

		client.Publish(config.Broker.HeartbeatTopic, 0, false, message)
		time.Sleep(5 * time.Second)
	}
}
