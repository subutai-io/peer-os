#!/bin/sh
hadoopVersion=2.3.0
hadoopHome="/opt/hadoop-$hadoopVersion"
export HADOOP_HOME=$hadoopHome
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
