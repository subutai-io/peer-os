package template

import (
	"crypto/tls"
	"io"
	"net/http"
	"os"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/fs"
	"github.com/subutai-io/base/agent/log"

	"github.com/jhoonb/archivex"
)

func IsRegistered(templateName string) bool {
	returnValue := true
	restTemplateURL := config.Cdn.Kurjun + templateName

	tr := &http.Transport{
		TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
	}
	client := &http.Client{Transport: tr}

	resp, err := client.Get(restTemplateURL)
	if err != nil {
		log.Error("IsTemplateRegistered: get rest, " + err.Error())
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		returnValue = false
	}
	return returnValue
}

func Tar(folder, file string) {
	archive := new(archivex.TarFile)
	archive.Create(file)
	log.Check(log.FatalLevel, "Packing template "+folder, archive.AddAll(folder, false))
	archive.Close()
}

func copy(src string, dst string) {
	sf, err := os.Open(src)
	log.Check(log.FatalLevel, "Opening "+src, err)
	defer sf.Close()

	df, err := os.Create(dst)
	log.Check(log.FatalLevel, "Creating "+dst, err)
	defer df.Close()

	_, err = io.Copy(df, sf)
	log.Check(log.FatalLevel, "Copying "+dst, err)
}

func Install(parent, child string) {
	delta := map[string][]string{
		child + "/deltas/rootfs.delta": {parent + "/rootfs", child},
		child + "/deltas/home.delta":   {parent + "/home", child},
		child + "/deltas/opt.delta":    {parent + "/opt", child},
		child + "/deltas/var.delta":    {parent + "/var", child},
	}

	fs.SubvolumeCreate(config.Agent.LxcPrefix + child)

	p := true
	if parent == child || parent == "" {
		p = false
	}

	for delta, path := range delta {
		fs.Receive(config.Agent.LxcPrefix+path[0], config.Agent.LxcPrefix+path[1], delta, p)
	}

	for _, file := range []string{"config", "fstab", "packages"} {
		copy(config.Agent.LxcPrefix+"tmpdir/"+child+"/"+file, config.Agent.LxcPrefix+child+"/"+file)
	}
}
