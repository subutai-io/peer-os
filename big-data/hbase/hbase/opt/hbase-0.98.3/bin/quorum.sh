#!/bin/bash
HBASE_CONF=/etc/hbase/

usage="Usage: master.sh \"List of machine names which will be HQourumPeer, do NOT use comma between machine names\""

# if no args specified, show usage
if [ $# -le 0 ]; then
  echo $usage
  exit 1
fi

# write to backup-masters file
list=""
for arg; do
   list=$list$arg,
done
list=$(echo $list | sed s'/.$//')

sed -i $HBASE_CONF/hbase-site.xml -e `expr $(sed -n '/<name>hbase.zookeeper.quorum<\/name>/=' $HBASE_CONF/hbase-site.xml) + 1`'s!.*!    <value>'$list'<\/value>!'
