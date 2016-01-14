package lib

import (
	"fmt"
	"io/ioutil"
	"os"
	"os/exec"
	"strings"
	"subutai/cli/lib"
	"subutai/config"
	"subutai/lib/gpg"
	"subutai/log"
)

var (
	conf    = config.Agent.AppPrefix + "etc/lighttpd.conf"
	confinc = config.Agent.AppPrefix + "etc/lighttpd-includes/"
)

func ProxyAdd(vlan, domain, node, policy, cert string) {
	if vlan == "" {
		log.Error("Please specify VLAN")
	} else if domain != "" {
		if isVlanExist(vlan) {
			log.Error("Domain already exist")
		}
		addDomain(vlan, domain, cert)
		switch policy {
		case "rr":
			setPolicy(vlan, "round-robin")
		case "lb":
			setPolicy(vlan, "fair")
		case "hash":
			setPolicy(vlan, "hash")
		}
		restart()
	} else if node != "" {
		if isNodeExist(vlan, node) {
			log.Error("Node is already in domain")
		}
		addNode(vlan, node)
		restart()
	}
}

func ProxyDel(vlan, node string, domain bool) {
	if !isVlanExist(vlan) {
		log.Error("Domain doesn't exist")
	}
	if domain && node == "" {
		delDomain(vlan)
	}
	if node != "" {
		delNode(vlan, node)
	}
	restart()
}

func ProxyCheck(vlan, node string, domain bool) {
	if vlan != "" && domain {
		if d := getDomain(vlan); d != "" {
			fmt.Println(d)
			os.Exit(0)
		} else {
			os.Exit(1)
		}
	} else if vlan != "" && node != "" {
		if isNodeExist(vlan, node) {
			log.Info("Node is in domain")
			os.Exit(0)
		} else {
			log.Info("Node is not in domain")
			os.Exit(1)
		}
	}
}

func restart() {
	log.Check(log.FatalLevel, "systemctl restart",
		exec.Command("systemctl", "restart", "subutai-mng_lighttpd_*").Run())
}

func addDomain(vlan, domain, cert string) {
	addLine(conf, "#Add new includes here", "include \""+confinc+vlan+".conf\"", false)
	if cert != "" && gpg.ValidatePem(cert) {
		lib.CopyFile(confinc+"vhost-ssl.example", confinc+vlan+".conf")
		lib.CopyFile(cert, config.Agent.DataPrefix+"/web/ssl/"+domain+".pem")
		addLine(confinc+vlan+".conf", "url.redirect = (\".*\" => \"https://DOMAIN$0\")", "url.redirect = (\".*\" => \"https://"+domain+"$0\")", true)
		addLine(confinc+vlan+".conf", "ssl.pemfile = \""+config.Agent.DataPrefix+"web/ssl/DOMAIN.pem\"", "ssl.pemfile = \""+config.Agent.DataPrefix+"web/ssl/"+domain+".pem\"", true)
	} else {
		lib.CopyFile(confinc+"vhost.example", confinc+vlan+".conf")
	}
	addLine(confinc+vlan+".conf", "$HTTP[\"host\"] == \"DOMAIN\" {", "$HTTP[\"host\"] == \""+domain+"\" {", true)
}

func addNode(vlan, node string) {
	addLine(confinc+vlan+".conf", "#Add new host here", "( \"host\" => \""+node+"\" ),", false)
}

func delDomain(vlan string) {
	delLine(conf, "include \""+confinc+vlan+".conf\"")
	os.Remove(confinc + vlan + ".conf")
}

func delNode(vlan, node string) {
	delLine(confinc+vlan+".conf", "( \"host\" => \""+node+"\" ),")
}

func getDomain(vlan string) string {
	f, err := ioutil.ReadFile(confinc + vlan + ".conf")
	if err != nil {
		return ""
	}
	lines := strings.Split(string(f), "\n")
	for _, v := range lines {
		if strings.Contains(v, "$HTTP[\"host\"]") {
			line := strings.Fields(v)
			if len(line) > 3 {
				return strings.Trim(line[2], "\"")
			}
		}
	}
	return ""
}

func isVlanExist(vlan string) bool {
	return addLine(conf, "include \""+confinc+vlan+".conf\"", "", false)
}

func isNodeExist(vlan, node string) bool {
	return addLine(confinc+vlan+".conf", "( \"host\" => \""+node+"\" ),", "", false)
}

func setPolicy(vlan, policy string) {
	addLine(confinc+vlan+".conf", "balance = \"round-robin\"", "balance = \""+policy+"\"", true)
}

func addLine(path, after, line string, replace bool) bool {
	if f, err := ioutil.ReadFile(path); err != nil {
		log.Error("Cannot read file " + path)
	} else {
		lines := strings.Split(string(f), "\n")
		for k, v := range lines {
			if strings.Contains(v, after) {
				if line != "" {
					if replace {
						log.Debug("Replacing " + lines[k] + " with " + line)
						lines[k] = line
					} else {
						log.Debug("Adding " + line + " after " + lines[k])
						lines[k] = after + "\n" + line
					}
				} else {
					return true
				}
			}
		}
		str := strings.Join(lines, "\n")
		log.Check(log.FatalLevel, "Writing new proxy config",
			ioutil.WriteFile(path, []byte(str), 0744))
	}
	return false
}

func delLine(path, line string) {
	var lines2 []string
	if f, err := ioutil.ReadFile(path); err != nil {
		log.Error("Cannot read file " + path)
	} else {
		lines := strings.Split(string(f), "\n")
		for _, v := range lines {
			if !strings.Contains(v, line) {
				lines2 = append(lines2, v)
			}
		}
		str := strings.Join(lines2, "\n")
		log.Check(log.FatalLevel, "Writing new proxy config",
			ioutil.WriteFile(path, []byte(str), 0744))
	}
}
