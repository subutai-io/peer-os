#!/bin/bash

mngt_service="$(systemctl | awk '/subutai-agent/ {print $1}')"
sudo systemctl stop $mngt_service
cmd_result="$(echo $?)"
if [[ "$cmd_result" -ne "0" ]];
then
	mngt_service="$(systemctl | awk '/subutai-agent/ {print $2}')"
	sudo systemctl stop $mngt_service
fi
sleep 3
sudo systemctl start $mngt_service

sleep 3
sudo systemctl status $mngt_service
