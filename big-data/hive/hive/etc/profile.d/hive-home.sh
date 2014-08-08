#!/bin/sh
hiveHome="/opt/hive"
hiveConf="/etc/hive"
export HIVE_HOME=$hiveHome
export HIVE_CONF_DIR=$hiveConf

path_content=$(echo $PATH)

pattern="$hiveHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi
