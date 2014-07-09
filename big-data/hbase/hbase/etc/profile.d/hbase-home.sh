hbaseVersion=0.98.3
hbaseHome="/opt/hbase-$hbaseVersion"
hbaseConf="/etc/hbase/"
export HBASE_HOME=$hbaseHome

path_content=$(echo $PATH)
pattern="$hbaseHome/bin"
if [[ $path_content != *$pattern* ]];
then
	export PATH=$PATH:$pattern
fi

