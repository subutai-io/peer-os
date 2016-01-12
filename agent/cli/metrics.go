package lib

import (
	"encoding/json"
	"fmt"
	"github.com/influxdb/influxdb/client/v2"
	"os"
	"subutai/config"
)

var (
	tableCPU  = "host_cpu"
	tableNet  = "host_net"
	tableMem  = "host_memory"
	tableDisk = "host_disk"
)

// queryDB convenience function to query the database
func queryInfluxDB(clnt client.Client, cmd string) (res []client.Result, err error) {
	q := client.Query{
		Command:  cmd,
		Database: config.Influxdb.Db,
	}
	if response, err := clnt.Query(q); err == nil {
		if response.Error() != nil {
			return res, response.Error()
		}
		res = response.Results
	}
	return res, nil
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
	fmt.Println("{\"Metrics\":")
	res, _ := queryInfluxDB(c, `
			SELECT non_negative_derivative(mean(value),1s) as value
			FROM day.`+tableCPU+`
			WHERE hostname = '`+host+`' AND time > '`+start+`' AND time < '`+end+`'
			GROUP BY time(1m), type fill(none);

			SELECT non_negative_derivative(mean(value),1s) as value
			FROM day.`+tableNet+`
			WHERE hostname = '`+host+`' AND time > '`+start+`' AND time < '`+end+`'
			GROUP BY time(1m), iface, type fill(none);

			SELECT mean(value) as value
			FROM day.`+tableMem+`
			WHERE hostname = '`+host+`' AND time > '`+start+`' AND time < '`+end+`'
			GROUP BY time(1m), type fill(none);

			SELECT mean(value) as value
			FROM day.`+tableDisk+`
			WHERE hostname = '`+host+`' AND time > '`+start+`' AND time < '`+end+`'
			GROUP BY time(1m), mount, type fill(none);
		`)
	out, _ := json.Marshal(res)
	fmt.Println(string(out))
	fmt.Println("}")
}
