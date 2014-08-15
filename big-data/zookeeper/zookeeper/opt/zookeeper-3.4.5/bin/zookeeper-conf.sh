#!/bin/bash
set -e

. /etc/profile

if [ "x$ZOOKEEPER_CONF_DIR" = "x" ];
then
  ZOOKEEPER_CONF_DIR="/etc/zookeeper"
fi

usage="Usage: zookeeper-conf.sh \"List of machine names which will be HQuorumPeer, do NOT use comma between machine names\""

# if no args specified, show usage
if [ $# -le 0 ]; then
  echo $usage
  exit 1
fi

# clean content of zoo.cfg file
sed -i '/2888/d' $ZOOKEEPER_CONF_DIR/zoo.cfg

# write to zoo.cfg file
i=1
for arg; do
   	echo "server.$i=$arg:2888:3888" >> $ZOOKEEPER_CONF_DIR/zoo.cfg
	i=$((i + 1))
done
