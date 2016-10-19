package gpg

import (
	"bufio"
	"bytes"
	"crypto/tls"
	"io/ioutil"
	"net/http"
	"os"
	"os/exec"
	"strings"

	"golang.org/x/crypto/openpgp"
	"golang.org/x/crypto/openpgp/clearsign"

	"github.com/subutai-io/base/agent/agent/utils"
	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/lib/container"
	"github.com/subutai-io/base/agent/log"
)

const (
	tmpfile = "epub.key"
)

//ImportPk import PK gpg2 --import pubkey.key
func ImportPk(k []byte) string {
	err := ioutil.WriteFile(tmpfile, k, 0644)
	log.Check(log.WarnLevel, "Writing Pubkey to temp file", err)

	command := exec.Command("gpg", "--import", tmpfile)
	out, err := command.CombinedOutput()
	log.Check(log.WarnLevel, "Importing MH public key", err)

	os.Remove(tmpfile)
	return string(out)
}

func GetContainerPk(name string) string {
	lxc_path := config.Agent.LxcPrefix + name + "/public.pub"
	stdout, err := exec.Command("/bin/bash", "-c", "gpg --no-default-keyring --keyring "+lxc_path+" --export -a "+name+"@subutai.io").Output()
	log.Check(log.WarnLevel, "Getting Container public key", err)
	return string(stdout)
}

func GetPk(name string) string {
	stdout, err := exec.Command("gpg", "--export", "-a", name).Output()
	log.Check(log.WarnLevel, "Getting public key", err)
	if len(stdout) == 0 {
		log.Warn("GPG key for RH not found. Creating new.")
		GenerateKey(name)
	}
	return string(stdout)
}

func DecryptWrapper(args ...string) string {
	gpg := "gpg --passphrase " + config.Agent.GpgPassword + " --no-tty"
	if len(args) == 3 {
		gpg = gpg + " --no-default-keyring --keyring " + args[2] + " --secret-keyring " + args[1]
	}
	command := exec.Command("/bin/bash", "-c", gpg)
	stdin, err := command.StdinPipe()
	log.Check(log.WarnLevel, "Opening Stdin Pipe", err)
	stdin.Write([]byte(args[0]))
	stdin.Close()

	output, err := command.Output()
	log.Check(log.WarnLevel, "Executing command "+gpg, err)

	return string(output)
}

func EncryptWrapper(user, recipient string, message []byte, args ...string) string {
	gpg := "gpg --batch --passphrase " + config.Agent.GpgPassword + " --trust-model always --armor -u " + user + " -r " + recipient + " --sign --encrypt --no-tty"
	if len(args) >= 2 {
		gpg = gpg + " --no-default-keyring --keyring " + args[0] + " --secret-keyring " + args[1]
	}
	command := exec.Command("/bin/bash", "-c", gpg)
	stdin, _ := command.StdinPipe()
	stdin.Write(message)
	stdin.Close()

	output, err := command.Output()
	if log.Check(log.WarnLevel, "Encrypting message", err) {
		return ""
	}

	return string(output)
}

func GenerateKey(name string) {
	path := config.Agent.LxcPrefix + name
	email := name + "@subutai.io"
	pass := config.Agent.GpgPassword
	if !container.IsContainer(name) {
		os.MkdirAll("/root/.gnupg/", 0700)
		path = "/root/.gnupg"
		email = name
		pass = config.Agent.GpgPassword
	}
	// err := ioutil.WriteFile(config.Agent.LxcPrefix+c+"/defaults", ident, 0644)
	conf, err := os.Create(path + "/defaults")
	defer conf.Close()
	conf.WriteString("%echo Generating default keys\n")
	conf.WriteString("Key-Type: RSA\n")
	conf.WriteString("Key-Length: 2048\n")
	conf.WriteString("Name-Real: " + name + "\n")
	conf.WriteString("Name-Comment: " + name + " GPG key\n")
	conf.WriteString("Name-Email: " + email + "\n")
	conf.WriteString("Expire-Date: 0\n")
	conf.WriteString("Passphrase: " + pass + "\n")
	conf.WriteString("%pubring " + path + "/public.pub\n")
	conf.WriteString("%secring " + path + "/secret.sec\n")
	conf.WriteString("%commit\n")
	conf.WriteString("%echo Done\n")

	log.Check(log.FatalLevel, "Writing default key ident", err)

	log.Check(log.FatalLevel, "Generating key",
		exec.Command("gpg", "--batch", "--gen-key", path+"/defaults").Run())
	if !container.IsContainer(name) {
		log.Check(log.FatalLevel, "Importing secret key",
			exec.Command("gpg", "--allow-secret-key-import", "--import", "/root/.gnupg/secret.sec").Run())
		log.Check(log.FatalLevel, "Importing public key",
			exec.Command("gpg", "--import", "/root/.gnupg/public.pub").Run())
	}
}

func GetFingerprint(email string) string {
	var out []byte
	if email == config.Agent.GpgUser {
		out, _ = exec.Command("gpg", "--fingerprint", email).Output()
	} else {
		out, _ = exec.Command("gpg", "--fingerprint", "--keyring", config.Agent.LxcPrefix+email+"/public.pub", email).Output()
	}
	scanner := bufio.NewScanner(bytes.NewReader(out))
	for scanner.Scan() {
		if strings.Contains(scanner.Text(), "fingerprint") {
			fp := strings.Split(scanner.Text(), "=")
			if len(fp) > 1 {
				return strings.Replace(fp[1], " ", "", -1)
			}
		}
	}
	return ""
}

func getMngKey(c string) {
	tr := &http.Transport{
		TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
	}
	client := &http.Client{Transport: tr}
	resp, err := client.Get("https://" + config.Management.Host + ":" + config.Management.Port + config.Management.RestPublicKey)
	log.Check(log.FatalLevel, "Getting Management public key", err)

	defer resp.Body.Close()
	body, _ := ioutil.ReadAll(resp.Body)

	err = ioutil.WriteFile(config.Agent.LxcPrefix+c+"/mgn.key", body, 0644)
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
	os.Remove(config.Agent.LxcPrefix + c + "/stdin.txt.asc")
	os.Remove(config.Agent.LxcPrefix + c + "/stdin.txt")
	token := []byte(t + "\n" + GetFingerprint(c) + "\n" + n + m)
	err := ioutil.WriteFile(config.Agent.LxcPrefix+c+"/stdin.txt", token, 0644)
	log.Check(log.FatalLevel, "Writing Management public key", err)
}

func sendData(c string) {
	asc, err := os.Open(config.Agent.LxcPrefix + c + "/stdin.txt.asc")
	log.Check(log.FatalLevel, "Reading encrypted stdin.txt.asc", err)
	defer asc.Close()

	client := utils.TLSConfig()
	resp, err := client.Post("https://"+config.Management.Host+":8444/rest/v1/registration/verify/container-token", "text/plain", asc)
	os.Remove(config.Agent.LxcPrefix + c + "/stdin.txt.asc")
	os.Remove(config.Agent.LxcPrefix + c + "/stdin.txt")
	log.Check(log.FatalLevel, "Sending registration request to management", err)

	if resp.StatusCode != 200 && resp.StatusCode != 202 {
		log.Error("Failed to exchange GPG Public Keys. StatusCode: " + resp.Status)
	}

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

func ValidatePem(cert string) bool {
	out, _ := exec.Command("openssl", "x509", "-in", cert, "-text", "-noout").Output()
	if strings.Contains(string(out), "Public Key") {
		return true
	}
	return false
}

func ParsePem(cert string) (crt, key []byte) {
	key, _ = exec.Command("openssl", "pkey", "-in", cert).Output()

	f, err := ioutil.ReadFile(cert)
	if !log.Check(log.DebugLevel, "Cannot read file "+cert, err) {
		crt = bytes.Replace(f, key, []byte(""), -1)
	}
	return crt, key
}

func KurjunUserPK(owner string) string {
	kurjun, _ := config.CheckKurjun()
	response, err := kurjun.Get(config.Cdn.Kurjun + "/auth/key?user=" + owner)
	log.Check(log.FatalLevel, "Getting owner public key", err)
	defer response.Body.Close()
	key, err := ioutil.ReadAll(response.Body)
	log.Check(log.FatalLevel, "Reading key body", err)
	return string(key)
}

func VerifySignature(key, signature string) string {
	entity, err := openpgp.ReadArmoredKeyRing(bytes.NewBufferString(key))
	log.Check(log.WarnLevel, "Reading user public key", err)

	if block, _ := clearsign.Decode([]byte(signature)); block != nil {
		_, err = openpgp.CheckDetachedSignature(entity, bytes.NewBuffer(block.Bytes), block.ArmoredSignature.Body)
		if log.Check(log.ErrorLevel, "Checking signature", err) {
			return ""
		}
		return string(block.Bytes)
	}
	return ""
}
