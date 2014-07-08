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
yamlFile="/etc/cassandra/cassandra.yaml"

if [[ "$1" == "rollback" ]];
then
	if [ -f "${yamlFile}.original" ]; then
    		rm $yamlFile
		cp -a "${yamlFile}.original" ${yamlFile}
	fi
	
	exit 0
elif [[ "$1" == "copy" ]];
then
	if [ -f "${yamlFile}.original" ]; then
        rm "${yamlFile}.original"
	fi
	cp -a ${yamlFile} "${yamlFile}.original"
	exit 0
elif [[ "$1" == "remove" ]];
then
	if [ -f "${yamlFile}.original" ]; then
        	rm "${yamlFile}.original"
        fi
	exit
else
	usage
fi
