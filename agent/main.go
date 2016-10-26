package main

import (
	"os"
	"subutai/cli"

	"github.com/subutai-io/base/agent/agent"
	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/log"

	gcli "github.com/codegangsta/cli"
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
	app := gcli.NewApp()
	app.Name = "Subutai"
	if len(config.Template.Branch) != 0 {
		commit = config.Template.Branch + "/" + commit
	}
	app.Version = version + " " + commit
	app.Usage = "daemon and command line interface binary"

	app.Flags = []gcli.Flag{gcli.BoolFlag{
		Name:  "d",
		Usage: "debug mode"}}

	app.Commands = []gcli.Command{{
		Name: "attach", Usage: "attach to Subutai container",
		Flags: []gcli.Flag{
			gcli.BoolFlag{Name: "clear, c", Usage: "clear environment"},
			gcli.BoolFlag{Name: "x86, x", Usage: "use x86 personality"},
			gcli.BoolFlag{Name: "regular, r", Usage: "connect as regular user"}},
		Action: func(c *gcli.Context) error {
			cli.LxcAttach(c.Args().Get(0), c.Bool("c"), c.Bool("x"), c.Bool("r"))
			return nil
		}}, {

		Name: "backup", Usage: "backup Subutai container",
		Flags: []gcli.Flag{
			gcli.BoolFlag{Name: "full, f", Usage: "make full backup"},
			gcli.BoolFlag{Name: "stop, s", Usage: "stop container at the time of backup"}},
		Action: func(c *gcli.Context) error {
			cli.BackupContainer(c.Args().Get(0), c.Bool("f"), c.Bool("s"))
			return nil
		}}, {

		Name: "batch", Usage: "batch commands execution",
		Flags: []gcli.Flag{
			gcli.StringFlag{Name: "json, j", Usage: "JSON string with commands"}},
		Action: func(c *gcli.Context) error {
			cli.Batch(c.String("j"))
			return nil
		}}, {

		Name: "clone", Usage: "clone Subutai container",
		Flags: []gcli.Flag{
			gcli.StringFlag{Name: "env, e", Usage: "set environment id for container"},
			gcli.StringFlag{Name: "ipaddr, i", Usage: "set container IP address and VLAN"},
			gcli.StringFlag{Name: "token, t", Usage: "token to verify with MH"}},
		Action: func(c *gcli.Context) error {
			cli.LxcClone(c.Args().Get(0), c.Args().Get(1), c.String("e"), c.String("i"), c.String("t"))
			return nil
		}}, {

		Name: "cleanup", Usage: "clean Subutai environment",
		Action: func(c *gcli.Context) error {
			cli.Cleanup(c.Args().Get(0))
			return nil
		}}, {

		Name: "config", Usage: "edit container config",
		Flags: []gcli.Flag{
			gcli.StringFlag{Name: "operation, o", Usage: "<add|del> operation"},
			gcli.StringFlag{Name: "key, k", Usage: "configuration key"},
			gcli.StringFlag{Name: "value, v", Usage: "configuration value"},
		},
		Action: func(c *gcli.Context) error {
			cli.LxcConfig(c.Args().Get(0), c.String("o"), c.String("k"), c.String("v"))
			return nil
		}}, {

		Name: "daemon", Usage: "start Subutai agent",
		Action: func(c *gcli.Context) error {
			config.InitAgentDebug()
			agent.Start(c)
			return nil
		}}, {

		Name: "demote", Usage: "demote Subutai container",
		Flags: []gcli.Flag{
			gcli.StringFlag{Name: "ipaddr, i", Usage: "IPv4 address, ie 192.168.1.1/24"},
			gcli.StringFlag{Name: "vlan, v", Usage: "VLAN tag"},
		},
		Action: func(c *gcli.Context) error {
			cli.LxcDemote(c.Args().Get(0), c.String("i"), c.String("v"))
			return nil
		}}, {

		Name: "destroy", Usage: "destroy Subutai container",
		Action: func(c *gcli.Context) error {
			cli.LxcDestroy(c.Args().Get(0))
			return nil
		}}, {

		Name: "export", Usage: "export Subutai container",
		Flags: []gcli.Flag{
			gcli.StringFlag{Name: "version, v", Usage: "template version"},
			gcli.StringFlag{Name: "size, s", Usage: "template preferred size"}},
		Action: func(c *gcli.Context) error {
			cli.LxcExport(c.Args().Get(0), c.String("v"), c.String("s"))
			return nil
		}}, {

		Name: "import", Usage: "import Subutai template",
		Flags: []gcli.Flag{
			gcli.BoolFlag{Name: "torrent", Usage: "use BitTorrent for downloading (experimental)"},
			gcli.StringFlag{Name: "v", Usage: "template version"},
			gcli.StringFlag{Name: "token, t", Usage: "token to access private repo"}},
		Action: func(c *gcli.Context) error {
			cli.LxcImport(c.Args().Get(0), c.String("v"), c.String("t"), c.Bool("torrent"))
			return nil
		}}, {

		Name: "info", Usage: "information about host system",
		Action: func(c *gcli.Context) error {
			cli.Info(c.Args().Get(0), c.Args().Get(1), c.Args().Get(2))
			return nil
		}}, {

		Name: "hostname", Usage: "Set hostname of container or host",
		Action: func(c *gcli.Context) error {
			if len(c.Args().Get(1)) != 0 {
				cli.LxcHostname(c.Args().Get(0), c.Args().Get(1))
			} else {
				cli.Hostname(c.Args().Get(0))
			}
			return nil
		}}, {

		Name: "list", Usage: "list Subutai container",
		Flags: []gcli.Flag{
			gcli.BoolFlag{Name: "container, c", Usage: "containers only"},
			gcli.BoolFlag{Name: "template, t", Usage: "templates only"},
			gcli.BoolFlag{Name: "info, i", Usage: "detailed container info"},
			gcli.BoolFlag{Name: "ancestor, a", Usage: "with ancestors"},
			gcli.BoolFlag{Name: "parent, p", Usage: "with parent"}},
		Action: func(c *gcli.Context) error {
			cli.LxcList(c.Args().Get(0), c.Bool("c"), c.Bool("t"), c.Bool("i"), c.Bool("a"), c.Bool("p"))
			return nil
		}}, {

		Name: "management_network", Usage: "configure management network",
		Subcommands: []gcli.Command{
			{
				Name:  "tunnel",
				Usage: "tunnels operation",
				Flags: []gcli.Flag{
					gcli.StringFlag{Name: "create, c", Usage: "create vxlan tunnel"},
					gcli.StringFlag{Name: "delete, d", Usage: "delete vxlan tunnel"},
					gcli.BoolFlag{Name: "list, l", Usage: "list vxlan tunnels"},

					gcli.StringFlag{Name: "remoteip, r", Usage: "vxlan tunnel remote ip"},
					gcli.StringFlag{Name: "vlan, vl", Usage: "tunnel vlan"},
					gcli.StringFlag{Name: "vni, v", Usage: "vxlan tunnel vni"},
				},
				Action: func(c *gcli.Context) error {
					cli.VxlanTunnel(c.String("c"), c.String("d"), c.String("r"), c.String("vl"), c.String("v"), c.Bool("l"))
					return nil
				}}, {

				Name:  "detect",
				Usage: "detect resource host IP",
				Action: func(c *gcli.Context) error {
					cli.Info("ipaddr", "", "")
					return nil
				}}, {

				Name:  "p2p",
				Usage: "p2p network operation",
				Flags: []gcli.Flag{
					gcli.BoolFlag{Name: "create, c", Usage: "create p2p instance (interfaceName hash key ttl localPeepIPAddr portRange)"},
					gcli.BoolFlag{Name: "delete, d", Usage: "delete p2p instance by swarm hash"},
					gcli.BoolFlag{Name: "update, u", Usage: "update p2p instance encryption key (hash newkey ttl)"},
					gcli.BoolFlag{Name: "list, l", Usage: "list of p2p instances"},
					gcli.BoolFlag{Name: "peers, p", Usage: "list of p2p swarm participants by hash"},
					gcli.BoolFlag{Name: "version, v", Usage: "print p2p version"}},
				Action: func(c *gcli.Context) error {
					if c.Bool("v") {
						cli.P2Pversion()
					} else {
						cli.P2P(c.Bool("c"), c.Bool("d"), c.Bool("u"), c.Bool("l"), c.Bool("p"), os.Args)
					}
					return nil
				}}},
	}, {

		Name: "metrics", Usage: "list Subutai container",
		Flags: []gcli.Flag{
			gcli.StringFlag{Name: "start, s", Usage: "start time"},
			gcli.StringFlag{Name: "end, e", Usage: "end time"}},
		Action: func(c *gcli.Context) error {
			cli.HostMetrics(c.Args().Get(0), c.String("s"), c.String("e"))
			return nil
		}}, {

		Name: "p2p", Usage: "P2P network operations",
		Flags: []gcli.Flag{
			gcli.BoolFlag{Name: "create, c", Usage: "create p2p instance (interfaceName hash key ttl localPeepIPAddr portRange)"},
			gcli.BoolFlag{Name: "delete, d", Usage: "delete p2p instance by swarm hash"},
			gcli.BoolFlag{Name: "update, u", Usage: "update p2p instance encryption key (hash newkey ttl)"},
			gcli.BoolFlag{Name: "list, l", Usage: "list of p2p instances"},
			gcli.BoolFlag{Name: "peers, p", Usage: "list of p2p swarm participants by hash"},
			gcli.BoolFlag{Name: "version, v", Usage: "print p2p version"}},
		Action: func(c *gcli.Context) error {
			if c.Bool("v") {
				cli.P2Pversion()
			} else {
				os.Args = append([]string{""}, os.Args...) //workaround to keep compatibility for management_network and p2p bindings
				cli.P2P(c.Bool("c"), c.Bool("d"), c.Bool("u"), c.Bool("l"), c.Bool("p"), os.Args)
			}
			return nil
		}}, {

		Name: "promote", Usage: "promote Subutai container",
		Action: func(c *gcli.Context) error {
			cli.LxcPromote(c.Args().Get(0))
			return nil
		}}, {

		Name: "proxy", Usage: "Subutai reverse proxy",
		Subcommands: []gcli.Command{
			{
				Name:     "add",
				Usage:    "add reverse proxy component",
				HideHelp: true,
				Flags: []gcli.Flag{
					gcli.StringFlag{Name: "domain, d", Usage: "add domain to vlan"},
					gcli.StringFlag{Name: "host, h", Usage: "add host to domain on vlan"},
					gcli.StringFlag{Name: "policy, p", Usage: "set load balance policy (rr|lb|hash)"},
					gcli.StringFlag{Name: "file, f", Usage: "specify pem certificate file"}},
				Action: func(c *gcli.Context) error {
					cli.ProxyAdd(c.Args().Get(0), c.String("d"), c.String("h"), c.String("p"), c.String("f"))
					return nil
				},
			},
			{
				Name:     "del",
				Usage:    "del reverse proxy component",
				HideHelp: true,
				Flags: []gcli.Flag{
					gcli.BoolFlag{Name: "domain, d", Usage: "delete domain from vlan"},
					gcli.StringFlag{Name: "host, h", Usage: "delete host from domain on vlan"}},
				Action: func(c *gcli.Context) error {
					cli.ProxyDel(c.Args().Get(0), c.String("h"), c.Bool("d"))
					return nil
				},
			},
			{
				Name:     "check",
				Usage:    "check existing domain or host",
				HideHelp: true,
				Flags: []gcli.Flag{
					gcli.BoolFlag{Name: "domain, d", Usage: "check domains on vlan"},
					gcli.StringFlag{Name: "host, h", Usage: "check hosts on vlan"}},
				Action: func(c *gcli.Context) error {
					cli.ProxyCheck(c.Args().Get(0), c.String("h"), c.Bool("d"))
					return nil
				},
			},
		}}, {

		Name: "quota", Usage: "set quotas for Subutai container",
		Flags: []gcli.Flag{
			gcli.StringFlag{Name: "set, s", Usage: "set quota for the specified resource type (cpu, cpuset, ram, disk, network)"},
			gcli.StringFlag{Name: "threshold, t", Usage: "set alert threshold"}},
		Action: func(c *gcli.Context) error {
			cli.LxcQuota(c.Args().Get(0), c.Args().Get(1), c.String("s"), c.String("t"))
			return nil
		}}, {

		Name: "rename", Usage: "rename Subutai container",
		Action: func(c *gcli.Context) error {
			cli.LxcRename(c.Args().Get(0), c.Args().Get(1))
			return nil
		}}, {

		Name: "restore", Usage: "restore Subutai container",
		Flags: []gcli.Flag{
			gcli.StringFlag{Name: "date, d", Usage: "date of backup snapshot"},
			gcli.StringFlag{Name: "container, c", Usage: "name of new container"}},
		Action: func(c *gcli.Context) error {
			cli.RestoreContainer(c.Args().Get(0), c.String("d"), c.String("c"))
			return nil
		}}, {

		Name: "stats", Usage: "statistics from host",
		Action: func(c *gcli.Context) error {
			cli.Info(c.Args().Get(0), c.Args().Get(1), c.Args().Get(2))
			return nil
		}}, {

		Name: "start", Usage: "start Subutai container",
		Action: func(c *gcli.Context) error {
			cli.LxcStart(c.Args().Get(0))
			return nil
		}}, {

		Name: "stop", Usage: "stop Subutai container",
		Action: func(c *gcli.Context) error {
			cli.LxcStop(c.Args().Get(0))
			return nil
		}}, {

		Name: "tunnel", Usage: "SSH tunnel management",
		Subcommands: []gcli.Command{
			{
				Name:  "add",
				Usage: "add ssh tunnel",
				Flags: []gcli.Flag{
					gcli.BoolFlag{Name: "global, g", Usage: "create tunnel to global proxy"}},
				Action: func(c *gcli.Context) error {
					cli.TunAdd(c.Args().Get(0), c.Args().Get(1), c.Bool("g"))
					return nil
				}}, {
				Name:  "del",
				Usage: "delete tunnel",
				Action: func(c *gcli.Context) error {
					cli.TunDel(c.Args().Get(0))
					return nil
				}}, {
				Name:  "list",
				Usage: "list active ssh tunnels",
				Action: func(c *gcli.Context) error {
					cli.TunList()
					return nil
				}},
		}}, {

		Name: "update", Usage: "update Subutai management, container or Resource host",
		Flags: []gcli.Flag{
			gcli.BoolFlag{Name: "check, c", Usage: "check for updates without installation"}},
		Action: func(c *gcli.Context) error {
			cli.Update(c.Args().Get(0), c.Bool("c"))
			return nil
		}}, {

		Name: "vxlan", Usage: "VXLAN tunnels operation",
		Flags: []gcli.Flag{
			gcli.StringFlag{Name: "create, c", Usage: "create vxlan tunnel"},
			gcli.StringFlag{Name: "delete, d", Usage: "delete vxlan tunnel"},
			gcli.BoolFlag{Name: "list, l", Usage: "list vxlan tunnels"},

			gcli.StringFlag{Name: "remoteip, r", Usage: "vxlan tunnel remote ip"},
			gcli.StringFlag{Name: "vlan, vl", Usage: "tunnel vlan"},
			gcli.StringFlag{Name: "vni, v", Usage: "vxlan tunnel vni"},
		},
		Action: func(c *gcli.Context) error {
			cli.VxlanTunnel(c.String("c"), c.String("d"), c.String("r"), c.String("vl"), c.String("v"), c.Bool("l"))
			return nil
		}},
	}

	app.Run(os.Args)
}
