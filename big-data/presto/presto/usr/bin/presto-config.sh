#!/bin/bash

prestoVer="0.73"
presto="presto-server-$prestoVer"

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
	pushd /etc/presto/
	> config.properties
	echo "coordinator=true" >> config.properties
	echo "node-scheduler.include-coordinator=false" >> config.properties
	echo "http-server.http.port=8413" >> config.properties
	echo "task.max-memory=1GB" >> config.properties
	echo "discovery-server.enabled=true" >> config.properties
	echo "discovery.uri=http://$ip:8413" >> config.properties
	if [ -f "/etc/subutai-agent/config/uuid.txt" ]; 
	then 
		uuid=`cat /etc/subutai-agent/config/uuid.txt`
		sed -i "s/node.id=ffffffff-ffff-ffff-ffff-ffffffffffff/node.id=$uuid/g" node.properties
	fi
	sed -i s/presto-server-[0-9]*.[0-9]*'\/'/presto-server-$prestoVer'\/'/g jvm.config
	popd
;;

worker)
	echo "Configuring presto worker ..."
	pushd /etc/presto/
	> config.properties
	echo "coordinator=false" >> config.properties
	echo "http-server.http.port=8413" >> config.properties
	echo "task.max-memory=1GB" >> config.properties
	echo "discovery.uri=http://$ip:8413" >> config.properties
	if [ -f "/etc/subutai-agent/config/uuid.txt" ]; 
	then 
		uuid=`cat /etc/subutai-agent/config/uuid.txt`
		sed -i "s/node.id=ffffffff-ffff-ffff-ffff-ffffffffffff/node.id=$uuid/g" node.properties
	fi
	sed -i s/presto-server-[0-9]*.[0-9]*'\/'/presto-server-$prestoVer'\/'/g jvm.config
	popd
;;

*)
	echo "Usage: $0 {coordinator|worker} discovery_ip"
	exit
esac
