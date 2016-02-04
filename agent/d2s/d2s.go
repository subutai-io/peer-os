package main

import (
	"fmt"
	"github.com/subutai-io/Subutai/agent/d2s/parser"
	"io/ioutil"
	"os"
	"os/exec"
	"strings"
	"subutai/lib/template"
)

func main() {
	tmpdir := "/home/ubuntu/tmp/"
	if len(os.Args) <= 1 {
		fmt.Println("Please specify path to Dockerfile")
		os.Exit(1)
	}
	dockerfile := os.Args[1]
	if !strings.HasSuffix(strings.ToUpper(dockerfile), "DOCKERFILE") {
		dockerfile = dockerfile + "/Dockerfile"
	}
	out, img := parser.Parce(dockerfile)

	if img != "" {
		cmd := exec.Command("subutai", "import", img)
		cmd.Stdout = os.Stdout
		cmd.Stderr = os.Stderr
		cmd.Run()
		// TODO add exec check here.
	}
	ioutil.WriteFile(strings.Trim(dockerfile, "Dockerfile")+"install.sh", []byte(out), 0755)
	template.Tar(strings.Trim(dockerfile, "Dockerfile"), tmpdir+"archive.tar.gz")
}
