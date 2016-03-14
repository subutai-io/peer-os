package parser

import (
	docker "github.com/docker/docker/builder/dockerfile/parser"
	"os"
	"regexp"
	"strconv"
	"strings"
)

func parceEnv(line string) string {
	var tokenWhitespace = regexp.MustCompile(`[\t\v\f\r ]+`)
	var str string

	line = strings.Replace(line, "ENV ", "", -1)
	if strings.Contains(line, "=") {
		str = line
	} else {
		slice := tokenWhitespace.Split(line, 2)
		str = slice[0] + "=" + `"` + strings.Join(slice[1:], " ") + `"`
	}
	str = strings.Replace(str, "\t", " ", -1)
	return "export " + str + "\n"
}

func parceCopy(line []string) string {
	if len(line) > 1 {
		line[1] = `"/opt/docker2subutai/` + strings.Replace(line[1], `"`, "", -1) + `"`
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

func parceCmd(line []string, isEntrypoint bool) string {
	if len(line) > 1 {
		str := strings.Join(line[1:], " ")
		str = strings.Replace(str, "\t", " ", -1)
		if isEntrypoint {
			str, _ = strconv.Unquote(str)
		}
		return str
	}
	return ""
}

func Parce(name string) (out, env, cmd, image, user, wdir string) {
	file, _ := os.Open(name)
	node, _ := docker.Parse(file)
	file.Close()

	for _, n := range node.Children {
		if str := strings.Fields(n.Dump()); len(str) > 0 {
			switch str[0] {
			case "env":
				// env = env + parceEnv(str)
				env = env + parceEnv(n.Original)
			case "run":
				out = out + parceRun(str)
			case "add", "copy":
				out = out + parceCopy(str)
			case "from":
				image = parceFrom(str)
			case "cmd":
				cmd = cmd + parceCmd(str, false)
			case "entrypoint":
				cmd = cmd + parceCmd(str, true) + " "
			case "user":
				user, _ = strconv.Unquote(strings.Join(str[1:], ""))
			case "workdir":
				wdir, _ = strconv.Unquote(strings.Join(str[1:], ""))
			}
		}
	}
	if len(out) > 0 {
		return out, env, cmd, image, user, wdir
	}
	return
}
