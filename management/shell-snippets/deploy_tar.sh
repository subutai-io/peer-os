#!/bin/bash

tar_package=$1

if [ -z "$tar_package" ]
then
	echo "Please pass tar_package folder as argument"
else
	build="$(echo $tar_package | awk -F ".tar" '{print $1}')"
	sudo rm -rf $build
	tar -xf $tar_package
	echo "Deploying lib..."
	for file in `ls $build/lib`; 
	do 
		#sudo rm -rf /apps/subutai-mng/current/lib/$file;
		sudo cp -rf $build/lib/$file /apps/subutai-mng/current/lib/$file;
		#echo $file; 
	done

	echo "Deploying system..."
	for file in `ls $build/system`;
	do
		#echo $file
		#sudo rm -rf /apps/subutai-mng/current/system/$file;
		sudo cp -rf $build/system/$file /apps/subutai-mng/current/system/$file;
	done

	
	echo "Restarting subutai-mng.."
	mngt_service="$(systemctl | awk '/subutai-mng-service/ {print $1}')"
	sudo systemctl stop $mngt_service
	cmd_result="$(echo $?)"
	if [[ "$cmd_result" -ne "0" ]];
	then
		mngt_service="$(systemctl | awk '/subutai-mng-service/ {print $2}')"
		sudo systemctl stop $mngt_service
	fi
	sudo systemctl start $mngt_service
	sudo systemctl status $mngt_service
	echo $mngt_service
	echo "Done"
	sudo ./karaf_ssh.sh
fi

#systemctl | grep subutai
