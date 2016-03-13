#!/bin/bash
function finish {
	lxc-attach -n $1 -- /opt/docker2subutai/install.sh
	lxc-attach -n $1 -- sync
	sync
	subutai stop $1
	subutai promote $1
}

function check {
	name=$1
	child="test`date +%s`"	
	subutai clone $name $child
	
	IP=""
	while [ "$IP" == "" ]; do
		IP=$(lxc-attach -n $child -- ifconfig eth0 | grep "inet addr:" | cut -d: -f2 | awk '{print $1}')
		sleep 1
	done
	
	while true; do
		STATUS=$(lxc-attach -n $child -- systemctl is-active docker2subutai.service)
		echo $STATUS
		if [ "$STATUS" == "active" ]; then
			break
		fi
		if [ "$STATUS" == "failed" ] || [ "$STATUS" == "unknown" ]; then
			subutai destroy $child
			echo "`date` $name failed" >> /home/ubuntu/docker.log
			return	
		fi
		sleep 1
	done
	
	sleep 30
	STATUS=$(lxc-attach -n $child -- systemctl is-active docker2subutai.service)
	if [ "$STATUS" == "active" ]; then
		subutai export $name
		echo "`date` $name exported" >> /home/ubuntu/docker.log
	else
		echo "`date` $name failed" >> /home/ubuntu/docker.log
	fi
	
	subutai destroy $child
}

for name in $(ls /home/ubuntu/docker); do
	UBUNTU=$(grep -i "from " /home/ubuntu/docker/riak/Dockerfile | head -n1 | grep -ic ubuntu)
	DEBIAN=$(grep -i "from " /home/ubuntu/docker/riak/Dockerfile | head -n1 | grep -ic debian)
	if [ "$DEBIAN" == "1" ]; then
		subutai clone debian $name	
	elif [ "$UBUNTU" == "1" ]; then
		subutai clone master $name
	fi

	IP=""
	while [ "$IP" == "" ]; do
		IP=$(lxc-attach -n $name -- ifconfig eth0 | grep "inet addr:" | cut -d: -f2 | awk '{print $1}')
		sleep 1
	done
	
	/apps/subutai/current/bin/d2s /home/ubuntu/docker/$name

	lxc-attach -n $name -- apt update
	lxc-attach -n $name -- apt install -y openssh-server wget curl
	lxc-attach -n $name -- mkdir /opt/docker2subutai
	
	cp /home/ubuntu/tmpfs/archive.tar.gz /mnt/lib/lxc/$name/opt/docker2subutai/
	lxc-attach -n $name -- tar zxvf /opt/docker2subutai/archive.tar.gz -C /opt/docker2subutai/
	rm /mnt/lib/lxc/$name/opt/docker2subutai/archive.tar.gz
	
	finish $name
	check $name &
done