package net

import (
	"fmt"
	"io/ioutil"
	"os"
	"os/exec"
	"strings"
	"subutai/config"
	"subutai/log"
)

func CreateVNIFile(name string) {
	if _, err := os.Stat(config.Agent.DataPrefix + "var/subutai-network/"); os.IsNotExist(err) {
		log.Check(log.FatalLevel, "create /var/subutai-network folder", os.MkdirAll(config.Agent.DataPrefix+"var/subutai-network", 0755))
	}
	if _, err := os.Stat(config.Agent.DataPrefix + "var/subutai-network/" + name); os.IsNotExist(err) {
		_, err = os.Create(config.Agent.DataPrefix + "var/subutai-network/" + name)
		if err != nil {
			log.Error("/var/subutai-network/" + name + " create " + err.Error())
		}
	}
}

// since we need to check this for mapping vni
func CheckVNIFile(filename, vni, vlan, envid string) ([]bool, []string) {
	var lines []string
	retBool := []bool{false, false, false, false}
	if f, err := ioutil.ReadFile(config.Agent.DataPrefix + "var/subutai-network/" + filename); err != nil {
		log.Error(err.Error())
	} else {
		lines = strings.Split(string(f), "\n")
		for _, v := range lines {
			row := strings.Fields(v)
			if len(row) > 2 {
				if row[0] == vni {
					retBool[0] = true
				}
				if row[1] == vlan {
					retBool[1] = true
				}
				if row[2] == envid {
					retBool[2] = true
				}
				if strings.Contains(v, vni+" "+vlan+" "+envid) {
					retBool[3] = true
				}
			}
		}
	}
	return retBool, lines
}

func MakeVNIMap(tunnelPortName, vni, vlan, envid string) {
	ret, lines := CheckVNIFile(tunnelPortName+"_vni_vlan", vni, vlan, envid)
	if ret[0] == true || ret[1] == true {
		log.Error(vni + " or " + vlan + " are already in use")
	}
	lines = append(lines, vni+" "+vlan+" "+envid)
	str := strings.Join(lines, "\n")
	log.Check(log.FatalLevel, "write "+tunnelPortName,
		ioutil.WriteFile(config.Agent.DataPrefix+"var/subutai-network/"+tunnelPortName+"_vni_vlan", []byte(str), 0744))
	// check: ovs-vsctl
	log.Check(log.FatalLevel, "MakeVNIMap set interface: ",
		exec.Command("ovs-vsctl", "--if-exists", "set", "interface", tunnelPortName, "options:key="+vni).Run())
	log.Check(log.FatalLevel, "MakeVNIMap set port: ",
		exec.Command("ovs-vsctl", "--if-exists", "set", "port", tunnelPortName, "tag="+vlan).Run())
}

func DisplayVNIMap() {
	files, _ := ioutil.ReadDir(config.Agent.DataPrefix + "var/subutai-network/")
	// fmt.Printf("%0s %20s %20s\n", "Tunnel", "VNI", "VLAN ID")
	for _, f := range files {
		if strings.Contains(f.Name(), "_vni_vlan") {
			tunnelPortName := strings.Trim(f.Name(), "_vni_vlan")
			if fcont, err := ioutil.ReadFile(config.Agent.DataPrefix + "var/subutai-network/" + f.Name()); err != nil {
				log.Error("read error " + config.Agent.DataPrefix + "var/subutai-network/" + f.Name() + "_vni_vlan " + err.Error())
			} else {
				for _, v := range strings.Split(string(fcont), "\n") {
					if v != "" {
						s := strings.Fields(v)
						fmt.Printf("%s\t%s\t%s\t%s\n", tunnelPortName, s[0], s[1], s[2])
					}
				}
			}
		}
	}
}

func DelVNI(tunnelPortName, vni, vlan string) {
	var lines []string
	if f, err := ioutil.ReadFile(config.Agent.DataPrefix + "var/subutai-network/" + tunnelPortName + "_vni_vlan"); err != nil {
		log.Error("read : " + config.Agent.DataPrefix + "var/subutai-network/" + tunnelPortName + "_vni_vlan" + err.Error())
	} else {
		lines = strings.Split(string(f), "\n")
		for k, v := range lines {
			if strings.Contains(v, vni+" "+vlan) {
				lines[k] = ""
			}
		}
	}
	str := strings.Join(lines, "\n")
	log.Check(log.FatalLevel, "write "+config.Agent.DataPrefix+"var/subutai-network/"+tunnelPortName+"_vni_vlan",
		ioutil.WriteFile(config.Agent.DataPrefix+"var/subutai-network/"+tunnelPortName+"_vni_vlan", []byte(str), 0744))
}

func MakeReservation(vni, vlan, envid string) {
	// regexpControl(envid)
	ret, lines := CheckVNIFile("vni_reserve", vni, vlan, envid)
	if ret[0] == true {
		log.Error("vni found.")
	}
	if ret[1] == true {
		log.Error("vlanid found.")
	}
	if ret[2] == true {
		log.Error("envid found.")
	}
	if ret[3] == true {
		log.Error("revervation found.")
	}

	log.Info("no reserv found. reserving...")
	lines = append(lines, vni+" "+vlan+" "+envid)
	str := strings.Join(lines, "\n")
	log.Check(log.FatalLevel, "reserv write ",
		ioutil.WriteFile(config.Agent.DataPrefix+"var/subutai-network/vni_reserve", []byte(str), 0744))
}

func ListVNI() {
	if _, err := os.Stat(config.Agent.DataPrefix + "/var/subutai-network/vni_reserve"); os.IsNotExist(err) {
		CreateVNIFile("vni_reserve")
	}

	f, err := ioutil.ReadFile(config.Agent.DataPrefix + "/var/subutai-network/vni_reserve")
	log.Check(log.ErrorLevel, "Reading "+config.Agent.DataPrefix+"/var/subutai-network/vni_reserve", err)

	for _, v := range strings.Split(string(f), "\n") {
		s := strings.Fields(v)
		if len(s) > 2 {
			fmt.Printf("%s,%s,%s\n", s[0], s[1], s[2])
		}
	}
}

func DeleteAllVNI(vlan string) {
	if _, err := os.Stat(config.Agent.DataPrefix + "/var/subutai-network/vni_reserve"); os.IsNotExist(err) {
		log.Error(config.Agent.DataPrefix + "/var/subutai-network/vni_reserve does not exist " + err.Error())
	}

	f, err := ioutil.ReadFile(config.Agent.DataPrefix + "/var/subutai-network/vni_reserve")
	log.Check(log.ErrorLevel, "Reading "+config.Agent.DataPrefix+"/var/subutai-network/vni_reserve", err)

	for _, v := range strings.Split(string(f), "\n") {
		s := strings.Fields(v)
		if len(s) > 2 && s[1] == vlan {
			DeleteVNI(s[0], s[1], s[2])
			break
		}
	}

	log.Info("All VNI's deleted.")
}

func DeleteVNI(vni, vlan, envid string) {
	if _, err := os.Stat(config.Agent.DataPrefix + "/var/subutai-network/vni_reserve"); os.IsNotExist(err) {
		log.Error(config.Agent.DataPrefix + "/var/subutai-network/vni_reserve" + " does not exist")
	}

	var lines []string

	f, err := ioutil.ReadFile(config.Agent.DataPrefix + "/var/subutai-network/vni_reserve")
	log.Check(log.ErrorLevel, "Reading "+config.Agent.DataPrefix+"/var/subutai-network/vni_reserve", err)

	lines = strings.Split(string(f), "\n")
	for k, v := range lines {
		if v == vni+" "+vlan+" "+envid {
			lines[k] = ""
		}
	}
	err = ioutil.WriteFile(config.Agent.DataPrefix+"/var/subutai-network/vni_reserve", []byte(strings.Join(lines, "\n")), 0744)
	log.Check(log.FatalLevel, "config.Agent.DataPrefix + /var/subutai-network/vni_reserve delete vni", err)

	log.Info(vni + " " + vlan + " " + envid + " deleted")
}
