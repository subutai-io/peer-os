#!/bin/bash

ID_FILE=/var/zookeeper/myid

usage="Usage: zookeeper-setID.sh ID" 

# if no args specified, show usage
if [ $# -le 0 ]; then
	echo $usage
	exit 1
fi

# if myid file does not exists, create it 
if [ ! -f $ID_FILE ]; then
	mkdir /var/zookeeper
	touch  $ID_FILE
fi

# clean id file
> $ID_FILE

# write arg 1 to file
echo $1 >> $ID_FILE
