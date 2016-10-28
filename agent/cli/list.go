package cli

import (
	"fmt"
	"io"
	"os"
	"strings"
	"text/tabwriter"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/log"
	lxc "gopkg.in/lxc/go-lxc.v2"
)

// printHeader prints list headerline
func printHeader(w io.Writer, c, t, i, a, p bool) {
	var header, line string
	if i {
		header = "NAME\tSTATE\tIP\tInterface"
		line = "----\t-----\t--\t---------"
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
		header = header + "\tPARENT"
		line = line + "\t------"
	}
	if a {
		header = header + "\tANCESTORS"
		line = line + "\t---------"
	}
	fmt.Fprintln(w, header)
	fmt.Fprintln(w, line)
}

// printList prints list
func printList(list []string, c, t, i, a, p bool) {
	w := new(tabwriter.Writer)
	w.Init(os.Stdout, 0, 8, 1, '\t', 0)
	printHeader(w, c, t, i, a, p)
	for _, item := range list {
		fmt.Fprintln(w, item)
	}
	w.Flush()
}

// LxcList function shows a listing of Subutai instances with information such as IP address, parent template, etc.
func LxcList(name string, c, t, i, a, p bool) {
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
	printList(list, c, t, i, a, p)

}

// addParent adds parent to each template in list
func addParent(list []string) []string {
	for i := range list {
		parent := container.GetParent(strings.Fields(list[i])[0])
		list[i] = list[i] + "\t" + parent
	}
	return list
}

// addAncestors adds ancestors to each template in list
func addAncestors(list []string) []string {
	for i := range list {
		child := strings.Fields(list[i])[0]
		result := ""
		for child != container.GetParent(child) {
			if result != "" {
				result = result + "," + container.GetParent(child)
			} else {
				result = container.GetParent(child)
			}
			child = container.GetParent(child)
		}
		list[i] = list[i] + "\t" + result
	}
	return list
}

// info adds container's IP and NIC to list
func info(name string) (result []string) {
	c, err := lxc.NewContainer(name, config.Agent.LxcPrefix)
	log.Check(log.FatalLevel, "Looking for container "+name, err)

	nic := "eth0"

	listip, _ := c.IPAddress(nic)
	ip := strings.Join(listip, " ")

	return append(result, name+"\t"+container.State(name)+"\t"+ip+"\t"+nic)
}
