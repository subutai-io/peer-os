#!/bin/sh
hadoopHome="/opt/hadoop-1.2.1"
export HADOOP_HOME="/opt/hadoop-1.2.1"
export HADOOP_CONF_DIR="/etc/hadoop"

path_content=$(echo $PATH)

pattern="$hadoopHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi

pattern="$hadoopHome/sbin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi