#!/bin/bash
set -e

function usage() {
        echo "Usage: $0 ResourceManager -> Configure the cluster according to the given parameters"
        echo "Usage: $0 rollback -> Rollbacks the last modification made"
		echo "Example: $0 hadoop1"
        exit 1
}
. /etc/profile
#Paths to the configuration files
yarn_site="/etc/hadoop/yarn-site.xml"

if [[ "$1" == "rollback" ]];
then
	if [ -f "${yarn_site}.original" ]; then
    		rm $yarn_site
		mv "${yarn_site}.original" ${yarn_site}
	fi	
	exit 0
fi

if [[ "$1" == "" ]];
then
	usage
fi


#Represent the ResourceManager machine of the cluster
#resourcemanager="hadoop1"
resourceManager=$1
tracker_port=8025
scheduler_port=8030
rm_port=8040

#Patterns to matched and replaced with
pattern1="<name>yarn.resourcemanager.resource-tracker.address<\/name>\n(.*?)<value>(.*?)<\/value>"
pattern2="<name>yarn.resourcemanager.resource-tracker.address<\/name>\n\t<value>${resourceManager}:$tracker_port<\/value>"
#Edit namenode in core-site.xml file
perl -0777 -i.original -pe "s/${pattern1}/${pattern2}/is" $yarn_site

#Patterns to matched and replaced with
pattern1="<name>yarn.resourcemanager.scheduler.address<\/name>\n(.*?)<value>(.*?)<\/value>"
pattern2="<name>yarn.yarn.resourcemanager.scheduler.address<\/name>\n\t<value>${resourceManager}:$scheduler_port<\/value>"
#Edit namenode in core-site.xml file
perl -0777 -i.original -pe "s/${pattern1}/${pattern2}/is" $yarn_site

#Patterns to matched and replaced with
pattern1="<name>yarn.resourcemanager.address<\/name>\n(.*?)<value>(.*?)<\/value>"
pattern2="<name>yarn.resourcemanager.address<\/name>\n\t<value>${resourceManager}:$rm_port<\/value>"
#Edit namenode in core-site.xml file
perl -0777 -i.original -pe "s/${pattern1}/${pattern2}/is" $yarn_site
