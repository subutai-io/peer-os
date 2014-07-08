#!/bin/bash
# Cassandar configuration script
#
cassVersion=cassandra-2.0.9
file=/etc/cassandra/cassandra.yaml

usage()
{
	echo "Supported configurations : cluster_name, data_dir, commitlog_dir, saved_cache_dir, rpc_address, listen_address, seeds"
	echo "cassandra-conf.sh cluster_name My_Cluster"	
	echo "Please use commas between nodes if you will change seeds parameter"	
	echo "ex: cassandra_conf.sh seeds \"node1,node2\""
	echo "ex: cassandra-conf.sh commitlog_dir /var/lib/cassandra/commitlog"
	echo "ex: cassandra-conf.sh data_dir /var/lib/cassandra/data"		
    	exit 1
}

escape_characters()
{
	temp_name=$1
	escaped_name=$(echo $temp_name | sed -e 's/\//\\\//g')
	#echo "Escaped Name:" $escaped_name
	echo $escaped_name
}

# Check if there are at least 2 parameters are passed to the methods!
if [[ $1 == "" || $2 == "" ]]; then
        usage
	exit 0
fi

case "$1" in
data_dir)
	path=$(escape_characters $2)
	a=$(sed -n '/data_file_directories:.*/=' $file)
	b=$((a + 1))
	sed -i ''$b'd' $file
	sed -i ''$b'i \ \ \ \ \- '$path'' $file
;;

commitlog_dir)
	path=$(escape_characters $2)
	sed -i "s/commitlog_directory:.*/commitlog_directory: $path/g" $file
;;

saved_cache_dir)
	path=$(escape_characters $2)
	sed -i "s/saved_caches_directory:.*/saved_caches_directory: $path/g" $file
;;

rpc_address)
	sed -i "s/rpc_address:.*/rpc_address: $2/g" $file
;;

listen_address)
	sed -i "s/listen_address:.*/listen_address: $2/g" $file
;;

seeds)
	sed -i "s/- seeds:.*/- seeds: \"$2\"/g" $file
;;

cluster_name)
	name=$(escape_characters $2)
	sed -i "s/cluster_name:.*/cluster_name: '$name'/g" $file
;;
*)
        usage
        exit 1
esac

