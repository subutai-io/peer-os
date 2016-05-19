package utils

import (
	"crypto/rand"
	"crypto/rsa"
	"crypto/tls"
	"crypto/x509"
	"crypto/x509/pkix"
	"encoding/pem"
	"io/ioutil"
	"math/big"
	"net"
	"net/http"
	"os"
	"strings"
	"time"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/log"
)

type Iface struct {
	InterfaceName string `json:"interfaceName"`
	Ip            string `json:"ip"`
}

func GetInterfaces() []Iface {
	n_ifaces, err := net.Interfaces()
	log.Check(log.WarnLevel, "Getting network interfaces", err)

	l_ifaces := []Iface{}
	for _, ifac := range n_ifaces {
		if ifac.Name == "lo0" || ifac.Name == "lo" || !strings.Contains(ifac.Flags.String(), "up") {
			continue
		}
		inter := new(Iface)
		inter.InterfaceName = ifac.Name
		addrs, _ := ifac.Addrs()
		for _, addr := range addrs {
			switch v := addr.(type) {
			case *net.IPNet:
				ipv4 := v.IP.To4().String()
				if ipv4 != "<nil>" {
					inter.Ip = ipv4
					l_ifaces = append(l_ifaces, *inter)
				}
			}
		}
	}
	return l_ifaces
}

func PublicCert() string {
	pemCerts, err := ioutil.ReadFile(config.Agent.DataPrefix + "ssl/cert.pem")
	if log.Check(log.WarnLevel, "Checking cert.pem file", err) {
		return ""
	}
	return string(pemCerts)
}

func InstanceType() string {
	uuid, err := ioutil.ReadFile("/sys/hypervisor/uuid")
	if !log.Check(log.DebugLevel, "Checking if AWS ec2 by reading /sys/hypervisor/uuid", err) {
		if strings.HasPrefix(string(uuid), "ec2") {
			return "EC2"
		}
	}
	return "LOCAL"
}

func TLSConfig() *http.Client {
	tlsconfig := newTLSConfig()
	for tlsconfig == nil || len(tlsconfig.Certificates[0].Certificate) == 0 {
		time.Sleep(time.Second * 2)
		for PublicCert() == "" {
			x509generate()
		}
		tlsconfig = newTLSConfig()
	}

	transport := &http.Transport{TLSClientConfig: tlsconfig}
	return &http.Client{Transport: transport, Timeout: time.Second * 10}
}

func x509generate() {
	hostname, _ := os.Hostname()
	host := []string{hostname}
	priv, err := rsa.GenerateKey(rand.Reader, 2048)
	if log.Check(log.WarnLevel, "Generating private key", err) {
		return
	}

	serialNumberLimit := new(big.Int).Lsh(big.NewInt(1), 128)
	serialNumber, err := rand.Int(rand.Reader, serialNumberLimit)
	if log.Check(log.WarnLevel, "Generating serial number", err) {
		return
	}

	var notBefore time.Time
	notBefore = time.Now()
	notAfter := notBefore.Add(3650 * 24 * time.Hour)
	template := x509.Certificate{
		SerialNumber:          serialNumber,
		Subject:               pkix.Name{Organization: []string{"Subutai Social Foundation"}},
		NotBefore:             notBefore,
		NotAfter:              notAfter,
		KeyUsage:              x509.KeyUsageKeyEncipherment | x509.KeyUsageDigitalSignature,
		ExtKeyUsage:           []x509.ExtKeyUsage{x509.ExtKeyUsageServerAuth},
		BasicConstraintsValid: true,
		DNSNames:              host,
	}

	derBytes, err := x509.CreateCertificate(rand.Reader, &template, &template, &priv.PublicKey, priv)
	if log.Check(log.WarnLevel, "Creating certificate", err) {
		return
	}

	os.MkdirAll(config.Agent.DataPrefix+"ssl", 0700)

	certOut, err := os.Create(config.Agent.DataPrefix + "ssl/cert.pem")
	if log.Check(log.WarnLevel, "Opening cert.pem for writing", err) {
		return
	}
	pem.Encode(certOut, &pem.Block{Type: "CERTIFICATE", Bytes: derBytes})
	certOut.Close()

	keyOut, err := os.OpenFile(config.Agent.DataPrefix+"ssl/key.pem", os.O_WRONLY|os.O_CREATE|os.O_TRUNC, 0600)
	if log.Check(log.WarnLevel, "Opening key.pem for writing", err) {
		return
	}
	pem.Encode(keyOut, &pem.Block{Type: "RSA PRIVATE KEY", Bytes: x509.MarshalPKCS1PrivateKey(priv)})
	keyOut.Close()
}

func newTLSConfig() *tls.Config {
	clientCert, err := ioutil.ReadFile(config.Agent.DataPrefix + "ssl/cert.pem")
	if log.Check(log.WarnLevel, "Checking cert.pem file", err) {
		return nil
	}
	privateKey, err := ioutil.ReadFile(config.Agent.DataPrefix + "ssl/key.pem")
	if log.Check(log.WarnLevel, "Checking key.pem file", err) {
		return nil
	}

	cert, err := tls.X509KeyPair(clientCert, privateKey)
	if log.Check(log.WarnLevel, "Loading x509 keypair", err) {
		return nil
	}

	if len(cert.Certificate) != 0 {
		cert.Leaf, err = x509.ParseCertificate(cert.Certificate[0])
		if log.Check(log.WarnLevel, "Parsing client certificates", err) {
			return nil
		}
	}

	// Create tls.Config with desired tls properties
	return &tls.Config{
		ClientAuth:         tls.NoClientCert,
		ClientCAs:          nil,
		InsecureSkipVerify: true,
		Certificates:       []tls.Certificate{cert},
	}
}
