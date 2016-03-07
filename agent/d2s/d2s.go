package main

import (
	"fmt"
	"github.com/subutai-io/base/agent/d2s/parser"
	"github.com/subutai-io/base/agent/lib/template"
	"io/ioutil"
	"os"
	"os/exec"
	"strings"
)

func main() {
	tmpdir := "/home/ubuntu/tmpfs/"

	if len(os.Args) <= 1 {
		fmt.Println("Please specify path to Dockerfile")
		os.Exit(1)
	}
	dockerfile := os.Args[1]
	if !strings.HasSuffix(strings.ToUpper(dockerfile), "DOCKERFILE") {
		dockerfile = dockerfile + "/Dockerfile"
	}
	head := "#!/bin/bash\n" + "uudecode $0 |tar zxf -\n" + ". .env\n"
	out, env, img := parser.Parce(dockerfile)

	if img != "" {
		cmd := exec.Command("subutai", "import", img)
		cmd.Stdout = os.Stdout
		cmd.Stderr = os.Stderr
		cmd.Run()
		// TODO add exec check here.
	}

	ioutil.WriteFile(strings.Trim(dockerfile, "Dockerfile")+".env", []byte(env), 0644)
	template.Tar(strings.Trim(dockerfile, "Dockerfile"), tmpdir+"archive.tar.gz")
	out = head + out + "\nexit 0\n\n"

	data, _ := exec.Command("uuencode", tmpdir+"archive.tar.gz", "-").Output()
	out = out + string(data) // add archived data

	// ioutil.WriteFile(strings.Trim(dockerfile, "Dockerfile")+"install.sh", []byte(out), 0755)
	ioutil.WriteFile(tmpdir+"install.sh", []byte(out), 0755)
	_ = os.Remove(tmpdir + "archive.tar.gz")

}
