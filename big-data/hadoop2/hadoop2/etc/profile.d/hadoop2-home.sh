#!/bin/bash
set -e

hadoopVersion=2.3.0
hadoopHome="/opt/hadoop-$hadoopVersion"
export HADOOP_HOME=$hadoopHome
path_content=$(echo $PATH)

pattern="$hadoopHome/bin"
if [[ $path_content != *$pattern* ]];
then
	export PATH=$PATH:$pattern
fi

pattern="$hadoopHome/sbin"
if [[ $path_content != *$pattern* ]];
then
        export PATH=$PATH:$pattern
fi
