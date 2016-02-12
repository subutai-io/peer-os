package template

import (
	"crypto/tls"
	"github.com/jhoonb/archivex"
	"github.com/subutai-io/Subutai/agent/config"
	"github.com/subutai-io/Subutai/agent/lib/fs"
	"github.com/subutai-io/Subutai/agent/log"
	"io"
	"net/http"
	"os"
)

func IsRegistered(templateName string) bool {
	returnValue := true
	restTemplateURL := config.Management.Kurjun + templateName

	// token := gpg.GetToken()
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
		fs.Receive(path[0], path[1], delta, p)
	}

	for _, file := range []string{"config", "fstab", "packages"} {
		copy(config.Agent.LxcPrefix+"lxc-data/tmpdir/"+child+"/"+file, config.Agent.LxcPrefix+child+"/"+file)
	}
}
