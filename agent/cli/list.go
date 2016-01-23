package lib

import (
	"fmt"
	lxc "gopkg.in/lxc/go-lxc.v2"
	"strings"
	"subutai/config"
	"subutai/lib/container"
	"subutai/log"
)

func printHeader(c, t, r, i, a, f, p bool) {
	var header, line string
	if i {
		header = "NAME\tSTATE\tHWADDR\t\t\tIP\t\tInterface"
		line = "-----------------------------------------------------------------"
	} else if c == t {
		header = "CONT/TEMP"
		line = "---------"
	} else if c {
		header = "CONTAINER"
		line = "---------"
	} else if t {
		header = "TEMPLATE"
		line = "--------"
	}
	if p {
		header = header + "\t" + "PARENT"
		line = line + "\t" + "------"
	}
	if a {
		header = header + "\t" + "ANCESTORS"
		line = line + "\t" + "---------"
	}
	fmt.Println(header)
	fmt.Println(line)
}

func printList(list []string, c, t, r, i, a, f, p bool) {
	printHeader(c, t, r, i, a, f, p)
	for _, item := range list {
		fmt.Println(item)
	}
}

// LxcList list the containers
// defined and active containers in order
func LxcList(name string, c, t, r, i, a, f, p bool) {
	list := []string{}
	if i {
		if name == "" {
			for _, item := range container.Containers() {
				list = append(list, info(item)...)
			}
		} else {
			list = append(list, info(name)...)
		}
	} else if c == t {
		list = append(list, container.All()...)
	} else if c {
		list = append(list, container.Containers()...)
	} else if t {
		list = append(list, container.Templates()...)
	}
	for j := range list {
		if list[j] == name {
			list = []string{name}
			break
		} else if name != "" && j == len(list)-1 && !i {
			list = []string{}
		}
	}
	if p {
		list = addParent(list)
	}
	if a {
		list = addAncestors(list)
	}
	printList(list, c, t, r, i, a, f, p)

}
func addParent(list []string) []string {
	for i := range list {
		parent := container.GetParent(list[i])
		list[i] = list[i] + "\t\t" + parent
	}
	return list
}

func addAncestors(list []string) []string {
	for i := range list {
		child := list[i]
		result := ""
		for child != container.GetParent(child) {
			if result != "" {
				result = result + "," + container.GetParent(child)
			} else {
				result = container.GetParent(child)
			}
			child = container.GetParent(child)
		}
		list[i] = list[i] + "\t\t" + result
	}
	return list
}

func info(name string) (result []string) {
	c, err := lxc.NewContainer(name, config.Agent.LxcPrefix)
	log.Check(log.FatalLevel, "Looking for container "+name, err)

	nic := "eth0"
	listip, _ := c.IPAddress(nic)
	ip := strings.Join(listip, " ")
	mac := container.GetConfigItem(config.Agent.LxcPrefix+"/"+name+"/config", "lxc.network.hwaddr")

	return append(result, name+"\tRUNNING\t"+mac+"\t"+ip+"\t"+nic)
}
