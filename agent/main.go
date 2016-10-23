package main

import (
	"os"

	"github.com/subutai-io/base/agent/agent"
	"github.com/subutai-io/base/agent/cli"
	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/log"

	"github.com/codegangsta/cli"
)

var version string = "unknown"
var commit string = "unknown"

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
	if len(config.Template.Branch) != 0 {
		commit = config.Template.Branch + "/" + commit
	}
	app.Version = version + " " + commit
	app.Usage = "daemon and command line interface binary"

	app.Flags = []cli.Flag{cli.BoolFlag{
		Name:  "d",
		Usage: "debug mode"}}

	app.Commands = []cli.Command{{
		Name: "attach", Usage: "attach to Subutai container",
		Flags: []cli.Flag{
			cli.BoolFlag{Name: "clear, c", Usage: "clear environment"},
			cli.BoolFlag{Name: "x86, x", Usage: "use x86 personality"},
			cli.BoolFlag{Name: "regular, r", Usage: "connect as regular user"}},
		Action: func(c *cli.Context) error {
			lib.LxcAttach(c.Args().Get(0), c.Bool("c"), c.Bool("x"), c.Bool("r"))
			return nil
		}}, {

		Name: "backup", Usage: "backup Subutai container",
		Flags: []cli.Flag{
			cli.BoolFlag{Name: "full, f", Usage: "make full backup"},
			cli.BoolFlag{Name: "stop, s", Usage: "stop container at the time of backup"}},
		Action: func(c *cli.Context) error {
			lib.BackupContainer(c.Args().Get(0), c.Bool("f"), c.Bool("s"))
			return nil
		}}, {

		Name: "batch", Usage: "batch commands execution",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "json, j", Usage: "JSON string with commands"}},
		Action: func(c *cli.Context) error {
			lib.Batch(c.String("j"))
			return nil
		}}, {

		Name: "clone", Usage: "clone Subutai container",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "env, e", Usage: "set environment id for container"},
			cli.StringFlag{Name: "ipaddr, i", Usage: "set container IP address and VLAN"},
			cli.StringFlag{Name: "token, t", Usage: "token to verify with MH"}},
		Action: func(c *cli.Context) error {
			lib.LxcClone(c.Args().Get(0), c.Args().Get(1), c.String("e"), c.String("i"), c.String("t"))
			return nil
		}}, {

		Name: "cleanup", Usage: "clean Subutai environment",
		Action: func(c *cli.Context) error {
			lib.Cleanup(c.Args().Get(0))
			return nil
		}}, {

		Name: "config", Usage: "edit container config",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "operation, o", Usage: "<add|del> operation"},
			cli.StringFlag{Name: "key, k", Usage: "configuration key"},
			cli.StringFlag{Name: "value, v", Usage: "configuration value"},
		},
		Action: func(c *cli.Context) error {
			lib.LxcConfig(c.Args().Get(0), c.String("o"), c.String("k"), c.String("v"))
			return nil
		}}, {

		Name: "daemon", Usage: "start Subutai agent",
		Action: func(c *cli.Context) error {
			config.InitAgentDebug()
			agent.Start(c)
			return nil
		}}, {

		Name: "demote", Usage: "demote Subutai container",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "ipaddr, i", Usage: "IPv4 address, ie 192.168.1.1/24"},
			cli.StringFlag{Name: "vlan, v", Usage: "VLAN tag"},
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
			cli.StringFlag{Name: "version, v", Usage: "template version"},
			cli.StringFlag{Name: "size, s", Usage: "template preferred size"}},
		Action: func(c *cli.Context) error {
			lib.LxcExport(c.Args().Get(0), c.String("v"), c.String("s"))
			return nil
		}}, {

		Name: "import", Usage: "import Subutai template",
		Flags: []cli.Flag{
			cli.BoolFlag{Name: "torrent", Usage: "use BitTorrent for downloading (experimental)"},
			cli.StringFlag{Name: "v", Usage: "template version"},
			cli.StringFlag{Name: "token, t", Usage: "token to access private repo"}},
		Action: func(c *cli.Context) error {
			lib.LxcImport(c.Args().Get(0), c.String("v"), c.String("t"), c.Bool("torrent"))
			return nil
		}}, {

		Name: "info", Usage: "information about host system",
		Action: func(c *cli.Context) error {
			lib.Info(c.Args().Get(0), c.Args().Get(1), c.Args().Get(2))
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
			cli.BoolFlag{Name: "container, c", Usage: "containers only"},
			cli.BoolFlag{Name: "template, t", Usage: "templates only"},
			cli.BoolFlag{Name: "info, i", Usage: "detailed container info"},
			cli.BoolFlag{Name: "ancestor, a", Usage: "with ancestors"},
			cli.BoolFlag{Name: "parent, p", Usage: "with parent"}},
		Action: func(c *cli.Context) error {
			lib.LxcList(c.Args().Get(0), c.Bool("c"), c.Bool("t"), c.Bool("i"), c.Bool("a"), c.Bool("p"))
			return nil
		}}, {

		Name: "management_network", Usage: "configure management network",
		Subcommands: []cli.Command{
			{
				Name:  "tunnel",
				Usage: "tunnels operation",
				Flags: []cli.Flag{
					cli.StringFlag{Name: "create, c", Usage: "create vxlan tunnel"},
					cli.StringFlag{Name: "delete, d", Usage: "delete vxlan tunnel"},
					cli.BoolFlag{Name: "list, l", Usage: "list vxlan tunnels"},

					cli.StringFlag{Name: "remoteip, r", Usage: "vxlan tunnel remote ip"},
					cli.StringFlag{Name: "vlan, vl", Usage: "tunnel vlan"},
					cli.StringFlag{Name: "vni, v", Usage: "vxlan tunnel vni"},
				},
				Action: func(c *cli.Context) error {
					lib.VxlanTunnel(c.String("c"), c.String("d"), c.String("r"), c.String("vl"), c.String("v"), c.Bool("l"))
					return nil
				}}, {

				Name:  "detect",
				Usage: "detect resource host IP",
				Action: func(c *cli.Context) error {
					lib.Info("ipaddr", "", "")
					return nil
				}}, {

				Name:  "p2p",
				Usage: "p2p network operation",
				Flags: []cli.Flag{
					cli.BoolFlag{Name: "create, c", Usage: "create p2p instance (interfaceName hash key ttl localPeepIPAddr portRange)"},
					cli.BoolFlag{Name: "delete, d", Usage: "delete p2p instance by swarm hash"},
					cli.BoolFlag{Name: "update, u", Usage: "update p2p instance encryption key (hash newkey ttl)"},
					cli.BoolFlag{Name: "list, l", Usage: "list of p2p instances"},
					cli.BoolFlag{Name: "peers, p", Usage: "list of p2p swarm participants by hash"},
					cli.BoolFlag{Name: "version, v", Usage: "print p2p version"}},
				Action: func(c *cli.Context) error {
					if c.Bool("v") {
						lib.P2Pversion()
					} else {
						lib.P2P(c.Bool("c"), c.Bool("d"), c.Bool("u"), c.Bool("l"), c.Bool("p"), os.Args)
					}
					return nil
				}}},
	}, {

		Name: "metrics", Usage: "list Subutai container",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "start, s", Usage: "start time"},
			cli.StringFlag{Name: "end, e", Usage: "end time"}},
		Action: func(c *cli.Context) error {
			lib.HostMetrics(c.Args().Get(0), c.String("s"), c.String("e"))
			return nil
		}}, {

		Name: "p2p", Usage: "P2P network operations",
		Flags: []cli.Flag{
			cli.BoolFlag{Name: "create, c", Usage: "create p2p instance (interfaceName hash key ttl localPeepIPAddr portRange)"},
			cli.BoolFlag{Name: "delete, d", Usage: "delete p2p instance by swarm hash"},
			cli.BoolFlag{Name: "update, u", Usage: "update p2p instance encryption key (hash newkey ttl)"},
			cli.BoolFlag{Name: "list, l", Usage: "list of p2p instances"},
			cli.BoolFlag{Name: "peers, p", Usage: "list of p2p swarm participants by hash"},
			cli.BoolFlag{Name: "version, v", Usage: "print p2p version"}},
		Action: func(c *cli.Context) error {
			if c.Bool("v") {
				lib.P2Pversion()
			} else {
				os.Args = append([]string{""}, os.Args...) //workaround to keep compatibility for management_network and p2p bindings
				lib.P2P(c.Bool("c"), c.Bool("d"), c.Bool("u"), c.Bool("l"), c.Bool("p"), os.Args)
			}
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
					cli.StringFlag{Name: "domain, d", Usage: "add domain to vlan"},
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
			cli.StringFlag{Name: "set, s", Usage: "set quota for the specified resource type (cpu, cpuset, ram, disk, network)"},
			cli.StringFlag{Name: "threshold, t", Usage: "set alert threshold"}},
		Action: func(c *cli.Context) error {
			lib.LxcQuota(c.Args().Get(0), c.Args().Get(1), c.String("s"), c.String("t"))
			return nil
		}}, {

		Name: "rename", Usage: "rename Subutai container",
		Action: func(c *cli.Context) error {
			lib.LxcRename(c.Args().Get(0), c.Args().Get(1))
			return nil
		}}, {

		Name: "restore", Usage: "restore Subutai container",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "date, d", Usage: "date of backup snapshot"},
			cli.StringFlag{Name: "container, c", Usage: "name of new container"}},
		Action: func(c *cli.Context) error {
			lib.RestoreContainer(c.Args().Get(0), c.String("d"), c.String("c"))
			return nil
		}}, {

		Name: "stats", Usage: "statistics from host",
		Action: func(c *cli.Context) error {
			lib.Info(c.Args().Get(0), c.Args().Get(1), c.Args().Get(2))
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
					cli.BoolFlag{Name: "global, g", Usage: "create tunnel to global proxy"}},
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

		Name: "update", Usage: "update Subutai management, container or Resource host",
		Flags: []cli.Flag{
			cli.BoolFlag{Name: "check, c", Usage: "check for updates without installation"}},
		Action: func(c *cli.Context) error {
			lib.Update(c.Args().Get(0), c.Bool("c"))
			return nil
		}}, {

		Name: "vxlan", Usage: "VXLAN tunnels operation",
		Flags: []cli.Flag{
			cli.StringFlag{Name: "create, c", Usage: "create vxlan tunnel"},
			cli.StringFlag{Name: "delete, d", Usage: "delete vxlan tunnel"},
			cli.BoolFlag{Name: "list, l", Usage: "list vxlan tunnels"},

			cli.StringFlag{Name: "remoteip, r", Usage: "vxlan tunnel remote ip"},
			cli.StringFlag{Name: "vlan, vl", Usage: "tunnel vlan"},
			cli.StringFlag{Name: "vni, v", Usage: "vxlan tunnel vni"},
		},
		Action: func(c *cli.Context) error {
			lib.VxlanTunnel(c.String("c"), c.String("d"), c.String("r"), c.String("vl"), c.String("v"), c.Bool("l"))
			return nil
		}},
	}

	app.Run(os.Args)
}
