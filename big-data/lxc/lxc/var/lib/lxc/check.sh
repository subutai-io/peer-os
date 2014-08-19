#!/bin/bash
BASE="/etc/init.d/"
#Remove 127.0.1.1 line
file="/etc/hosts"
sed -i '/127.0.1.1/d' $file
hostname=`hostname`
ip_address=`hostname -I`
host_entry="$ip_address$hostname"
# Add ip_address hostname information to /etc/hosts file if does not exist
if ! grep -q "$host_entry" "$file"; then
  echo $host_entry >> $file
fi
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
