#!/bin/bash
set -e
usage() {
        echo "Usage: $0 ip_address_of_derby_database|local"
	echo "Example: $0 10.10.10.10"
        echo "Example: $0 local"
	exit 1
}

. /etc/profile
#Paths to the configuration files
hive_site="${HIVE_HOME}/conf/hive-site.xml"

if [ "$1" == "" ]; then
	usage
elif [ "$1" == "local" ]; then
	#Patterns to matched and replaced with
        pattern1="<name>javax.jdo.option.ConnectionURL<\/name>\n(.*?)<value>(.*?)<\/value>"
        pattern2="<name>javax.jdo.option.ConnectionURL<\/name>\n\t<value>jdbc:derby:;databaseName=\/etc\/hive\/metastore_db;create=true<\/value>"
        #Edit namenode in core-site.xml file
        perl -0777 -i.original -pe "s/${pattern1}/${pattern2}/is" $hive_site
else
	#Patterns to matched and replaced with
	pattern1="<name>javax.jdo.option.ConnectionURL<\/name>\n(.*?)<value>(.*?)<\/value>"
	pattern2="<name>javax.jdo.option.ConnectionURL<\/name>\n\t<value>jdbc:derby:\/\/$1:50000\/etc\/hive\/metastore_db;create=true<\/value>"
	#Edit namenode in core-site.xml file
	perl -0777 -i.original -pe "s/${pattern1}/${pattern2}/is" $hive_site
fi
