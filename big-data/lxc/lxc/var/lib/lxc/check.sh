#!/bin/bash
BASE="/etc/init.d/"
#Agent checks
agentresult=$(dpkg --get-selections | grep ksks-agent)

if [ -z "$agentresult" ] ;then
  echo "not found installed agent package"
  sudo apt-get update
  sudo apt-get --force-yes --assume-yes install ksks-agent
else
  echo "installed agent package is found"
fi 
