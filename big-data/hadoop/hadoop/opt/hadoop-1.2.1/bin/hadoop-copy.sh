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
core_site="${HADOOP_HOME}/conf/core-site.xml"
mapred_site="${HADOOP_HOME}/conf/mapred-site.xml"
hdfs_site="${HADOOP_HOME}/conf/hdfs-site.xml"
masters="${HADOOP_HOME}/conf/masters"
slaves="${HADOOP_HOME}/conf/slaves"
dfsInclude="${HADOOP_HOME}/conf/dfs.include"
mapredInclude="${HADOOP_HOME}/conf/mapred.include"
dfsExclude="${HADOOP_HOME}/conf/dfs.exclude"
mapredExclude="${HADOOP_HOME}/conf/mapred.exclude"
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
	if [ -f "${masters}.original" ]; then
                rm $masters
                cp -a "${masters}.original" ${masters}
        fi
	if [ -f "${slaves}.original" ]; then
                rm $slaves
                cp -a "${slaves}.original" ${slaves}
        fi
	if [ -f "${dfsExclude}.original" ]; then
                rm $dfsExclude
                cp -a "${dfsExclude}.original" ${dfsExclude}
        fi
	if [ -f "${mapredExclude}.original" ]; then
                rm $mapredExclude
                cp -a "${mapredExclude}.original" ${mapredExclude}
        fi
	if [ -f "${dfsInclude}.original" ]; then
                rm $dfsInclude
                cp -a "${dfsInclude}.original" ${dfsInclude}
        fi
        if [ -f "${mapredInclude}.original" ]; then
                rm $mapredInclude
                cp -a "${mapredInclude}.original" ${mapredInclude}
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
	
	if [ -f "${masters}.original" ]; then
        rm "${masters}.original"
        fi
        cp -a ${masters} "${masters}.original"
	
	if [ -f "${slaves}.original" ]; then
        rm "${slaves}.original"
        fi
        cp -a ${slaves} "${slaves}.original"
	
	if [ -f "${dfsExclude}.original" ]; then
        rm "${dfsExclude}.original"
        fi
        cp -a ${dfsExclude} "${dfsExclude}.original"

	if [ -f "${mapredExclude}.original" ]; then
        rm "${mapredExclude}.original"
        fi
        cp -a ${mapredExclude} "${mapredExclude}.original"
	
	if [ -f "${dfsInclude}.original" ]; then
        rm "${dfsInclude}.original"
        fi
        cp -a ${dfsInclude} "${dfsInclude}.original"

        if [ -f "${mapredInclude}.original" ]; then
        rm "${mapredInclude}.original"
        fi
        cp -a ${mapredInclude} "${mapredInclude}.original"

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

        if [ -f "${masters}.original" ]; then
        	rm "${masters}.original"
        fi

        if [ -f "${slaves}.original" ]; then
        	rm "${slaves}.original"
        fi
	
	if [ -f "${dfsExclude}.original" ]; then
                rm "${dfsExclude}.original"
        fi

	if [ -f "${mapredExclude}.original" ]; then
                rm "${mapredExclude}.original"
        fi
	
	if [ -f "${dfsInclude}.original" ]; then
                rm "${dfsInclude}.original"
        fi

        if [ -f "${mapredInclude}.original" ]; then
                rm "${mapredInclude}.original"
        fi

	exit
else
	usage
fi
