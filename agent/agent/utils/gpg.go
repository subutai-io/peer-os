package utils

import (
	"bufio"
	"bytes"
	"errors"
	p "golang.org/x/crypto/openpgp"
	a "golang.org/x/crypto/openpgp/armor"
	"io"
	"io/ioutil"
	"os"
	"os/exec"
	"strings"
	"subutai/config"
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

func GetKeyByEmail(keyring *p.EntityList, email string) (*p.Entity, error) {
	for _, entity := range *keyring {
		for _, ident := range entity.Identities {
			if ident.UserId.Email == email {
				return entity, nil
			}
		}
	}
	return nil, ErrUserKeyNotFoundByEmail
}

func GetKeyById(keyring *p.EntityList, id string) (*p.Entity, error) {
	for _, entity := range *keyring {
		for _, ident := range entity.Identities {
			if strings.Contains(ident.UserId.Name, id) {
				return entity, nil
			}
		}
	}
	return nil, ErrUserKeyNotFoundByEmail
}

func GetKeyring(path string) (*p.EntityList, error) {
	file, err := os.Open(path)
	if log.Check(log.WarnLevel, "Retrieving PGP key", err) {
		return nil, ErrUserKeyRingNotFound
	}
	keyring, err := p.ReadKeyRing(file)
	if log.Check(log.WarnLevel, "Reading from keyring: "+path, err) {
		return nil, ErrUnreadableKeyring
	}
	return &keyring, nil
}

func Decrypt(message io.Reader, user string, keyring p.EntityList, mh *p.Entity) (md string, unverified error) {
	result, err := a.Decode(message)
	log.Check(log.WarnLevel, "Decoding PGP armor", err)

	entity, _ := GetKeyByEmail(&keyring, user)
	err = entity.PrivateKey.Decrypt(pass_bytes)
	log.Check(log.WarnLevel, "Decrypting private key", err)

	for _, subkey := range entity.Subkeys {
		err = subkey.PrivateKey.Decrypt(pass_bytes)
		log.Check(log.WarnLevel, "Decrypting subkey", err)
	}

	details, err := p.ReadMessage(result.Body, keyring, nil, nil)
	if log.Check(log.WarnLevel, "Decrypting message", err) {
		return "", ErrUnverifiedMessage
	}

	return verifySignature(details, mh), nil
}

func verifySignature(md *p.MessageDetails, entity *p.Entity) string {
	if md.SignatureError != nil {
		return ""
	}
	bytes, _ := ioutil.ReadAll(md.UnverifiedBody)
	sig := md.Signature
	hash := sig.Hash.New()
	_, err := hash.Write(bytes)

	if log.Check(log.WarnLevel, "Writting hash", err) {
		return ""
	}
	if ok := entity.PrimaryKey.VerifySignature(hash, sig); ok != nil {
		return string(bytes)
	}
	return ""
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
	return getPublicKey(gpg_C)
}

func getPublicKey(gpg_c string) string {
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
	key := getPublicKey(gpg_C)
	for key == "KEY_NOT_FOUND" {
		GenerateGPGKeys(name)
		key = getPublicKey(gpg_C)
	}
	return key
}

func Encrypt(message string, signer, receiver *p.Entity) string {
	encbuf := bytes.NewBuffer(nil)
	armor, _ := a.Encode(encbuf, "PGP MESSAGE", nil)

	err := signer.PrivateKey.Decrypt(pass_bytes)
	log.Check(log.WarnLevel, "Decrypting secret key", err)

	out, err := p.Encrypt(armor, []*p.Entity{receiver}, signer, nil, nil)
	log.Check(log.WarnLevel, "Encrypting message", err)

	_, err = out.Write([]byte(message))
	log.Check(log.WarnLevel, "Reading bytes", err)

	out.Close()
	armor.Close()

	return encbuf.String()
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
