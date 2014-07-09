HBASE_CONF=/etc/hbase/

usage="Usage: region.sh \"List of machine names which will be HRegionServer, do NOT use comma between machine names\""

# if no args specified, show usage
if [ $# -le 0 ]; then
  echo $usage
  exit 1
fi

# clean content of regionservers file
> $HBASE_CONF/regionservers

# write to regionservers file
for arg; do
   echo "$arg" >> $HBASE_CONF/regionservers
done

