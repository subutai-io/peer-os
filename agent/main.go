package main

import (
	"os"

	"github.com/subutai-io/Subutai/agent/agent"
	"github.com/subutai-io/Subutai/agent/cli"
	"github.com/subutai-io/Subutai/agent/config"
	"github.com/subutai-io/Subutai/agent/log"

	"github.com/codegangsta/cli"
)

var TIMESTAMP string = "-unknown"

func init() {
	os.Setenv("PATH", "/apps/subutai/current/bin:/apps/subutai-mng/current/bin:"+os.Getenv("PATH"))
	if len(os.Args) > 1 {
		if os.Args[1] == "-d" {
			log.Level(log.DebugLevel)
		}
	}
}

func main() {
	app := cli.NewApp()
	app.Name = "Subutai"
	app.Version = "v4.0.0-RC6" + TIMESTAMP
	app.Usage = "daemon and command line interface binary"

	app.Flags = []cli.Flag{cli.BoolFlag{
		Name:  "d",
		Usage: "debug mode"}}

	app.Commands = []cli.Command{{
		Name: "attach", Usage: "attach to container",
		Flags: []cli.Flag{
			cli.BoolFlag{Name: "c", Usage: "clear environment"},
			cli.BoolFlag{Name: "x", Usage: "use x86 personality"},
			cli.BoolFlag{Name: "r", Usage: "connect as regular user"}},
		Action: func(c *cli.Context) {
			lib.LxcAttach(c.Args().Get(0), c.Bool("c"), c.Bool("x"), c.Bool("r"))
		}}, {

		Name: "batch", Usage: "batch commands execution",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "json", Usage: "JSON string with commands"}},
		Action: func(c *cli.Context) {
			lib.Batch(c.String("json"))
		}}, {

		Name: "clone", Usage: "clone Subutai container",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "e", Usage: "set environment id for container"},
			cli.StringFlag{Name: "i", Usage: "set container IP address and VLAN"},
			cli.StringFlag{Name: "t", Usage: "token to verify with MH"}},
		Action: func(c *cli.Context) {
			lib.LxcClone(c.Args().Get(0), c.Args().Get(1), c.String("e"), c.String("i"), c.String("t"))
		}}, {

		Name: "cleanup", Usage: "clean Subutai environment",
		Action: func(c *cli.Context) {
			lib.Cleanup(c.Args().Get(0))
		}}, {

		Name: "collect", Usage: "collect performance stats",
		Action: func(c *cli.Context) {
			lib.CollectStats()
		}}, {

		Name: "config", Usage: "containerName add/del key value",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "o", Usage: "add/del key value"},
			cli.StringFlag{Name: "k", Usage: "add/del key value"},
			cli.StringFlag{Name: "v", Usage: "add/del key value"},
		},
		Action: func(c *cli.Context) {
			lib.LxcConfig(c.Args().Get(0), c.String("o"), c.String("k"), c.String("v"))
		}}, {

		Name: "daemon", Usage: "start an agent",
		Action: func(c *cli.Context) {
			config.InitAgentDebug()
			agent.Start(c)
		},
		Flags: []cli.Flag{
			cli.StringFlag{Name: "server", Value: config.Management.Host, Usage: "management host ip address/host name"},
			cli.StringFlag{Name: "port", Value: config.Management.Port, Usage: "management host port number"},
			cli.StringFlag{Name: "user", Value: config.Agent.GpgUser, Usage: "gpg user name/email to encrypt/decrypt messages"},
			cli.StringFlag{Name: "secret", Value: config.Management.Secret, Usage: "send secret passphrase via flag"},
		}}, {

		Name: "demote", Usage: "demote Subutai container",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "i", Usage: "network value ie 192.168.1.1/24"},
			cli.StringFlag{Name: "v", Usage: "vlan id"},
		},
		Action: func(c *cli.Context) {
			lib.LxcDemote(c.Args().Get(0), c.String("i"), c.String("v"))
		}}, {

		Name: "destroy", Usage: "destroy Subutai container",
		Action: func(c *cli.Context) {
			lib.LxcDestroy(c.Args().Get(0))
		}}, {

		Name: "export", Usage: "export Subutai container",
		Action: func(c *cli.Context) {
			lib.LxcExport(c.Args().Get(0))
		}}, {

		Name: "import", Usage: "import Subutai template",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "v", Usage: "template version"},
			cli.StringFlag{Name: "t", Usage: "token to access kurjun repo"}},
		Action: func(c *cli.Context) {
			lib.LxcImport(c.Args().Get(0), c.String("v"), c.String("t"))
		}}, {

		Name: "list", Usage: "list Subutai container",
		Flags: []cli.Flag{
			cli.BoolFlag{Name: "c", Usage: "containers only"},
			cli.BoolFlag{Name: "t", Usage: "templates only"},
			cli.BoolFlag{Name: "r", Usage: "registered only"},
			cli.BoolFlag{Name: "i", Usage: "info ???? only"},
			cli.BoolFlag{Name: "a", Usage: "with ancestors"},
			cli.BoolFlag{Name: "f", Usage: "fancy mode"},
			cli.BoolFlag{Name: "p", Usage: "with parent"}},
		Action: func(c *cli.Context) {
			lib.LxcList(c.Args().Get(0), c.Bool("c"), c.Bool("t"), c.Bool("r"), c.Bool("i"), c.Bool("a"), c.Bool("f"), c.Bool("p"))
		}}, {

		Name: "management_network", Usage: "configure management network",
		Flags: []cli.Flag{
			cli.BoolFlag{Name: "listtunnel, l", Usage: "-l"},
			cli.StringFlag{Name: "createtunnel, c", Usage: "-c TUNNELPORTNAME TUNNELIPADDRESS TUNNELTYPE"},
			cli.StringFlag{Name: "removetunnel, r", Usage: "-r tunnerPortName"},

			cli.BoolFlag{Name: "listvnimap, v", Usage: "-v"},
			cli.StringFlag{Name: "createvnimap, m", Usage: "-m TUNNELPORTNAME VNI VLANID ENV_ID"},
			cli.StringFlag{Name: "reservvni, E", Usage: "-E vni, vlanid, envid"},
			cli.StringFlag{Name: "removevni, M", Usage: "-M TUNNELPORTNAME VNI VLANID"},

			cli.StringFlag{Name: "deletegateway, D", Usage: "-D VLANID"},
			cli.StringFlag{Name: "creategateway, T", Usage: "-T VLANIP/SUBNET VLANID"},
			cli.StringFlag{Name: "vniop, Z", Usage: "-Z [deleteall] | [list]"}},

		Subcommands: []cli.Command{{
			Name:  "p2p",
			Usage: "p2p network operation",
			Flags: []cli.Flag{
				cli.BoolFlag{Name: "c", Usage: "create p2p instance (p2p -c interfaceName localPeepIPAddr hash key ttl)"},
				cli.BoolFlag{Name: "d", Usage: "delete p2p instance (p2p -d hash)"},
				cli.BoolFlag{Name: "u", Usage: "update p2p instance encryption key (p2p -u hash newkey ttl)"},
				cli.BoolFlag{Name: "l", Usage: "list of p2p instances (p2p -l)"},
				cli.BoolFlag{Name: "p", Usage: "list of p2p participants (p2p -p hash)"}},
			Action: func(c *cli.Context) {
				lib.P2P(c.Bool("c"), c.Bool("d"), c.Bool("u"), c.Bool("l"), c.Bool("p"), os.Args)
			}}},

		Action: func(c *cli.Context) {
			lib.LxcManagementNetwork(os.Args)
		}}, {

		Name: "metrics", Usage: "list Subutai container",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "s", Usage: "start time"},
			cli.StringFlag{Name: "e", Usage: "end time"}},
		Action: func(c *cli.Context) {
			lib.HostMetrics(c.Args().Get(0), c.String("s"), c.String("e"))
		}}, {

		Name: "network", Usage: "containerName set/remove/list network vlan id",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "set, s", Usage: "IPADDRESS/NETMASK"},
			cli.StringFlag{Name: "vlan, v", Usage: "vlanid"},
			cli.BoolFlag{Name: "remove, r", Usage: ""},
			cli.BoolFlag{Name: "list, l", Usage: ""},
		},
		Action: func(c *cli.Context) {
			lib.LxcNetwork(c.Args().Get(0), c.String("s"), c.String("vlan"), c.Bool("r"), c.Bool("l"))
		}}, {

		Name: "promote", Usage: "promote Subutai container",
		Action: func(c *cli.Context) {
			lib.LxcPromote(c.Args().Get(0))
		}}, {

		Name: "proxy", Usage: "Subutai reverse proxy",
		Subcommands: []cli.Command{
			{
				Name:     "add",
				Usage:    "add reverse proxy component",
				HideHelp: true,
				Flags: []cli.Flag{
					cli.StringFlag{Name: "domain,d", Usage: "add domain to vlan"},
					cli.StringFlag{Name: "host, h", Usage: "add host to domain on vlan"},
					cli.StringFlag{Name: "policy, p", Usage: "set load balance policy (rr|lb|hash)"},
					cli.StringFlag{Name: "file, f", Usage: "specify pem certificate file"}},
				Action: func(c *cli.Context) {
					lib.ProxyAdd(c.Args().Get(0), c.String("d"), c.String("h"), c.String("p"), c.String("f"))
				},
			},
			{
				Name:     "del",
				Usage:    "del reverse proxy component",
				HideHelp: true,
				Flags: []cli.Flag{
					cli.BoolFlag{Name: "domain, d", Usage: "delete domain from vlan"},
					cli.StringFlag{Name: "host, h", Usage: "delete host from domain on vlan"}},
				Action: func(c *cli.Context) {
					lib.ProxyDel(c.Args().Get(0), c.String("h"), c.Bool("d"))
				},
			},
			{
				Name:     "check",
				Usage:    "check existing domain or host",
				HideHelp: true,
				Flags: []cli.Flag{
					cli.BoolFlag{Name: "domain,d", Usage: "check domains on vlan"},
					cli.StringFlag{Name: "host, h", Usage: "check hosts on vlan"}},
				Action: func(c *cli.Context) {
					lib.ProxyCheck(c.Args().Get(0), c.String("h"), c.Bool("d"))
				},
			},
		}}, {

		Name: "quota", Usage: "set quotas for Subutai container",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "s", Usage: "set quota for the specified resource type"},
			cli.StringFlag{Name: "m", Usage: "get the maximum quota can be set to the specified container and resource_type in their default units"}},
		Action: func(c *cli.Context) {
			lib.LxcQuota(c.Args().Get(0), c.Args().Get(1), c.String("s"), c.String("m"))
		}}, {

		Name: "rename", Usage: "rename Subutai container",
		Action: func(c *cli.Context) {
			lib.LxcRename(c.Args().Get(0), c.Args().Get(1))
		}}, {

		Name: "register", Usage: "register Subutai container",
		Action: func(c *cli.Context) {
			lib.LxcRegister(c.Args().Get(0))
		}}, {

		Name: "stats", Usage: "statistics from host",
		Action: func(c *cli.Context) {
			lib.Stats(c.Args().Get(0), c.Args().Get(1), c.Args().Get(2))
		}}, {

		Name: "start", Usage: "start Subutai container",
		Action: func(c *cli.Context) {
			lib.LxcStart(c.Args().Get(0))
		}}, {

		Name: "stop", Usage: "stop Subutai container",
		Action: func(c *cli.Context) {
			lib.LxcStop(c.Args().Get(0))
		}}, {

		Name: "tunnel", Usage: "create SSH tunnel to container",
		Action: func(c *cli.Context) {
			lib.SshTunnel(c.Args().Get(0), c.Args().Get(1))
		}}, {

		Name: "unregister", Usage: "unregister Subutai container",
		Action: func(c *cli.Context) {
			lib.LxcUnregister(c.Args().Get(0))
		}}, {

		Name: "backup", Usage: "backup Subutai container",
		Flags: []cli.Flag{
			cli.BoolFlag{Name: "full", Usage: "make full backup"}},
		Action: func(c *cli.Context) {
			lib.BackupContainer(c.Args().Get(0), c.Bool("full"))
		}}, {

		Name: "restore", Usage: "restore Subutai container",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "d", Usage: "date of backup snapshot"},
			cli.StringFlag{Name: "c", Usage: "name of new container"}},
		Action: func(c *cli.Context) {
			lib.RestoreContainer(c.Args().Get(0), c.Args().Get(1), c.Args().Get(2))
		}},
	}

	app.Run(os.Args)
}
