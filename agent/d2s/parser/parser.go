package parser

import (
	"fmt"
	docker "github.com/docker/docker/builder/dockerfile/parser"
	"os"
	"strconv"
	"strings"
)

func parceEnv(line []string) string {
	line = line[1:]
	str := ""
	if strings.Contains(line[0], "=") {
		str = strings.Join(line, " ")
	} else {
		str = line[0] + "=" + strings.Join(line[1:], " ")

	}
	str = strings.Replace(str, `\t`, " ", -1)
	fmt.Println(str)
	return "export " + str + "\n"
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
			// str = strings.Replace(str, " && ", "\n", -1)
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
		str := strings.Join(line[1:], " ")
		str = strings.Replace(str, "\t", " ", -1)
		return str
	}
	return ""
}

func Parce(name string) (out, env, cmd, image string) {
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
			case "cmd":
				cmd = cmd + parceCmd(str)
			case "entrypoint":
				cmd = cmd + parceCmd(str) + " "
			}
		}
	}
	if len(cmd) > 1 {
		slice := strings.Split(cmd, " ")
		for i, _ := range slice {
			slice[i] = strings.Replace(slice[i], `"`, "", -1)
		}

		cmd = slice[0] + ` "` + strings.Join(slice[1:], " ") + `"`
	}
	if len(out) > 0 {
		return out, env, cmd, image
	}
	return
}
