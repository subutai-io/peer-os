#!/bin/bash
set -e

export SPARK_MEM=1g

# (Required) Set the master program's memory
export SHARK_MASTER_MEM=1g


function usage()
{
	echo "Example usage:"
    echo "sharkMemConf.sh spark 3  -> Changes SPARK_MEM property to 3g insdie shark-env.sh file"
	echo "sharkMemconf.sh shark 4  -> Sets SHARK_MASTER_MEM property 4g inside shark-env.sh file" 
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
spark)
	sed -i "s/export SPARK_MEM=.*/export SPARK_MEM=$2g/g" $file ;;
shark)
	sed -i "s/export SHARK_MASTER_MEM=.*/export SHARK_MASTER_MEM=$2g/g" $file ;;
*)
	usage
	exit 1
esac
