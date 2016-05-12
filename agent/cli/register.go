package lib

import (
	"bytes"
	// "crypto/tls"
	"fmt"
	"io"
	"mime/multipart"
	"net/http"
	"os"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/log"
)

func LxcRegister(name string) {
	config.CheckKurjun()

	tarFullPath := config.Agent.LxcPrefix + "tmpdir/" + name + "-subutai-template_" + config.Template.Version + "_" + config.Template.Arch + ".tar.gz"

	_, err := os.Stat(tarFullPath)
	if log.Check(log.WarnLevel, "Looking for archive", err) {
		LxcExport(name, "")
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

	req, err := http.NewRequest("POST", config.Cdn.Kurjun+"/upload/public", &data)
	log.Debug(config.Cdn.Kurjun + "/upload/public")

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
