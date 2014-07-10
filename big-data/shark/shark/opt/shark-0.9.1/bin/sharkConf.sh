#!/bin/bash
set -e

function usage()
{
	echo "Example usage:"
    echo "sharkConf.sh clear master/hive -> Clears the content of the shark-env.sh file with respect to second argument"
    echo "sharkConf.sh master x -> Exports x as spark master in shark-env.sh"
	echo "sharkConf.sh hive x -> Exports x as HIVE_HOME and HIVE_CONF_DIR in spark-env.sh"
	exit 0
}
if [[ "$1" == "" ]];
then
	usage
fi
if [[ "$1" == "help" ]];
then
	usage	
fi
. /etc/profile

file="/etc/shark/shark-env.sh"

case "$1" in
clear)
	if [ "$2" == "master" ];then					
		sed -i "/export MASTER=/d" $file
	elif [ "$2" == "hive" ];then
		sed -i "/export HIVE_HOME=/d" $file 
		sed -i "/export HIVE_CONF_DIR=/d" $file 		
	fi ;;
master)
	echo "export MASTER=\"spark://$2:7077\"" >> $file ;;
hive)
	echo "export HIVE_HOME=$2" >> $file 
	echo "export HIVE_CONF_DIR=$2/conf" >> $file ;;
hadoop)

	echo "export HADOOP_HOME=$2" >> $file ;;
spark)
	echo "export SPARK_HOME=$2" >> $file ;;
*)
	echo "Usage: $0 {start|stop|restart|status|kill}"
	exit 1
esac
