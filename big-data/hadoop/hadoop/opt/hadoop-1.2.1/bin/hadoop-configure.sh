#!/bin/bash
set -e

function usage() {
        echo "Usage: $0 Namenode JobTracker dfs_replication -> Configure the cluster according to the given parameters"
        echo "Usage: $0 rollback -> Rollbacks the last modification made"
	echo "Example: $0 hadoop1-node1:8020 hadoop1-node2:9000 1"
        exit 1
}
. /etc/profile
#Paths to the configuration files
core_site="${HADOOP_HOME}/conf/core-site.xml"
mapred_site="${HADOOP_HOME}/conf/mapred-site.xml"
hdfs_site="${HADOOP_HOME}/conf/hdfs-site.xml"

if [[ "$1" == "rollback" ]];
then
	if [ -f "${core_site}.original" ]; then
    		rm $core_site
		mv "${core_site}.original" ${core_site}
	fi
	if [ -f "${mapred_site}.original" ]; then
                rm $mapred_site
                mv "${mapred_site}.original" ${mapred_site}
        fi
	if [ -f "${hdfs_site}.original" ]; then
                rm $hdfs_site
                mv "${hdfs_site}.original" ${hdfs_site}
        fi
	
	exit 0
fi
if [[ "$1" == "" || "$2" == "" || "$3" == "" ]];
then
	usage
fi

#Represent the namenode machine of the cluster
#namenode="hdfs://hadoop1-node1:8020"
namenode="hdfs:\/\/"$1
#Represent the jobtracker machine of the cluster
#jobtracker="hadoop1-node2:9000"
jobtracker=$2
# Represent the replication factor of the cluster
#dfs_replication=3
dfs_replication=$3

#Patterns to matched and replaced with
pattern1="<name>fs.default.name<\/name>\n(.*?)<value>(.*?)<\/value>"
pattern2="<name>fs.default.name<\/name>\n\t<value>${namenode}<\/value>"
#Edit namenode in core-site.xml file
perl -0777 -i.original -pe "s/${pattern1}/${pattern2}/is" $core_site

#Patterns to matched and replaced with
pattern1="<name>mapred.job.tracker<\/name>\n(.*?)<value>(.*?)<\/value>"
pattern2="<name>mapred.job.tracker<\/name>\n\t<value>${jobtracker}<\/value>"
#Edit jobtracker in mapred-site.xml
perl -0777 -i.original -pe "s/${pattern1}/${pattern2}/is" $mapred_site

#Patterns to matched and replaced with
pattern1="<name>dfs.replication<\/name>\n(.*?)<value>(.*?)<\/value>"
pattern2="<name>dfs.replication<\/name>\n\t<value>${dfs_replication}<\/value>"
#Edit dfs replication in hdfs-site.xml
perl -0777 -i.original -pe "s/${pattern1}/${pattern2}/is" $hdfs_site
