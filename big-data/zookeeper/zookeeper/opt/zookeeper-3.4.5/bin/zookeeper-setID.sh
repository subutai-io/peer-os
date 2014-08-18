#!/bin/bash

ID_FILE=myid
ID_DIR=/var/zookeeper

usage="Usage: zookeeper-setID.sh ID"

# if no args specified, show usage
if [ $# -le 0 ]; then
        echo $usage
        exit 1
fi

# check if /var/zookeeper directory exists
if [ ! -d $ID_DIR ];then
        mkdir -p $ID_DIR
fi

# if myid file does not exists, create it 
if [ ! -f $ID_FILE ]; then
        touch $ID_DIR/$ID_FILE
fi
