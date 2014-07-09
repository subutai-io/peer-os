#!/bin/bash
set -e
function usage() {
        echo "Usage: $0 copy -> Makes a copy of the current configuration files that we are going to change"
	echo "Usage: $0 remove -> Removes the copy of the configuration files"
        echo "Usage: $0 rollback -> Rollbacks the whole modifications of configuration files before the time that have been copied."
	exit 1
}
. /etc/profile
#Paths to the configuration files
core_site="/etc/hadoop/core-site.xml"
mapred_site="/etc/hadoop/mapred-site.xml"
hdfs_site="/etc/hadoop/hdfs-site.xml"
slaves="/etc/hadoop/slaves"
yarn="/etc/hadoop/yarn-site.xml"

if [[ "$1" == "rollback" ]];
then
	if [ -f "${core_site}.original" ]; then
    		rm $core_site
		cp -a "${core_site}.original" ${core_site}
	fi
	if [ -f "${mapred_site}.original" ]; then
                rm $mapred_site
                cp -a "${mapred_site}.original" ${mapred_site}
        fi
	if [ -f "${hdfs_site}.original" ]; then
                rm $hdfs_site
                cp -a "${hdfs_site}.original" ${hdfs_site}
        fi
	if [ -f "${slaves}.original" ]; then
                rm $slaves
                cp -a "${slaves}.original" ${slaves}
        fi
	if [ -f "${yarn}.original" ]; then
                rm $yarn
                cp -a "${yarn}.original" ${yarn}
        fi
	exit 0
elif [[ "$1" == "copy" ]];
then
	if [ -f "${core_site}.original" ]; then
        rm "${core_site}.original"
	fi
	cp -a ${core_site} "${core_site}.original"
	
	if [ -f "${mapred_site}.original" ]; then
        rm "${mapred_site}.original"
        fi
        cp -a ${mapred_site} "${mapred_site}.original"
	
	if [ -f "${hdfs_site}.original" ]; then
        rm "${hdfs_site}.original"
        fi
        cp -a ${hdfs_site} "${hdfs_site}.original"
	
	if [ -f "${slaves}.original" ]; then
        rm "${slaves}.original"
        fi
        cp -a ${slaves} "${slaves}.original"

	if [ -f "${yarn}.original" ]; then
        rm "${yarn}.original"
        fi
        cp -a ${yarn} "${yarn}.original"
	exit 0
elif [[ "$1" == "remove" ]];
then
	if [ -f "${core_site}.original" ]; then
        	rm "${core_site}.original"
        fi

        if [ -f "${mapred_site}.original" ]; then
        	rm "${mapred_site}.original"
        fi

        if [ -f "${hdfs_site}.original" ]; then
        	rm "${hdfs_site}.original"
        fi

        if [ -f "${slaves}.original" ]; then
        	rm "${slaves}.original"
        fi

        if [ -f "${yarn}.original" ]; then
        	rm "${yarn}.original"
        fi
	exit
else
	usage
fi
