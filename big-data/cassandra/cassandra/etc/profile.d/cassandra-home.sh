cassandraHome="/opt/cassandra-2.0.9"
export CASSANDRA_HOME=$cassandraHome
export CASSANDRA_CONF=/etc/cassandra

path_content=$(echo $PATH)
pattern="$cassandraHome/bin"
if [[ $path_content != *$pattern* ]];
then
	export PATH=$PATH:$pattern
fi