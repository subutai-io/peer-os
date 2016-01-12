package agent

import (
	"encoding/json"
	// "fmt"
	mqtt "git.eclipse.org/gitroot/paho/org.eclipse.paho.mqtt.golang.git"
	"github.com/codegangsta/cli"
	log "gopkg.in/inconshreveable/log15.v2"
	"os"
	"runtime"
	"strings"
	"subutai/agent/alert"
	"subutai/agent/container"
	"subutai/agent/executer"
	"subutai/agent/utils"
	"subutai/config"
	clilog "subutai/log"
	"time"
)

func initAgent() {
	os.MkdirAll(config.Agent.DataPrefix+"log", 0755)
	logfile, err := log.FileHandler(config.Agent.DataPrefix+"log/agent.log", log.TerminalFormat())
	clilog.Check(clilog.WarnLevel, "Trancating file", err)

	log.Root().SetHandler(logfile)
	log.Info("Initializing agent....")

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
		clilog.Check(clilog.WarnLevel, "Unmarshal payload", json.Unmarshal(msg.Payload(), &rsp))

		email, _ := os.Hostname()
		fingerprint := utils.GetFingerprint(email + "@subutai.io")
		if rsp.HostId == fingerprint {
			flag = true
			md = utils.DecryptWrapper(rsp.Request)
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
			md = utils.DecryptNoDefaultKeyring(rsp.Request, keyring, pub)
		}
		clilog.Debug(md)
		i := strings.Index(md, "{")
		j := strings.LastIndex(md, "}") + 1
		if i > j && i > 0 {
			clilog.Warn("Error getting JSON request")
			return
		}
		request := md[i:j]
		// log.Info("INFO", "Incoming request", request)

		err = json.Unmarshal([]byte(request), &req.Request)
		clilog.Check(clilog.WarnLevel, "Decrypting request", err)

		//create channels for stdout and stderr
		sOut := make(chan executer.ResponseOptions)
		req.Execute(flag, sOut)

		for sOut != nil {
			select {
			case elem, ok := <-sOut:
				if ok {
					log.Info("Command results", "response", elem)
					response := executer.Response{ResponseOpts: elem}
					jsonR, err := json.Marshal(response)
					clilog.Check(clilog.WarnLevel, "Marshal response", err)
					if rsp.HostId == fingerprint {
						payload = utils.EncryptWrapper(config.Agent.GpgUser, config.Management.GpgUser, string(jsonR))
					} else {
						payload = utils.EncryptWrapperNoDefaultKeyring(contName, config.Management.GpgUser, string(jsonR), pub, keyring)
					}
					message, err := json.Marshal(map[string]string{
						"hostId":   elem.Id,
						"response": payload,
					})
					clilog.Check(clilog.WarnLevel, "Marshal response json", err)
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

	myuuid := utils.GetFingerprint(c.String("user"))
	client := mqtt.NewClient(opts)

	token := client.Connect()
	for token.Wait(); clilog.Check(clilog.WarnLevel, "Connecting to MQTT Broker", token.Error()); {
		token = client.Connect()
		time.Sleep(time.Second * 5)
	}

	for {
		if token := client.Subscribe(myuuid, 0, requestHandler); !token.WaitTimeout(time.Second * 10) {
			clilog.Warn("Waiting MQTT client token failed - timeout")
			if token.Error() != nil {
				clilog.Warn("Waiting MQTT client token failed with error: " + token.Error().Error())
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
		// log.Info("INFO", "HEARTBEAT", beat)
		clilog.Debug(string(jbeat))

		// message := utils.EncryptWrapper(config.Agent.GpgUser, config.Management.GpgUser, string(jbeat))

		message, err := json.Marshal(map[string]string{
			"hostId":   myuuid,
			"response": utils.EncryptWrapper(config.Agent.GpgUser, config.Management.GpgUser, string(jbeat)),
		})
		clilog.Check(clilog.WarnLevel, "Marshal response json", err)

		// fmt.Println(string(message))
		client.Publish(config.Broker.HeartbeatTopic, 0, false, message)
		time.Sleep(5 * time.Second)
	}
}
