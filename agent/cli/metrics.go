package lib

import (
	"encoding/json"
	"fmt"
	"os"
	"time"

	"github.com/influxdata/influxdb/client/v2"

	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/log"
)

var (
	tableCPU  = "host_cpu"
	tableNet  = "host_net"
	tableMem  = "host_memory"
	tableDisk = "host_disk"

	timeRange = "day"
	timeGroup = "5m"
)

// queryDB convenience function to query the database
func queryInfluxDB(clnt client.Client, cmd string) (res []client.Result, err error) {
	q := client.Query{
		Command:   cmd,
		Database:  config.Influxdb.Db,
		Precision: "s",
	}
	if response, err := clnt.Query(q); err == nil {
		if response.Error() != nil {
			return res, response.Error()
		}
		res = response.Results
	} else {
		return res, err
	}
	return res, nil
}

func CleanupNetStat(vlan string) {
	c, _ := client.NewHTTPClient(client.HTTPConfig{
		Addr:               "https://" + config.Influxdb.Server + ":8086",
		Username:           config.Influxdb.User,
		Password:           config.Influxdb.Pass,
		InsecureSkipVerify: true,
	})
	queryInfluxDB(c, `drop series from host_net where iface = 'p2p`+vlan+`'`)
	queryInfluxDB(c, `drop series from host_net where iface = 'gw-`+vlan+`'`)
}

func HostMetrics(host, start, end string) {
	// Make client
	c, _ := client.NewHTTPClient(client.HTTPConfig{
		Addr:               "https://" + config.Influxdb.Server + ":8086",
		Username:           config.Influxdb.User,
		Password:           config.Influxdb.Pass,
		InsecureSkipVerify: true,
	})
	hostname, _ := os.Hostname()
	if host != hostname {
		tableCPU = "lxc_cpu"
		tableNet = "lxc_net"
		tableMem = "lxc_memory"
		tableDisk = "lxc_disk"
	}
	a, err := time.Parse("2006-01-02 15:04:05", start)
	log.Check(log.ErrorLevel, "Parsing start date", err)
	b, err := time.Parse("2006-01-02 15:04:05", end)
	log.Check(log.ErrorLevel, "Parsing end date", err)

	delta := b.Sub(a)
	if delta.Hours() <= 1 {
		timeRange = "hour"
		timeGroup = "1m"
	} else if delta.Hours() > 24 {
		timeRange = "week"
		timeGroup = "20m"
	}

	fmt.Print("{\"Metrics\":")
	res, _ := queryInfluxDB(c, `
			SELECT non_negative_derivative(mean(value),1s) as value
			FROM `+timeRange+`.`+tableCPU+`
			WHERE hostname = '`+host+`' AND time > '`+start+`' AND time < '`+end+`'
			GROUP BY time(`+timeGroup+`), type fill(none);

			SELECT non_negative_derivative(mean(value),1s) as value
			FROM `+timeRange+`.`+tableNet+`
			WHERE hostname = '`+host+`' AND time > '`+start+`' AND time < '`+end+`'
			GROUP BY time(`+timeGroup+`), iface, type fill(none);

			SELECT mean(value) as value
			FROM `+timeRange+`.`+tableMem+`
			WHERE hostname = '`+host+`' AND time > '`+start+`' AND time < '`+end+`'
			GROUP BY time(`+timeGroup+`), type fill(none);

			SELECT mean(value) as value
			FROM `+timeRange+`.`+tableDisk+`
			WHERE hostname = '`+host+`' AND time > '`+start+`' AND time < '`+end+`'
			GROUP BY time(`+timeGroup+`), mount, type fill(none);
		`)
	out, _ := json.Marshal(res)
	fmt.Print(string(out))
	fmt.Println("}")
}
