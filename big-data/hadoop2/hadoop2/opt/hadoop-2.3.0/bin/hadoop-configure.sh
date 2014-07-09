#!/bin/bash
set -e

function usage() {
        echo "Usage: $0 Namenode SecondaryNameNode dfs_replication -> Configure the cluster according to the given parameters"
        echo "Usage: $0 rollback -> Rollbacks the last modification made"
		echo "Example: $0 hadoop1-node1:9000 hadoop1-node2:50090 1"
        exit 1
}
. /etc/profile
#Paths to the configuration files
core_site="/etc/hadoop/core-site.xml"
hdfs_site="/etc/hadoop/hdfs-site.xml"

if [[ "$1" == "rollback" ]];
then
	if [ -f "${core_site}.original" ]; then
    		rm $core_site
		mv "${core_site}.original" ${core_site}
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
#namenode="hdfs://hadoop1-node1:9000"
namenode="hdfs:\/\/"$1

#Represent the secondaryNameNode machine of the cluster
#secondaryNameNode="hadoop1-node2:50090"
secondaryNameNode=$2

# Represent the replication factor of the cluster
#dfs_replication=3
dfs_replication=$3

#Patterns to matched and replaced with
pattern1="<name>fs.default.name<\/name>\n(.*?)<value>(.*?)<\/value>"
pattern2="<name>fs.default.name<\/name>\n\t<value>${namenode}<\/value>"
#Edit namenode in core-site.xml file
perl -0777 -i.original -pe "s/${pattern1}/${pattern2}/is" $core_site

#Patterns to matched and replaced with
pattern1="<name>dfs.namenode.secondary.http-address<\/name>\n(.*?)<value>(.*?)<\/value>"
pattern2="<name>dfs.namenode.secondary.http-address<\/name>\n\t<value>${secondaryNameNode}<\/value>"
#Edit jobtracker in mapred-site.xml
perl -0777 -i.original -pe "s/${pattern1}/${pattern2}/is" $hdfs_site

#Patterns to matched and replaced with
pattern1="<name>dfs.replication<\/name>\n(.*?)<value>(.*?)<\/value>"
pattern2="<name>dfs.replication<\/name>\n\t<value>${dfs_replication}<\/value>"
#Edit dfs replication in hdfs-site.xml
perl -0777 -i.original -pe "s/${pattern1}/${pattern2}/is" $hdfs_site
