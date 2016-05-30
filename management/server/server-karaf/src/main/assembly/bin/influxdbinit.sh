#!/bin/bash
USER="root"
PASS="root"
URL="https://localhost:8086/query"

while [ $(curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=SHOW DATABASES" | grep -c metrics) -ne 1 ]; do
	sleep 3 
	curl -sk -X POST $URL --data-urlencode "q=CREATE USER root WITH PASSWORD 'root' WITH ALL PRIVILEGES"
        curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE DATABASE metrics"
done

curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE RETENTION POLICY hour ON metrics DURATION 1h REPLICATION 1 DEFAULT" > /dev/null 2>&1
curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE RETENTION POLICY day  ON metrics DURATION 1d REPLICATION 1" > /dev/null 2>&1
curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE RETENTION POLICY week ON metrics DURATION 7d REPLICATION 1" > /dev/null 2>&1

curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE CONTINUOUS QUERY lxc_net_day ON metrics BEGIN
                SELECT mean(value) AS value INTO metrics.day.lxc_net FROM metrics.hour.lxc_net GROUP BY time(1m), hostname, type, iface END" > /dev/null 2>&1

curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE CONTINUOUS QUERY lxc_net_week ON metrics BEGIN
                SELECT mean(value) AS value INTO metrics.week.lxc_net FROM metrics.day.lxc_net GROUP BY time(5m), hostname, type, iface END" > /dev/null 2>&1

curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE CONTINUOUS QUERY lxc_cpu_day ON metrics BEGIN
                SELECT mean(value) AS value INTO metrics.day.lxc_cpu FROM metrics.hour.lxc_cpu GROUP BY time(1m), hostname, type END" > /dev/null 2>&1

curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE CONTINUOUS QUERY lxc_cpu_week ON metrics BEGIN
                SELECT mean(value) AS value INTO metrics.week.lxc_cpu FROM metrics.day.lxc_cpu GROUP BY time(5m), hostname, type END" > /dev/null 2>&1

curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE CONTINUOUS QUERY lxc_memory_day ON metrics BEGIN
                SELECT mean(value) AS value INTO metrics.day.lxc_memory FROM metrics.hour.lxc_memory GROUP BY time(1m), hostname, type END" > /dev/null 2>&1

curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE CONTINUOUS QUERY lxc_memory_week ON metrics BEGIN
                SELECT mean(value) AS value INTO metrics.week.lxc_memory FROM metrics.day.lxc_memory GROUP BY time(5m), hostname, type END" > /dev/null 2>&1

curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE CONTINUOUS QUERY lxc_disk_day ON metrics BEGIN
                SELECT mean(value) AS value INTO metrics.day.lxc_disk FROM metrics.hour.lxc_disk GROUP BY time(1m), hostname, mount, type END" > /dev/null 2>&1

curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE CONTINUOUS QUERY lxc_disk_week ON metrics BEGIN
                SELECT mean(value) AS value INTO metrics.week.lxc_disk FROM metrics.day.lxc_disk GROUP BY time(5m), hostname, mount, type END" > /dev/null 2>&1


curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE CONTINUOUS QUERY host_memory_day ON metrics BEGIN
                SELECT mean(value) AS value INTO metrics.day.host_memory FROM metrics.hour.host_memory GROUP BY time(1m), hostname, type END" > /dev/null 2>&1

curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE CONTINUOUS QUERY host_memory_week ON metrics BEGIN
                SELECT mean(value) AS value INTO metrics.week.host_memory FROM metrics.day.host_memory GROUP BY time(5m), hostname, type END" > /dev/null 2>&1

curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE CONTINUOUS QUERY host_net_day ON metrics BEGIN
                SELECT mean(value) AS value INTO metrics.day.host_net FROM metrics.hour.host_net GROUP BY time(1m), hostname, iface, type END" > /dev/null 2>&1

curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE CONTINUOUS QUERY host_net_week ON metrics BEGIN
                SELECT mean(value) AS value INTO metrics.week.host_net FROM metrics.day.host_net GROUP BY time(5m), hostname, iface, type END" > /dev/null 2>&1

curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE CONTINUOUS QUERY host_disk_day ON metrics BEGIN
                SELECT mean(value) AS value INTO metrics.day.host_disk FROM metrics.hour.host_disk GROUP BY time(1m), hostname, mount, type END" > /dev/null 2>&1

curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE CONTINUOUS QUERY host_disk_week ON metrics BEGIN
                SELECT mean(value) AS value INTO metrics.week.host_disk FROM metrics.day.host_disk GROUP BY time(5m), hostname, mount, type END" > /dev/null 2>&1

curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE CONTINUOUS QUERY host_cpu_day ON metrics BEGIN
                SELECT mean(value) AS value INTO metrics.day.host_cpu FROM metrics.hour.host_cpu GROUP BY time(1m), hostname, type END" > /dev/null 2>&1

curl -sk -X POST $URL -u $USER:$PASS --data-urlencode "q=CREATE CONTINUOUS QUERY host_cpu_week ON metrics BEGIN
                SELECT mean(value) AS value INTO metrics.week.host_cpu FROM metrics.day.host_cpu GROUP BY time(5m), hostname, type END" > /dev/null 2>&1
