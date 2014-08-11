#!/bin/sh
sqoopHome="/opt/sqoop-1.4.4"
hadoopHome="/opt/hadoop-1.2.1"
export SQOOP_HOME=$sqoopHome
export HADOOP_COMMON_HOME=$hadoopHome
export HADOOP_MAPRED_HOME=$hadoopHome

path_content=$(echo $PATH)
pattern="$sqoopHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi