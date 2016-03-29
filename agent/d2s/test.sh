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

	if [ "$(subutai list -i $child | grep -c RUNNING)" == "0" ]; then return; fi	
	IP=""
	while [ "$IP" == "" ]; do
		IP=$(lxc-attach -n $child -- ifconfig eth0 | grep "inet addr:" | cut -d: -f2 | awk '{print $1}')
		sleep 1
	done
	UBUNTU=$(grep -i "from " /home/ubuntu/docker/$name/Dockerfile | head -n1 | grep -ic ubuntu)
	DEBIAN=$(grep -i "from " /home/ubuntu/docker/$name/Dockerfile | head -n1 | grep -ic debian)
	while [ "$(lxc-attach -n $child -- ps aux | grep -v grep | grep -c apt)" != "0" ]; do
		sleep 1
	done
	sleep 30
	while true; do
        if [ "$DEBIAN" == "1" ]; then
			STATUS=$(lxc-attach -n $child -- systemctl is-active docker2subutai.service)
        elif [ "$UBUNTU" == "1" ]; then
			lxc-attach -n $child -- service docker2subutai status
			STATUS=$(lxc-attach -n $child -- service docker2subutai status | grep -c running)
		fi

		echo $STATUS
		if [ "$STATUS" == "active" ] || [ "$STATUS" == "1" ]; then
			break
		fi
		if [ "$STATUS" == "failed" ] || [ "$STATUS" == "unknown" ] || [ "$STATUS" == "0" ]; then
			subutai destroy $child
			echo "`date` $name failed" >> /home/ubuntu/docker.log
			return	
		fi
		sleep 1
	done
	
	sleep 30
        if [ "$DEBIAN" == "1" ]; then
		STATUS=$(lxc-attach -n $child -- systemctl is-active docker2subutai.service)
        elif [ "$UBUNTU" == "1" ]; then
		STATUS=$(lxc-attach -n $child -- service docker2subutai status  | grep -c running)
        fi
	if [ "$STATUS" == "active" ] || [ "$STATUS" == "1" ]; then
		subutai export $name
		echo "`date` $name exported" >> /home/ubuntu/docker.log
	else
		echo "`date` $name failed" >> /home/ubuntu/docker.log
	fi
	
	subutai destroy $child
}

for name in $(ls /home/ubuntu/docker); do
	UBUNTU=$(grep -i "from " /home/ubuntu/docker/$name/Dockerfile | head -n1 | grep -ic ubuntu)
	DEBIAN=$(grep -i "from " /home/ubuntu/docker/$name/Dockerfile | head -n1 | grep -ic debian)
	if [ "$DEBIAN" == "1" ]; then
		subutai clone debian $name	
		DISTR="debian"
	elif [ "$UBUNTU" == "1" ]; then
		subutai clone master $name
		DISTR="master"
	else
		continue
	fi

    if [ "$(subutai list -i $name | grep -c RUNNING)" == "0" ]; then continue; fi

	IP=""
	while [ "$IP" == "" ]; do
		IP=$(lxc-attach -n $name -- ifconfig eth0 | grep "inet addr:" | cut -d: -f2 | awk '{print $1}')
		sleep 1
	done
	while [ "$(lxc-attach -n $name -- ps aux | grep -v grep | grep -c apt)" != "0" ]; do
		sleep 1
	done
	
	/apps/subutai/current/bin/d2s /home/ubuntu/docker/$name

	# lxc-attach -n $name -- apt update
	# lxc-attach -n $name -- apt install -y openssh-server wget curl
	lxc-attach -n $name -- mkdir /opt/docker2subutai
	
	cp /home/ubuntu/tmpfs/archive.tar.gz /mnt/lib/lxc/$name/opt/docker2subutai/
	lxc-attach -n $name -- tar zxvf /opt/docker2subutai/archive.tar.gz -C /opt/docker2subutai/
	rm /mnt/lib/lxc/$name/opt/docker2subutai/archive.tar.gz
	
	finish $name
	check $name $DISTR 
done
