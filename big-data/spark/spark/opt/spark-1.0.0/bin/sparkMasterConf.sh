#!/bin/bash
set -e
sparkVer="1.0.0"

function usage()
{
  echo "Example usage:"
  echo "sparkMasterConf.sh clear -> Removes SPARK_MASTER_IP from spark-env.sh"	
  echo "sparkMasterConf.sh IP -> Adds SPARK_MASTER_IP to spark-env.sh"
  exit 0
}
if [[ "$1" == "" ]]; then
  usage
fi
if [[ "$1" == "help" ]]; then
  usage	
fi

file="/etc/spark/spark-env.sh"
if [[ "$1" == "clear" ]]; then
   #Else remove the specific machine from the slaves file
   sed -i "/SPARK_MASTER_IP=$2/d" $file
   exit 0
else
  if [[ -z `cat $file | grep "SPARK_MASTER_IP"` ]]; then
     echo "SPARK_MASTER_IP="$1 >> $file
  else 
   sed -i "/SPARK_MASTER_IP=$2/d" $file 
   echo "SPARK_MASTER_IP="$1 >> $file
  fi 
fi

. /etc/profile
