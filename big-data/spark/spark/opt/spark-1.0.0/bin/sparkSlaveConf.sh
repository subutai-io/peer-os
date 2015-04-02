#!/bin/bash
set -e

function usage()
{
  echo "Example usage:"
  echo "sparkSlaveConf.sh clear -> Clears the content of the slaves file"
  echo "sparkSlaveConf.sh clear x -> Removes x from slaves file"
  echo "sparkSlaveConf.sh x y z -> Adds x, y, z  to slaves file. Do NOT use comma between nodes"
  exit 0
}

if [[ "$1" == "" ]]; then
  usage
fi

if [[ "$1" == "help" ]]; then
  usage	
fi
. /etc/profile

file="/etc/spark/slaves"

#Clear the whole file or remove one machine from the slaves file
if [[ "$1" == "clear" ]]; then
  #Remove the whole file
  if [[ "$2" == "" ]]; then
    > $file
    exit 0
  fi
  #Else remove the specific machine from the slaves file
  sed -i "/$2/d" $file
  exit 0
  #or just add the machine to the slaves file
else
  for arg; do
    if [[ -z `cat $file | grep $arg` ]]; then
      echo "$arg" >> $file
    fi
  done
fi
