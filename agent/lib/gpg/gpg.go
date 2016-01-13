package gpg

import (
	"bufio"
	"bytes"
	"crypto/tls"
	"errors"
	p "golang.org/x/crypto/openpgp"
	"io/ioutil"
	"net/http"
	"os"
	"os/exec"
	"strings"
	"subutai/config"
	"subutai/lib/container"
	"subutai/log"
)

var (
	ErrUserKeyNotFoundByEmail = errors.New("Key not found by user email")
	ErrUserKeyRingNotFound    = errors.New("Keyring not found")
	ErrUnreadableKeyring      = errors.New("Could not read keyring")
	ErrUnverifiedMessage      = errors.New("Message does not contain signature")
	pass_bytes                = []byte(config.Agent.GpgPassword)
)

const (
	MANAGEMENT_HOST_PK = "/root/.gnupg/pubring.gpg"
	gnupg              = "gpg"
)

func keyByEmail(keyring *p.EntityList, email string) (*p.Entity, error) {
	for _, entity := range *keyring {
		for _, ident := range entity.Identities {
			if ident.UserId.Email == email {
				return entity, nil
			}
		}
	}
	return nil, ErrUserKeyNotFoundByEmail
}

//import PK gpg2 --import pubkey.key
func ImportPk(file string) string {
	command := exec.Command("/bin/bash", "-c", gnupg+" --import "+file)
	out, err := command.CombinedOutput()
	log.Check(log.WarnLevel, "Importing MH public key", err)
	return string(out)
}

func GetContainerPk(name string) string {
	lxc_path := config.Agent.LxcPrefix + name + "/public.pub"
	gpg_C := "gpg --no-default-keyring --keyring " + lxc_path + " --export -a " + name + "@subutai.io"
	return publicKey(gpg_C)
}

func publicKey(gpg_c string) string {
	buf := new(bytes.Buffer)
	command := exec.Command("/bin/bash", "-c", gpg_c)

	stdout, err := command.StdoutPipe()
	log.Check(log.WarnLevel, "Openning Stdout pipe", err)
	log.Check(log.WarnLevel, "Executing command"+gpg_c, command.Start())

	size, err := buf.ReadFrom(stdout)
	log.Check(log.WarnLevel, "Reading from Stdout pipe", err)
	log.Check(log.WarnLevel, "Waiting for command"+gpg_c, command.Wait())

	defer stdout.Close()

	if size == 0 {
		log.Warn("No key found")
		return "KEY_NOT_FOUND"
	}
	return buf.String()
}

func GetPk(name string) string {
	gpg_C := "gpg --export -a " + name
	key := publicKey(gpg_C)
	for key == "KEY_NOT_FOUND" {
		GenerateGPGKeys(name)
		key = publicKey(gpg_C)
	}
	return key
}

func DecryptNoDefaultKeyring(message, keyring, pub string) string {
	gpg_d := "gpg --passphrase " + config.Agent.GpgPassword + " --no-tty --no-default-keyring --keyring " + pub + " --secret-keyring " + keyring
	command := exec.Command("/bin/bash", "-c", gpg_d)

	stdin, err := command.StdinPipe()
	log.Check(log.WarnLevel, "Opening Stdin Pipe", err)
	stdin.Write([]byte(message))
	stdin.Close()

	output, err := command.Output()
	log.Check(log.WarnLevel, "Executing command "+gpg_d, err)

	return string(output)
}

func DecryptWrapper(message string) string {
	command := exec.Command("/bin/bash", "-c", "gpg --passphrase "+config.Agent.GpgPassword+" --no-tty")

	stdin, err := command.StdinPipe()
	stdin.Write([]byte(message))
	stdin.Close()

	output, err := command.Output()
	log.Check(log.WarnLevel, "Decrypting message "+string(output), err)

	return string(output)
}

func EncryptWrapper(user string, recipient string, message string) string {
	gpg_C := gnupg + " --batch --passphrase " + config.Agent.GpgPassword + " --trust-model always  --armor -u " + user + " -r " + recipient + " --sign --encrypt --no-tty"
	command := exec.Command("/bin/bash", "-c", gpg_C)
	stdin, _ := command.StdinPipe()
	stdin.Write([]byte(message))
	stdin.Close()

	output, err := command.Output()
	if log.Check(log.WarnLevel, "Encrypting message "+string(output), err) {
		return ""
	}

	return string(output)
}
func EncryptWrapperNoDefaultKeyring(user, recipient, message, pub, sec string) string {
	gpg_C := gnupg + " --batch --passphrase " + config.Agent.GpgPassword + " --trust-model always  --no-default-keyring --keyring " + pub + " --secret-keyring " + sec + " --armor -u " + user + "@subutai.io -r " + recipient + " --sign --encrypt --no-tty"
	command := exec.Command("/bin/bash", "-c", gpg_C)
	stdin, _ := command.StdinPipe()
	stdin.Write([]byte(message))
	stdin.Close()

	output, err := command.Output()
	log.Check(log.WarnLevel, "Encrypting message "+string(output), err)
	return string(output)
}

func ImportMHKeyNoDefaultKeyring(cont string) {
	pub := config.Agent.LxcPrefix + cont + "/public.pub"
	gpg_C := gnupg + " --no-default-keyring --keyring " + pub + " --import epub.key"
	command := exec.Command("/bin/bash", "-c", gpg_C)
	status, err := command.CombinedOutput()
	log.Check(log.WarnLevel, "Importing Management Host Key "+string(status), err)
}

func GenerateGPGKeys(email string) {
	os.MkdirAll("/root/.gnupg/", 0700)
	conf, _ := os.Create("/root/.gnupg/defaults")
	defer conf.Close()
	conf.WriteString("%echo Generating default keys\n")
	conf.WriteString("Key-Type: RSA\n")
	conf.WriteString("Key-Length: 2048\n")
	conf.WriteString("Name-Real: Subutai Host\n")
	conf.WriteString("Name-Comment: Subutai Host GPG key\n")
	conf.WriteString("Name-Email: " + email + "\n")
	conf.WriteString("Expire-Date: 0\n")
	conf.WriteString("Passphrase: " + config.Agent.GpgPassword + "\n")
	conf.WriteString("%pubring /root/.gnupg/public.pub\n")
	conf.WriteString("%secring /root/.gnupg/secret.sec\n")
	conf.WriteString("%commit\n")
	conf.WriteString("%echo Done\n")

	// exec.Command("rngd", "-f", "-r", "/dev/urandom").Run()
	exec.Command("gpg", "--batch", "--gen-key", "/root/.gnupg/defaults").Run()
	exec.Command("gpg", "--allow-secret-key-import", "--import", "/root/.gnupg/secret.sec").Run()
	exec.Command("gpg", "--import", "/root/.gnupg/public.pub").Run()
}

func GetFingerprint(email string) (fingerprint string) {
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
				fingerprint = strings.Replace(fp[1], " ", "", -1)
				return fingerprint
			}
		}
	}
	return ""
}

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

func ValidatePem(cert string) bool {
	out, _ := exec.Command("openssl", "x509", "-in", cert, "-text", "-noout").Output()
	if strings.Contains(string(out), "Public Key") {
		return true
	}
	return false
}
