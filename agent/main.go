package main

import (
	"os"

	"github.com/subutai-io/base/agent/agent"
	"github.com/subutai-io/base/agent/cli"
	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/log"

	"github.com/codegangsta/cli"
)

var Version string = "unknown"

func init() {
	if os.Getuid() != 0 {
		log.Error("Please run as root")
	}
	os.Setenv("PATH", "/apps/subutai/current/bin:"+os.Getenv("PATH"))
	if len(os.Args) > 1 {
		if os.Args[1] == "-d" {
			log.Level(log.DebugLevel)
		}
	}
}

func main() {
	app := cli.NewApp()
	app.Name = "Subutai"
	app.Version = Version
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
		Action: func(c *cli.Context) error {
			lib.LxcAttach(c.Args().Get(0), c.Bool("c"), c.Bool("x"), c.Bool("r"))
			return nil
		}}, {

		Name: "backup", Usage: "backup Subutai container",
		Flags: []cli.Flag{
			cli.BoolFlag{Name: "full", Usage: "make full backup"},
			cli.BoolFlag{Name: "stop", Usage: "stop container at the time of backup"}},
		Action: func(c *cli.Context) error {
			lib.BackupContainer(c.Args().Get(0), c.Bool("full"), c.Bool("stop"))
			return nil
		}}, {

		Name: "batch", Usage: "batch commands execution",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "json", Usage: "JSON string with commands"}},
		Action: func(c *cli.Context) error {
			lib.Batch(c.String("json"))
			return nil
		}}, {

		Name: "clone", Usage: "clone Subutai container",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "e", Usage: "set environment id for container"},
			cli.StringFlag{Name: "i", Usage: "set container IP address and VLAN"},
			cli.StringFlag{Name: "t", Usage: "token to verify with MH"}},
		Action: func(c *cli.Context) error {
			lib.LxcClone(c.Args().Get(0), c.Args().Get(1), c.String("e"), c.String("i"), c.String("t"))
			return nil
		}}, {

		Name: "cleanup", Usage: "clean Subutai environment",
		Action: func(c *cli.Context) error {
			lib.Cleanup(c.Args().Get(0))
			return nil
		}}, {

		Name: "config", Usage: "containerName add/del key value",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "o", Usage: "add/del key value"},
			cli.StringFlag{Name: "k", Usage: "add/del key value"},
			cli.StringFlag{Name: "v", Usage: "add/del key value"},
		},
		Action: func(c *cli.Context) error {
			lib.LxcConfig(c.Args().Get(0), c.String("o"), c.String("k"), c.String("v"))
			return nil
		}}, {

		Name: "daemon", Usage: "start an agent",
		Action: func(c *cli.Context) error {
			config.InitAgentDebug()
			agent.Start(c)
			return nil
		}}, {

		Name: "demote", Usage: "demote Subutai container",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "i", Usage: "network value ie 192.168.1.1/24"},
			cli.StringFlag{Name: "v", Usage: "vlan id"},
		},
		Action: func(c *cli.Context) error {
			lib.LxcDemote(c.Args().Get(0), c.String("i"), c.String("v"))
			return nil
		}}, {

		Name: "destroy", Usage: "destroy Subutai container",
		Action: func(c *cli.Context) error {
			lib.LxcDestroy(c.Args().Get(0))
			return nil
		}}, {

		Name: "export", Usage: "export Subutai container",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "v", Usage: "template version"}},
		Action: func(c *cli.Context) error {
			lib.LxcExport(c.Args().Get(0), c.String("v"))
			return nil
		}}, {

		Name: "import", Usage: "import Subutai template",
		Flags: []cli.Flag{
			cli.BoolFlag{Name: "torrent", Usage: "use BitTorrent for downloading (experimental)"},
			cli.StringFlag{Name: "v", Usage: "template version"},
			cli.StringFlag{Name: "t", Usage: "token to access kurjun repo"}},
		Action: func(c *cli.Context) error {
			lib.LxcImport(c.Args().Get(0), c.String("v"), c.String("t"), c.Bool("torrent"))
			return nil
		}}, {

		Name: "hostname", Usage: "Set hostname of container or host",
		Action: func(c *cli.Context) error {
			if len(c.Args().Get(1)) != 0 {
				lib.LxcHostname(c.Args().Get(0), c.Args().Get(1))
			} else {
				lib.Hostname(c.Args().Get(0))
			}
			return nil
		}}, {

		Name: "list", Usage: "list Subutai container",
		Flags: []cli.Flag{
			cli.BoolFlag{Name: "c", Usage: "containers only"},
			cli.BoolFlag{Name: "t", Usage: "templates only"},
			cli.BoolFlag{Name: "i", Usage: "detailed container info"},
			cli.BoolFlag{Name: "a", Usage: "with ancestors"},
			cli.BoolFlag{Name: "p", Usage: "with parent"}},
		Action: func(c *cli.Context) error {
			lib.LxcList(c.Args().Get(0), c.Bool("c"), c.Bool("t"), c.Bool("i"), c.Bool("a"), c.Bool("p"))
			return nil
		}}, {

		Name: "management_network", Usage: "configure management network",
		Flags: []cli.Flag{
			cli.BoolFlag{Name: "listtunnel, l", Usage: "-l"},
			cli.StringFlag{Name: "createtunnel, c", Usage: "-c TUNNELPORTNAME TUNNELIPADDRESS TUNNELTYPE"},

			cli.BoolFlag{Name: "listvnimap, v", Usage: "-v"},
			cli.StringFlag{Name: "createvnimap, m", Usage: "-m TUNNELPORTNAME VNI VLANID ENV_ID"},
		},

		Subcommands: []cli.Command{
			{
				Name:  "tunnel",
				Usage: "tunnels operation",
				Flags: []cli.Flag{
					cli.StringFlag{Name: "create", Usage: "create tunnel (tunnel -c)"},
					cli.StringFlag{Name: "delete", Usage: "delete tunnel (tunnel -d)"},
					cli.BoolFlag{Name: "list", Usage: "list of tunnels (tunnel -l)"},

					cli.StringFlag{Name: "remoteip", Usage: "remote ip"},
					cli.StringFlag{Name: "vlan", Usage: "tunnel vlan"},
					cli.StringFlag{Name: "vni", Usage: "vni"},
				},
				Action: func(c *cli.Context) error {
					lib.VxlanTunnel(c.String("create"), c.String("delete"), c.String("remoteip"), c.String("vlan"), c.String("vni"), c.Bool("list"))
					return nil
				}}, {

				Name:  "detect",
				Usage: "detect resource host IP",
				Action: func(c *cli.Context) error {
					lib.DetectIp()
					return nil
				}}, {

				Name:  "p2p",
				Usage: "p2p network operation",
				Flags: []cli.Flag{
					cli.BoolFlag{Name: "c", Usage: "create p2p instance (p2p -c interfaceName hash key ttl localPeepIPAddr portRange)"},
					cli.BoolFlag{Name: "d", Usage: "delete p2p instance (p2p -d hash)"},
					cli.BoolFlag{Name: "u", Usage: "update p2p instance encryption key (p2p -u hash newkey ttl)"},
					cli.BoolFlag{Name: "l", Usage: "list of p2p instances (p2p -l)"},
					cli.BoolFlag{Name: "p", Usage: "list of p2p participants (p2p -p hash)"},
					cli.BoolFlag{Name: "v", Usage: "print p2p version (p2p -v)"}},
				Action: func(c *cli.Context) error {
					if c.Bool("v") {
						lib.P2Pversion()
					} else {
						lib.P2P(c.Bool("c"), c.Bool("d"), c.Bool("u"), c.Bool("l"), c.Bool("p"), os.Args)
					}
					return nil
				}}},

		Action: func(c *cli.Context) error {
			lib.LxcManagementNetwork(os.Args)
			return nil
		}}, {

		Name: "metrics", Usage: "list Subutai container",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "s", Usage: "start time"},
			cli.StringFlag{Name: "e", Usage: "end time"}},
		Action: func(c *cli.Context) error {
			lib.HostMetrics(c.Args().Get(0), c.String("s"), c.String("e"))
			return nil
		}}, {

		Name: "network", Usage: "containerName set/remove/list network vlan id",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "set, s", Usage: "IPADDRESS/NETMASK"},
			cli.StringFlag{Name: "vlan, v", Usage: "vlanid"},
			cli.BoolFlag{Name: "remove, r", Usage: ""},
			cli.BoolFlag{Name: "list, l", Usage: ""},
		},
		Action: func(c *cli.Context) error {
			lib.LxcNetwork(c.Args().Get(0), c.String("s"), c.String("vlan"), c.Bool("r"), c.Bool("l"))
			return nil
		}}, {

		Name: "promote", Usage: "promote Subutai container",
		Action: func(c *cli.Context) error {
			lib.LxcPromote(c.Args().Get(0))
			return nil
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
				Action: func(c *cli.Context) error {
					lib.ProxyAdd(c.Args().Get(0), c.String("d"), c.String("h"), c.String("p"), c.String("f"))
					return nil
				},
			},
			{
				Name:     "del",
				Usage:    "del reverse proxy component",
				HideHelp: true,
				Flags: []cli.Flag{
					cli.BoolFlag{Name: "domain, d", Usage: "delete domain from vlan"},
					cli.StringFlag{Name: "host, h", Usage: "delete host from domain on vlan"}},
				Action: func(c *cli.Context) error {
					lib.ProxyDel(c.Args().Get(0), c.String("h"), c.Bool("d"))
					return nil
				},
			},
			{
				Name:     "check",
				Usage:    "check existing domain or host",
				HideHelp: true,
				Flags: []cli.Flag{
					cli.BoolFlag{Name: "domain, d", Usage: "check domains on vlan"},
					cli.StringFlag{Name: "host, h", Usage: "check hosts on vlan"}},
				Action: func(c *cli.Context) error {
					lib.ProxyCheck(c.Args().Get(0), c.String("h"), c.Bool("d"))
					return nil
				},
			},
		}}, {

		Name: "quota", Usage: "set quotas for Subutai container",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "s", Usage: "set quota for the specified resource type"},
			cli.StringFlag{Name: "t", Usage: "set alert threshold"}},
		Action: func(c *cli.Context) error {
			lib.LxcQuota(c.Args().Get(0), c.Args().Get(1), c.String("s"), c.String("t"))
			return nil
		}}, {

		Name: "register", Usage: "register Subutai container",
		Action: func(c *cli.Context) error {
			lib.LxcRegister(c.Args().Get(0))
			return nil
		}}, {

		Name: "rename", Usage: "rename Subutai container",
		Action: func(c *cli.Context) error {
			lib.LxcRename(c.Args().Get(0), c.Args().Get(1))
			return nil
		}}, {

		Name: "restore", Usage: "restore Subutai container",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "d", Usage: "date of backup snapshot"},
			cli.StringFlag{Name: "c", Usage: "name of new container"}},
		Action: func(c *cli.Context) error {
			lib.RestoreContainer(c.Args().Get(0), c.Args().Get(1), c.Args().Get(2))
			return nil
		}}, {

		Name: "stats", Usage: "statistics from host",
		Action: func(c *cli.Context) error {
			lib.Stats(c.Args().Get(0), c.Args().Get(1), c.Args().Get(2))
			return nil
		}}, {

		Name: "start", Usage: "start Subutai container",
		Action: func(c *cli.Context) error {
			lib.LxcStart(c.Args().Get(0))
			return nil
		}}, {

		Name: "stop", Usage: "stop Subutai container",
		Action: func(c *cli.Context) error {
			lib.LxcStop(c.Args().Get(0))
			return nil
		}}, {

		Name: "tunnel", Usage: "SSH tunnel management",
		Subcommands: []cli.Command{
			{
				Name:  "add",
				Usage: "add ssh tunnel",
				Flags: []cli.Flag{
					cli.BoolFlag{Name: "g", Usage: "create tunnel to global proxy"}},
				Action: func(c *cli.Context) error {
					lib.TunAdd(c.Args().Get(0), c.Args().Get(1), c.Bool("g"))
					return nil
				}}, {
				Name:  "del",
				Usage: "delete tunnel",
				Action: func(c *cli.Context) error {
					lib.TunDel(c.Args().Get(0))
					return nil
				}}, {
				Name:  "list",
				Usage: "list active ssh tunnels",
				Action: func(c *cli.Context) error {
					lib.TunList()
					return nil
				}},
		}}, {

		Name: "unregister", Usage: "unregister Subutai container",
		Action: func(c *cli.Context) error {
			lib.LxcUnregister(c.Args().Get(0))
			return nil
		}}, {

		Name: "update", Usage: "update Subutai management, container or Resource host",
		Flags: []cli.Flag{
			cli.BoolFlag{Name: "c", Usage: "check for updates without installation"}},
		Action: func(c *cli.Context) error {
			lib.Update(c.Args().Get(0), c.Bool("c"))
			return nil
		}},
	}

	app.Run(os.Args)
}
