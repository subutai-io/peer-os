package parser

import (
	docker "github.com/docker/docker/builder/dockerfile/parser"
	"os"
	"strconv"
	"strings"
)

func parceEnv(line []string) string {
	if len(line) > 2 {
		return "export " + line[1] + "=" + strings.Join(line[2:], " ") + "\n"
	}
	return ""
}

func parceCopy(line []string) string {
	if len(line) > 1 {
		return "cp -rf " + strings.Join(line[1:], " ") + "\n"
	}
	return ""
}

func parceRun(line []string) string {
	if len(line) > 1 {
		str, _ := strconv.Unquote(strings.Join(line[1:], " "))
		if !(strings.Contains(str, "ln") && (strings.Contains(str, "/dev/stdout") || strings.Contains(str, "/dev/stderr"))) {
			str = strings.Replace(str, "\t", " ", -1)
			str = strings.Replace(str, " && ", "\n", -1)
			return str + "\n"
		}
	}
	return ""
}

func parceFrom(line []string) string {
	if len(line) > 1 {
		if strings.Contains(line[1], "debian") {
			return "debian"
		} else if strings.Contains(line[1], "ubuntu") {
			return "ubuntu"
		}
	}
	return ""
}

func parceCmd(line []string) string {
	if len(line) > 1 {
		str, _ := strconv.Unquote(strings.Join(line[1:], " "))
		str = strings.Replace(str, "\t", " ", -1)
		return `sed -i -e '$i \` + str + ` &\n' /etc/rc.local`
	}
	return ""
}

func Parce(name string) (out, env, image string) {
	file, _ := os.Open(name)
	node, _ := docker.Parse(file)
	file.Close()

	for _, n := range node.Children {
		if str := strings.Fields(n.Dump()); len(str) > 0 {
			switch str[0] {
			case "env":
				env = env + parceEnv(str)
			case "run":
				out = out + parceRun(str)
			case "add", "copy":
				out = out + parceCopy(str)
			case "from":
				image = parceFrom(str)
			// case "cmd":
			// 	out = out + parceCmd(str)
			// }
		}
	}
	if len(out) > 0 {
		return out, env, image
	}
	return
}
