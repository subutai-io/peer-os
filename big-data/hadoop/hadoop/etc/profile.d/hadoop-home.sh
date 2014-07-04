#!/bin/bash
set -e

hadoopHome="/opt/hadoop-1.2.1"
export HADOOP_HOME=$hadoopHome
export HADOOP_CONF_DIR="/etc/hadoop"
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
