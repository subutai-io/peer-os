#!/bin/sh
hbaseVersion=0.98.3
hbaseHome="/opt/hbase-$hbaseVersion"
hbaseConf="/etc/hbase/"
export HBASE_HOME=$hbaseHome

path_content=$(echo $PATH)
pattern="$hbaseHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi

