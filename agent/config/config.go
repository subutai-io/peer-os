package config

import (
	"code.google.com/p/gcfg"
	"io/ioutil"
	"net"
	"os"
	"subutai/log"
	"time"
)

type agentConfig struct {
	DataPrefix  string
	AppPrefix   string
	LxcPrefix   string
	Version     string
	GpgUser     string
	GpgPassword string
	Debug       bool
}
type managementConfig struct {
	Host          string
	Port          string
	Login         string
	Password      string
	GpgUser       string
	RestToken     string
	RestPublicKey string
	RestVerify    string
	Secret        string
	Kurjun        string
}
type brokerConfig struct {
	Url               string
	Port              string
	Password          string
	ResponseTopic     string
	ExecuteTimeout    string
	BroadcastTopic    string
	HeartbeatTopic    string
	ExecuteResponce   string
	ExecuteTerminated string
}
type influxdbConfig struct {
	Server string
	Db     string
	User   string
	Pass   string
}
type miscConfig struct {
	Version string
	Arch    string
}
type configFile struct {
	Agent      agentConfig
	Management managementConfig
	Broker     brokerConfig
	Misc       miscConfig
	Influxdb   influxdbConfig
}

const defaultConfig = `
	[agent]
	version = 4.0.0
	gpgUser =
	gpgPassword = 12345678
	debug = true
	appPrefix = /apps/subutai/current/
	dataPrefix = /var/lib/apps/subutai/current/
	lxcPrefix = /mnt/lib/lxc/    

	[management]
	gpgUser =
	port = 8443
	host = 10.10.10.1
	login = admin
	password = secret
	secret = secret
	restToken = /rest/v1/identity/gettoken
	restPublicKey = /rest/v1/registration/public-key
	restVerify = /rest/v1/registration/verify/container-token
    kurjun = http://10.10.10.1:8551/rest/kurjun/templates

	[broker]
	port = 8883
	password = client
	url = ssl://10.10.10.1
	responseTopic = RESPONSE_TOPIC
	executeTimeout = EXECUTE_TIMEOUT
	BroadcastTopic = BROADCAST_TOPIC
	heartbeatTopic = HEARTBEAT_TOPIC
	executeResponce = EXECUTE_RESPONSE
	executeTerminated = EXECUTE_TERMINATED

	[influxdb]
	server = 10.10.10.1
	user = root
	pass = root
	db = metrics

    [misc]
    version = 4.0.0
    arch = amd64
`

var (
	config configFile
	// Agent describes configuration options that used for configuring Subutai Agent
	Agent agentConfig
	// Management describes configuration options that used for accessing Subutai Management server
	Management managementConfig
	// Broker describes configuration options that used for interaction with MQTT broker
	Broker brokerConfig
	// Influxdb describes configuration options for InluxDB server
	Influxdb influxdbConfig
	// Misc describes misc configuration options
	Misc miscConfig
)

func init() {
	log.Level(log.InfoLevel)

	err := gcfg.ReadStringInto(&config, defaultConfig)
	log.Check(log.InfoLevel, "Loading default config ", err)

	err = gcfg.ReadFileInto(&config, "/apps/subutai/current/etc/agent.gcfg")
	log.Check(log.WarnLevel, "Opening Agent config file /apps/subutai/current/etc/agent.gcfg", err)

	files, _ := ioutil.ReadDir("/apps/")
	for _, f := range files {
		if f.Name() == "subutai-mng" {
			config.Agent.AppPrefix = "/apps/subutai-mng/current/"
			config.Agent.DataPrefix = "/var/lib/" + config.Agent.AppPrefix
		}
	}

	name, _ := os.Hostname()
	config.Agent.GpgUser = name + "@subutai.io"

	Misc = config.Misc
	Agent = config.Agent
	Broker = config.Broker
	Influxdb = config.Influxdb
	Management = config.Management
}

func InitAgentDebug() {
	if config.Agent.Debug {
		log.Level(log.DebugLevel)
	}
}

func CheckKurjun() {
	_, err := net.DialTimeout("tcp", "10.10.10.1:8551", time.Duration(3)*time.Second)
	if log.Check(log.WarnLevel, "Connecting local Kurjun", err) {
		Management.Kurjun = "http://repo.critical-factor.com:8081/rest/kurjun/templates"
	}
}
