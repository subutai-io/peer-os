package config

import (
	"crypto/tls"
	"net"
	"net/http"
	"time"

	"github.com/subutai-io/base/agent/log"

	"gopkg.in/gcfg.v1"
)

var client *http.Client

type agentConfig struct {
	Debug       bool
	GpgUser     string
	AppPrefix   string
	LxcPrefix   string
	DataPrefix  string
	GpgPassword string
}
type managementConfig struct {
	CDN           string
	Host          string
	Port          string
	Secret        string
	GpgUser       string
	RestVerify    string
	RestPublicKey string
}

type influxdbConfig struct {
	Server string
	Db     string
	User   string
	Pass   string
}
type cdnConfig struct {
	Allowinsecure bool
	URL           string
	SSLport       string
	Kurjun        string
}
type templateConfig struct {
	Branch  string
	Version string
	Arch    string
}
type configFile struct {
	Agent      agentConfig
	Management managementConfig
	Influxdb   influxdbConfig
	CDN        cdnConfig
	Template   templateConfig
}

const defaultConfig = `
	[agent]
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
	secret = secret
	restPublicKey = /rest/v1/security/keyman/getpublickeyring	

    [cdn]
    url = cdn.subut.ai
    sslport = 8338
    allowinsecure = false

	[influxdb]
	server = 10.10.10.1
	user = root
	pass = root
	db = metrics

	[template]
	version = 4.0.0
	branch =
	arch = amd64
`

var (
	config configFile
	// Agent describes configuration options that used for configuring Subutai Agent
	Agent agentConfig
	// Management describes configuration options that used for accessing Subutai Management server
	Management managementConfig
	// Influxdb describes configuration options for InluxDB server
	Influxdb influxdbConfig
	// CDN url and port
	CDN cdnConfig
	// Template describes template configuration options
	Template templateConfig
)

func init() {
	log.Level(log.InfoLevel)

	err := gcfg.ReadStringInto(&config, defaultConfig)
	log.Check(log.InfoLevel, "Loading default config ", err)

	err = gcfg.ReadFileInto(&config, "/apps/subutai/current/etc/agent.gcfg")
	log.Check(log.WarnLevel, "Opening Agent config file /apps/subutai/current/etc/agent.gcfg", err)

	err = gcfg.ReadFileInto(&config, "/var/lib/apps/subutai/current/agent.gcfg")
	log.Check(log.DebugLevel, "Opening preserved config file /var/lib/apps/subutai/current/etc/agent.gcfg", err)

	if config.Agent.GpgUser == "" {
		config.Agent.GpgUser = "rh@subutai.io"
	}
	Agent = config.Agent
	Influxdb = config.Influxdb
	Template = config.Template
	Management = config.Management
	CDN = config.CDN
}

// InitAgentDebug turns on Debug output for the Subutai Agent.
func InitAgentDebug() {
	if config.Agent.Debug {
		log.Level(log.DebugLevel)
	}
}

// CheckKurjun checks if the Kurjun node available.
func CheckKurjun() (*http.Client, error) {
	// _, err := net.DialTimeout("tcp", Management.Host+":8339", time.Duration(2)*time.Second)
	tr := &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true}}
	client = &http.Client{Transport: tr}
	// if !log.Check(log.InfoLevel, "Trying local repo", err) {
	// Cdn.Kurjun = "https://" + Management.Host + ":8339/rest/kurjun"
	// } else {
	_, err := net.DialTimeout("tcp", CDN.URL+":"+CDN.SSLport, time.Duration(2)*time.Second)
	for c := 0; err != nil && c < 5; _, err = net.DialTimeout("tcp", CDN.URL+":"+CDN.SSLport, time.Duration(2)*time.Second) {
		log.Info("CDN unreachable, retrying")
		time.Sleep(3 * time.Second)
		c++
	}
	if log.Check(log.WarnLevel, "Checking CDN accessibility", err) {
		return nil, err
	}

	CDN.Kurjun = "https://" + CDN.URL + ":" + CDN.SSLport + "/kurjun/rest"
	if !CDN.Allowinsecure {
		client = &http.Client{}
	}
	// }
	return client, nil
}
