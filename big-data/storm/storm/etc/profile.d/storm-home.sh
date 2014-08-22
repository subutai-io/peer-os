#!/bin/sh
stormHome="/opt/storm-0.8.2"
stormConf="/etc/storm"
export STORM_HOME=$stormHome
export STORM_CONF_DIR=$stormConf

path_content=$(echo $PATH)
pattern="$stormHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi
