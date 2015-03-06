#!/bin/bash
HBASE_CONF=/etc/hbase/

usage="Usage: region.sh \"List of machine names which will be Backup Masters, do NOT use comma between machine names\""

# if no args specified, show usage
if [ $# -le 0 ]; then
  echo $usage
  exit 1
fi

file=$HBASE_CONF/backup-masters

if [[ "$1" == "remove" ]]; then
  if [[ "$2" == "" ]]; then
    > $file
    exit 0
  fi
  sed -i "/$2/d" $file
  exit 0
elif [[ "$1" == "add" ]]; then
  shift
  for arg; do
   echo "$arg" >> $file
  done
else
  echo $usage
fi
