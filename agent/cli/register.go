package lib

import (
	"bytes"
	// "crypto/tls"
	"fmt"
	"io"
	"mime/multipart"
	"net/http"
	"os"
	"subutai/config"
	"subutai/lib/gpg"
	"subutai/log"
)

func LxcRegister(name string) {
	tarFullPath := config.Agent.LxcPrefix + "lxc-data/tmpdir/" + name + "-subutai-template_" + config.Misc.Version + "_" + config.Misc.Arch + ".tar.gz"

	_, err := os.Stat(tarFullPath)
	if log.Check(log.WarnLevel, "Looking for archive", err) {
		LxcExport(name)
	}

	var data bytes.Buffer
	w := multipart.NewWriter(&data)
	defer w.Close()
	f, err := os.Open(tarFullPath)
	log.Check(log.FatalLevel, "Opening template archive: "+tarFullPath, err)

	fw, err := w.CreateFormFile("package", tarFullPath)
	log.Check(log.FatalLevel, "Creating HTTP request", err)

	_, err = io.Copy(fw, f)
	log.Check(log.FatalLevel, "Copying file to request", err)

	req, err := http.NewRequest("POST", config.Management.Kurjun+"/upload/public?sptoken="+gpg.GetToken(), &data)
	log.Debug(config.Management.Kurjun + "upload/public?sptoken=" + gpg.GetToken())

	log.Check(log.FatalLevel, "Creating post request", err)

	req.Header.Set("Content-Type", w.FormDataContentType())

	// tr := &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true}}
	// client := &http.Client{Transport: tr}
	client := &http.Client{}
	res, err := client.Do(req)
	log.Check(log.FatalLevel, "Executing post request", err)

	if res.StatusCode != http.StatusOK {
		fmt.Println(res)
		log.Fatal("Bad status: " + res.Status)
	}

	log.Info("Template " + name + " successfully registered")
}
