package gpg

import (
	"bufio"
	"bytes"
	"crypto/tls"
	// "github.com/pborman/uuid"
	"io/ioutil"
	"net/http"
	"os"
	"os/exec"
	"strings"
	"subutai/config"
	"subutai/lib/container"
	"subutai/log"
)

func GetToken() string {
	tr := &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true}}
	client := &http.Client{Transport: tr}

	resp, err := client.Get("https://" + config.Management.Host + ":" + config.Management.Port + config.Management.RestToken + "?username=" + config.Management.Login + "&password=" + config.Management.Password)
	log.Check(log.ErrorLevel, "Getting token", err)

	defer resp.Body.Close()
	token, _ := ioutil.ReadAll(resp.Body)

	if len(string(token)) != 168 {
		return ""
	}

	return string(token)
}

func GenerateKey(c string) {
	// err := ioutil.WriteFile(config.Agent.LxcPrefix+c+"/defaults", ident, 0644)
	conf, err := os.Create(config.Agent.LxcPrefix + c + "/defaults")
	defer conf.Close()
	conf.WriteString("%echo Generating default keys\n")
	conf.WriteString("Key-Type: RSA\n")
	conf.WriteString("Key-Length: 2048\n")
	conf.WriteString("Name-Real: " + c + "\n")
	conf.WriteString("Name-Comment: " + c + " GPG key\n")
	conf.WriteString("Name-Email: " + c + "@subutai.io\n")
	conf.WriteString("Expire-Date: 0\n")
	conf.WriteString("Passphrase: 12345678\n")
	conf.WriteString("%pubring " + config.Agent.LxcPrefix + c + "/public.pub\n")
	conf.WriteString("%secring " + config.Agent.LxcPrefix + c + "/secret.sec\n")
	conf.WriteString("%commit\n")
	conf.WriteString("%echo Done\n")

	log.Check(log.FatalLevel, "Writing default key ident", err)

	err = exec.Command("gpg", "--batch", "--gen-key", config.Agent.LxcPrefix+c+"/defaults").Run()
	log.Check(log.FatalLevel, "Generating key", err)
}

func getMngKey(c string) {
	tr := &http.Transport{
		TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
	}
	client := &http.Client{Transport: tr}
	resp, err := client.Get("https://" + config.Management.Host + ":" + config.Management.Port + config.Management.RestPublicKey + "?sptoken=" + GetToken())
	log.Check(log.FatalLevel, "Getting Management public key", err)

	defer resp.Body.Close()
	body, _ := ioutil.ReadAll(resp.Body)

	mngkey := []byte(body)
	err = ioutil.WriteFile(config.Agent.LxcPrefix+c+"/mgn.key", mngkey, 0644)
	log.Check(log.FatalLevel, "Writing Management public key", err)
}

func parseKeyId(s string) string {
	var id string

	line := strings.Split(s, "\n")
	if len(line) > 2 {
		cell := strings.Split(line[1], " ")
		if len(cell) > 3 {
			key := strings.Split(cell[3], "/")
			if len(key) > 1 {
				id = key[1]
			}
		}
	}
	if len(id) == 0 {
		log.Fatal("Key id parsing error")
	}
	return id
}

func writeData(c, t, n, m string) {
	token := []byte(t + "\n" + GetFingerprint(c) + "\n" + n + m)
	err := ioutil.WriteFile(config.Agent.LxcPrefix+c+"/stdin.txt", token, 0644)
	log.Check(log.FatalLevel, "Writing Management public key", err)
}

func sendData(c string) {
	asc, err := os.Open(config.Agent.LxcPrefix + c + "/stdin.txt.asc")
	log.Check(log.FatalLevel, "Reading encrypted stdin.txt.asc", err)
	defer asc.Close()

	tr := &http.Transport{
		TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
	}
	client := &http.Client{Transport: tr}
	resp, err := client.Post("https://"+config.Management.Host+":"+config.Management.Port+config.Management.RestVerify+"?sptoken="+GetToken(), "text/plain", asc)
	log.Check(log.FatalLevel, "Sending registration request to management", err)

	if resp.Status != "200 OK" {
		container.Destroy(c)
		log.Error("Failed to exchange GPG Public Keys. StatusCode: " + resp.Status)
	}

	os.Remove(config.Agent.LxcPrefix + c + "/stdin.txt.asc")
	os.Remove(config.Agent.LxcPrefix + c + "/stdin.txt")
}

func ExchageAndEncrypt(c, t string) {
	var impout, expout, imperr, experr bytes.Buffer

	getMngKey(c)

	impkey := exec.Command("gpg", "-v", "--no-default-keyring", "--keyring", config.Agent.LxcPrefix+c+"/public.pub", "--import", config.Agent.LxcPrefix+c+"/mgn.key")
	impkey.Stdout = &impout
	impkey.Stderr = &imperr
	err := impkey.Run()
	log.Check(log.FatalLevel, "Importing Management public key to keyring", err)

	id := parseKeyId(imperr.String())
	expkey := exec.Command("gpg", "--no-default-keyring", "--keyring", config.Agent.LxcPrefix+c+"/public.pub", "--export", "--armor", c+"@subutai.io")
	expkey.Stdout = &expout
	expkey.Stderr = &experr
	err = expkey.Run()
	log.Check(log.FatalLevel, "Exporting armomred key", err)

	writeData(c, t, expout.String(), experr.String())

	err = exec.Command("gpg", "--no-default-keyring", "--keyring", config.Agent.LxcPrefix+c+"/public.pub", "--trust-model", "always", "--armor", "-r", id, "--encrypt", config.Agent.LxcPrefix+c+"/stdin.txt").Run()
	log.Check(log.FatalLevel, "Encrypting stdin.txt", err)

	sendData(c)
}

func GetFingerprint(name string) (fingerprint string) {
	out, _ := exec.Command("gpg", "--fingerprint", "--keyring", config.Agent.LxcPrefix+name+"/public.pub", name).Output()
	scanner := bufio.NewScanner(bytes.NewReader(out))
	for scanner.Scan() {
		if strings.Contains(scanner.Text(), "fingerprint") {
			fp := strings.Split(scanner.Text(), "=")
			if len(fp) > 1 {
				fingerprint = strings.Replace(fp[1], " ", "", -1)
				return fingerprint
			}
		}
	}
	return ""
}

func ValidatePem(cert string) bool {
	out, _ := exec.Command("openssl", "x509", "-in", cert, "-text", "-noout").Output()
	if strings.Contains(string(out), "Public Key") {
		return true
	}
	return false
}
