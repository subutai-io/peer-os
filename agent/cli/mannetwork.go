package lib

import (
	"fmt"
	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/net/p2p"
	"github.com/subutai-io/base/agent/log"
	"io/ioutil"
	"os"
	"os/exec"
	"strings"
)

func P2P(c, d, u, l, p bool, args []string) {
	if c {
		if len(args) > 8 {
			p2p.Create(args[4], args[8], args[5], args[6], args[7])
		} else if len(args) > 7 {
			p2p.Create(args[4], "dhcp", args[5], args[6], args[7])
		} else {
			fmt.Println("Wrong usage")
		}
	} else if u {
		if len(args) > 6 {
			p2p.UpdateKey(args[4], args[5], args[6])
		} else {
			fmt.Println("Wrong usage")
		}
	} else if d {
		if len(args) > 4 {
			p2p.Remove(args[4])
		} else {
			fmt.Println("Wrong usage")
		}
	} else if p {
		if len(args) > 4 {
			p2p.Peers(args[4])
		} else {
			p2p.Peers("")
		}
	}
}

func LxcManagementNetwork(args []string) {
	if len(args) < 3 {
		log.Error("Not enough arguments")
	}
	switch args[2] {
	case "-v", "--listvnimap":
		displayVNIMap()
	case "-r", "--removetunnel":
		removeTunnel(args[3])
	case "-T", "--creategateway":
		return
	case "-M", "--removevni":
		return
	case "-E", "--reservevni":
		reserveVNI(args[3], args[4], args[5])
	case "-m", "--createvnimap":
		createVNIMap(args[3], args[4], args[5], args[6])
	case "-c", "--createtunnel":
		createTunnel(args[3], args[4], args[5])
	case "-l", "--listtunnel":
		listTunnel()
	case "-Z", "--vniop":
		switch args[3] {
		case "deleteall":
			return
		case "delete":
			return
		case "list":
			listVNI()
		}
	}
}

func createFile() {
	if _, err := os.Stat(config.Agent.DataPrefix + "var/subutai-network/"); os.IsNotExist(err) {
		log.Check(log.FatalLevel, "Creating network data folder", os.MkdirAll(config.Agent.DataPrefix+"var/subutai-network", 0755))
	}
	if _, err := os.Stat(config.Agent.DataPrefix + "var/subutai-network/vni_reserve"); os.IsNotExist(err) {
		_, err = os.Create(config.Agent.DataPrefix + "var/subutai-network/vni_reserve")
		log.Check(log.ErrorLevel, "Creating VNI file", err)
	}
}

func listVNI() {
	createFile()
	f, err := ioutil.ReadFile(config.Agent.DataPrefix + "/var/subutai-network/vni_reserve")
	log.Check(log.ErrorLevel, "Reading "+config.Agent.DataPrefix+"/var/subutai-network/vni_reserve", err)

	for _, v := range strings.Split(string(f), "\n") {
		s := strings.Fields(v)
		if len(s) > 2 {
			fmt.Printf("%s,%s,%s\n", s[0], s[1], s[2])
		}
	}
}

func checkVNI(vni, vlan, envid string) {
	f, _ := ioutil.ReadFile(config.Agent.DataPrefix + "var/subutai-network/vni_reserve")
	lines := strings.Split(string(f), "\n")
	for _, v := range lines {
		if v == vni+" "+vlan+" "+envid {
			log.Error("Reservation already exist")
		}
	}
}

func reserveVNI(vni, vlan, envid string) {
	createFile()
	checkVNI(vni, vlan, envid)
	f, err := os.OpenFile(config.Agent.DataPrefix+"var/subutai-network/vni_reserve", os.O_APPEND|os.O_WRONLY, 0600)
	log.Check(log.ErrorLevel, "Appending line", err)

	defer f.Close()

	if _, err = f.WriteString(vni + " " + vlan + " " + envid + "\n"); err != nil {
		log.Error("Writing reserver")
	}
}

func listTunnel() {
	fmt.Println("List of Tunnels\n--------")
	ret, err := exec.Command("ovs-vsctl", "show").CombinedOutput()
	log.Check(log.FatalLevel, "Getting OVS interfaces list", err)
	ports := strings.Split(string(ret), "\n")

	for k, port := range ports {
		if strings.Contains(string(port), "remote_ip") {
			iface := strings.Fields(ports[k-2])
			tunnel := strings.Trim(iface[1], "\"")
			addr := strings.Fields(port)
			fmt.Println(tunnel + "-" + strings.Trim(strings.Trim(addr[3], "remote_ip="), "\","))
		}
	}

}

func createTunnel(tunnel, addr, tunType string) {
	ifTunExist(tunnel)

	log.Check(log.FatalLevel, "Creating tunnel port",
		exec.Command("ovs-vsctl", "--may-exist", "add-port", "wan", tunnel, "--", "set", "interface", tunnel, "type="+tunType,
			"options:stp_enable=true", "options:key=flow", "options:remote_ip="+addr).Run())
}

func ifTunExist(name string) {
	ret, err := exec.Command("ovs-vsctl", "list-ports", "wan").CombinedOutput()
	log.Check(log.FatalLevel, "Getting port list", err)
	ports := strings.Split(string(ret), "\n")

	for _, port := range ports {
		if port == name {
			log.Error("Tunnel port " + name + " is already exists")
		}
	}
}

func displayVNIMap() {
	// tunnel1	8880164	100	04c088b9-e2f5-40b3-bd6c-2305b9a88058
	ret, err := exec.Command("ovs-vsctl", "show").CombinedOutput()
	log.Check(log.FatalLevel, "Getting OVS interfaces list", err)
	ports := strings.Split(string(ret), "\n")

	for k, port := range ports {
		if strings.Contains(string(port), "env") {
			iface := strings.Fields(ports[k-2])
			tunname := strings.Trim(iface[1], "\"")
			tag := strings.Fields(ports[k-3])[1]
			addr := strings.Fields(port)
			// ip := strings.Trim(strings.Trim(addr[3], "remote_ip="), "\",")
			key := strings.Trim(strings.Trim(addr[2], "key="), "\",")
			env := strings.Trim(strings.Trim(addr[1], "{env="), "\",")
			fmt.Println(tunname + " " + key + " " + tag + " " + env)
		}
	}
}

func createVNIMap(tunnel, vni, vlan, envid string) {
	log.Check(log.FatalLevel, "MakeVNIMap set interface: ",
		exec.Command("ovs-vsctl", "--if-exists", "set", "interface", tunnel, "options:key="+vni, "options:env="+envid).Run())
	log.Check(log.FatalLevel, "MakeVNIMap set port: ",
		exec.Command("ovs-vsctl", "--if-exists", "set", "port", tunnel, "tag="+vlan).Run())

}

func removeTunnel(tunnel string) {
	log.Check(log.WarnLevel, "Removing port "+tunnel,
		exec.Command("ovs-vsctl", "--if-exists", "del-port", tunnel).Run())
}

func delTunById(envId string) {
	ret, err := exec.Command("ovs-vsctl", "show").CombinedOutput()
	log.Check(log.FatalLevel, "Getting OVS interfaces list", err)
	ports := strings.Split(string(ret), "\n")

	for k, port := range ports {
		if strings.Contains(string(port), envId) {
			tunnel := strings.Split(ports[k-2], "\"")[1]
			log.Check(log.WarnLevel, "Removing port "+tunnel,
				exec.Command("ovs-vsctl", "--if-exists", "del-port", tunnel).Run())
		}
	}

}

func ClearVlan(vlan string) {
	var lines []string
	f, err := ioutil.ReadFile(config.Agent.DataPrefix + "/var/subutai-network/vni_reserve")
	if !log.Check(log.DebugLevel, "Reading "+config.Agent.DataPrefix+"/var/subutai-network/vni_reserve", err) {
		lines = strings.Split(string(f), "\n")
		for k, v := range lines {
			s := strings.Fields(v)
			if len(s) > 2 && s[1] == vlan {
				delTunById(s[2])
				p2p.Remove(s[2])
				lines[k] = ""
			}
		}
	}
	err = ioutil.WriteFile(config.Agent.DataPrefix+"/var/subutai-network/vni_reserve", []byte(strings.Join(lines, "\n")), 0744)
	log.Check(log.WarnLevel, "config.Agent.DataPrefix + /var/subutai-network/vni_reserve delete vni", err)
}
