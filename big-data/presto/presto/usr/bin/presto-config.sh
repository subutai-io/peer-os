#!/bin/bash

presto="presto-server-0.57"
. /etc/profile

if [ "$#" != 2 ]; then
	echo "Usage: $0 {coordinator|worker} discovery_ip"
	exit
fi

role=$1
ip=$2

case "$1" in
coordinator)
	echo "Configuring presto coordinator ..."
	cd /opt/$presto/etc
	> config.properties
	echo "coordinator=true" >> config.properties
	echo "datasources=jmx" >> config.properties
	echo "http-server.http.port=8413" >> config.properties
	echo "presto-metastore.db.type=h2" >> config.properties
	echo "presto-metastore.db.filename=/var/db/MetaStore" >> config.properties
	echo "task.max-memory=1GB" >> config.properties
	echo "discovery-server.enabled=true" >> config.properties
	echo "discovery.uri=http://$ip:8413" >> config.properties
	if [ -f "/etc/ksks-agent/config/uuid.txt" ]; 
	then 
		uuid=`cat /etc/ksks-agent/config/uuid.txt`
		sed -i "s/node.id=ffffffff-ffff-ffff-ffff-ffffffffffff/node.id=$uuid/g" node.properties
	fi
;;

worker)
	echo "Configuring presto worker ..."
	cd /opt/$presto/etc
	> config.properties
	echo "coordinator=false" >> config.properties
	echo "datasources=jmx,hive" >> config.properties
	echo "http-server.http.port=8413" >> config.properties
	echo "presto-metastore.db.type=h2" >> config.properties
	echo "presto-metastore.db.filename=/var/db/MetaStore" >> config.properties
	echo "task.max-memory=1GB" >> config.properties
	echo "discovery.uri=http://$ip:8413" >> config.properties
	if [ -f "/etc/ksks-agent/config/uuid.txt" ]; 
	then 
		uuid=`cat /etc/ksks-agent/config/uuid.txt`
		sed -i "s/node.id=ffffffff-ffff-ffff-ffff-ffffffffffff/node.id=$uuid/g" node.properties
	fi
;;

*)
	echo "Usage: $0 {coordinator|worker} discovery_ip"
	exit
esac
