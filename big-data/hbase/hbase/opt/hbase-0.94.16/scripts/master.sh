HBASE_CONF=/opt/hbase-0.94.16/conf

usage="Usage: master.sh \"name of NameNode machine\" \"name of HMaster machine\""

# if no args specified, show usage
if [ $# -le 0 ]; then
  echo $usage
  exit 1
fi

# the machine on which namenode of Hadoop is running
hdfs=$1

# the machine on which HMaster will run
hmaster=$2

# do configurations in hbase-site.xml file
sed -i $HBASE_CONF/hbase-site.xml -e `expr $(sed -n '/<name>hbase.rootdir<\/name>/=' $HBASE_CONF/hbase-site.xml) + 1`'s!.*!    <value>hdfs:\/\/'$hdfs':8020\/hbase<\/value>!'

sed -i $HBASE_CONF/hbase-site.xml -e `expr $(sed -n '/<name>hbase.master<\/name>/=' $HBASE_CONF/hbase-site.xml) + 1`'s!.*!    <value>'$hmaster':60000<\/value>!'
