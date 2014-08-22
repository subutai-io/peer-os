#!/bin/sh
cassandraHome="/opt/cassandra-2.0.4"
export CASSANDRA_HOME=$cassandraHome
export CASSANDRA_CONF=/etc/cassandra

path_content=$(echo $PATH)
pattern="$cassandraHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi