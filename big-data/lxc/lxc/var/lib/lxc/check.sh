#!/bin/bash
BASE="/etc/init.d/"
#Edit localhost line
file="/etc/hosts"
sed -i '/127.0.0.1/d' $file
echo "127.0.0.1 localhost" >> $file
#Agent checks
agentresult=$(dpkg --get-selections | grep ksks-agent)
source /etc/profile
if [ -z "$agentresult" ] ;then
  echo "not found installed agent package"
  #editing collectd hostname
  /etc/init.d/collectd stop
  (echo "Hostname `hostname | head -1`" && sed '1d' /etc/collectd/collectd.conf) >> /etc/collectd/newcollectd.conf
  mv /etc/collectd/newcollectd.conf /etc/collectd/collectd.conf
  /etc/init.d/collectd start
  #installing agent
  sudo apt-get update
  sudo apt-get --force-yes --assume-yes install ksks-agent
else
  echo "installed agent package is found script will be by-passed"
fi
