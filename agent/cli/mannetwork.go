package lib

import (
	"fmt"
	"io/ioutil"
	"os/exec"
	"strings"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/net"
	"github.com/subutai-io/base/agent/lib/net/p2p"
	"github.com/subutai-io/base/agent/log"
)

func VxlanTunnel(create, del, remoteip, vlan, vni string, list bool) {
	if len(create) > 0 {
		tunnelCreate(create, remoteip, vlan, vni)
	} else if len(del) > 0 {
		net.DelIface(del)
		return
	} else if list {
		tunnelList()
		return
	}
}

func tunnelCreate(tunnel, addr, vlan, vni string) {
	log.Check(log.WarnLevel, "Creating bridge ", exec.Command("ovs-vsctl", "--may-exist", "add-br", "gw-"+vlan).Run())

	log.Check(log.FatalLevel, "Creating tunnel port",
		exec.Command("ovs-vsctl", "--may-exist", "add-port", "gw-"+vlan, tunnel, "--", "set", "interface", tunnel, "type=vxlan",
			"options:stp_enable=true", "options:key="+vni, "options:remote_ip="+string(addr)).Run())

	log.Check(log.FatalLevel, "MakeVNIMap set port: ", exec.Command("ovs-vsctl", "--if-exists", "set", "port", tunnel, "tag="+vlan).Run())
}

func tunnelList() {
	ret, err := exec.Command("ovs-vsctl", "show").CombinedOutput()
	log.Check(log.FatalLevel, "Getting OVS interfaces list", err)
	ports := strings.Split(string(ret), "\n")

	for k, port := range ports {
		if strings.Contains(port, "remote_ip") {
			tunnel := strings.Trim(strings.Trim(ports[k-2], "Interface "), "\"")
			tag := strings.TrimLeft(ports[k-3], "tag: ")
			addr := strings.Fields(port)
			vni := strings.Trim(strings.Trim(addr[1], "{key="), "\",")
			ip := strings.Trim(strings.Trim(addr[2], "remote_ip="), "\",")
			fmt.Println(tunnel, ip, tag, vni)
		}
	}
}

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
	case "-m", "--createvnimap":
		createVNIMap(args[3], args[4], args[5], args[6])
	case "-c", "--createtunnel":
		createTunnel(args[3], args[4], args[5])
	case "-l", "--listtunnel":
		listTunnel()
	}
}

func listTunnel() {
	fmt.Println("List of Tunnels\n--------")
	ret, err := exec.Command("ovs-vsctl", "show").CombinedOutput()
	log.Check(log.FatalLevel, "Getting OVS interfaces list", err)
	ports := strings.Split(string(ret), "\n")

	for k, port := range ports {
		if strings.Contains(port, "remote_ip") {
			iface := strings.Fields(ports[k-2])
			tunnel := strings.Trim(iface[1], "\"")
			addr := strings.Fields(port)
			fmt.Println(tunnel + "-" + strings.Trim(strings.Trim(addr[2], "remote_ip="), "\","))
		}
	}

}

func createTunnel(tunnel, addr, tunType string) {
	ifTunExist(tunnel)
	err := ioutil.WriteFile(config.Agent.DataPrefix+"var/subutai-network/"+tunnel, []byte(addr), 0600)
	log.Check(log.ErrorLevel, "Creating tunnel file", err)

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
		if strings.Contains(port, "env") {
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
	log.Check(log.WarnLevel, "Creating bridge ", exec.Command("ovs-vsctl", "add-br", "gw-"+vlan).Run())

	addr, _ := ioutil.ReadFile(config.Agent.DataPrefix + "var/subutai-network/" + tunnel)
	log.Check(log.FatalLevel, "Creating tunnel port",
		exec.Command("ovs-vsctl", "--may-exist", "add-port", "gw-"+vlan, tunnel, "--", "set", "interface", tunnel, "type=vxlan",
			"options:stp_enable=true", "options:key="+vni, "options:remote_ip="+string(addr), "options:env="+envid).Run())

	log.Check(log.FatalLevel, "MakeVNIMap set port: ", exec.Command("ovs-vsctl", "--if-exists", "set", "port", tunnel, "tag="+vlan).Run())
}
