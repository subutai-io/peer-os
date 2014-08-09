#!/bin/sh
zookeeperHome="/opt/zookeeper-3.4.5"
zookeeperConf="/etc/zookeeper"
export ZOOKEEPER_HOME=$zookeeperHome
export ZOOKEEPER_CONF_DIR=$zookeeperConf

path_content=$(echo $PATH)
pattern="$zookeeperHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi