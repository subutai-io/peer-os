package template

import (
	"crypto/tls"
	"github.com/jhoonb/archivex"
	"io"
	"net/http"
	"os"
	"subutai/config"
	"subutai/lib/fs"
	"subutai/log"
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
		child + "/deltas/opt.delta":    {"lxc/" + parent + "-opt", "lxc/"},
		child + "/deltas/home.delta":   {"lxc-data/" + parent + "-home", "lxc-data/"},
		child + "/deltas/var.delta":    {"lxc-data/" + parent + "-var", "lxc-data/"},
	}

	log.Check(log.FatalLevel, "Creating dir "+child, os.MkdirAll(config.Agent.LxcPrefix+child, 0700))

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
