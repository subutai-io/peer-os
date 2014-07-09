HBASE_CONF=/etc/hbase/

usage="Usage: backUpMasters.sh \"name of BackUpMaster machine\""

# if no args specified, show usage
if [ $# -le 0 ]; then
  echo $usage
  exit 1
fi

# clean content of backup-masters file
> $HBASE_CONF/backup-masters

# write to backup-masters file
for arg; do
   echo "$arg" >> $HBASE_CONF/backup-masters
done

