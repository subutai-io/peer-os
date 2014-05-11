ZOOKEEPER_CONF=/opt/zookeeper-3.4.5/conf

usage="Usage: zookeeper-conf.sh \"List of machine names which will be HQuorumPeer, do NOT use comma between machine names\""

# if no args specified, show usage
if [ $# -le 0 ]; then
  echo $usage
  exit 1
fi

# clean content of zoo.cfg file
sed -i '/2888/d' $ZOOKEEPER_CONF/zoo.cfg

# write to zoo.cfg file
i=1
for arg; do
   	echo "$i=$arg:2888:3888" >> $ZOOKEEPER_CONF/zoo.cfg
	i=$((i + 1))
done
