package lib

import (
	"io"
	"net/http"
	"os"
)

func DownloadFile(DLfrom, DLto string) error {
	fp, err := os.Create(DLto)
	defer fp.Close()
	response, err := http.Get(DLfrom)
	defer response.Body.Close()
	_, err = io.Copy(fp, response.Body)
	return err
}
