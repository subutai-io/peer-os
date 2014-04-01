#!/bin/bash
set -e

usage()
{
        echo "Usage: $0 {add|remove|configure}"
        echo "Usage: $0 add --> adds dummy oozie configurations"
        echo "Usage: $0 remove --> removes oozie configurations"
        echo "Usage: $0 configure OOZIE_SERVER_USER OOZIE_SERVER_HOSTNAME --> replaces dummy configuration with the provided input"
        echo "Example: $0 configure root 10.10.10.10" 
        exit 1
}
. /etc/profile
core_site=/opt/hadoop-1.2.1/conf/core-site.xml
add_prop="";
if [ "x$HADOOP_HOME" = "x" ];
then
        :
else
        core_site="$HADOOP_HOME/conf/core-site.xml"
fi

do_add()
{
	if grep -q "hadoop.proxyuser.*.hosts<\/name>" $core_site
	then
        	:
	else
        	add_prop=$add_prop"\t<property>\n\t <name>hadoop.proxyuser.[OOZIE_SERVER_USER].hosts<\/name>\n\t <value>[OOZIE_SERVER_HOSTNAME]<\/value>\n\t<\/property>\n"
	fi
	if grep -q "hadoop.proxyuser.*.groups<\/name>" $core_site
	then
        	:
	else
        	add_prop=$add_prop"\t<property>\n\t <name>hadoop.proxyuser.[OOZIE_SERVER_USER].groups<\/name>\n\t <value>[USER_GROUPS_THAT_ALLOW_IMPERSONATION]<\/value>\n\t<\/property>"

	fi

	if [[ $add_prop == "" ]];
	then
        	echo "No property added!"
	elif  grep -q "<!-- OOZIE -->" $core_site
	then
        	:
	else
      		add_prop="\t<!-- OOZIE -->\n"$add_prop
	fi

	sed -i "s/<\/configuration>/$add_prop\n<\/configuration>/g" $core_site
}
case "$1" in

add)
	do_add
;;
remove)

        pattern1="<property>([^\w]*?)<name>hadoop.proxyuser([^\s]*?).hosts<\/name>([^\w]*?)<value>(.*?)<\/value>([^\w]*?)<\/property>"
        pattern2="([\s]*?)<\/configuration>"
        #Edit namenode in core-site.xml file
        perl -0777 -i.original -pe "s/${pattern1}/""/is" $core_site
        pattern3="<property>([^\w]*?)<name>hadoop.proxyuser([^\s]*?).groups<\/name>([^\w]*?)<value>(.*?)<\/value>([^\w]*?)property>"
	#Edit namenode in core-site.xml file
        perl -0777 -i.original -pe "s/${pattern3}/""/is" $core_site
        pattern4="<!-- OOZIE -->"
        perl -0777 -i.original -pe "s/${pattern4}/""/is" $core_site
        perl -0777 -i.original -pe "s/${pattern2}/\n<\/configuration>/is" $core_site
;;
configure)
        if [[ $2 == "" || $3 == "" ]];
        then
                usage
        else
        	do_add
	        pattern1="<name>hadoop.proxyuser([^\s]*?).hosts<\/name>\n(.*?)<value>(.*?)<\/value>"
                pattern2="<name>hadoop.proxyuser.$2.hosts<\/name>\n\t<value>$3<\/value>"
                #Edit namenode in core-site.xml file
                perl -0777 -i.original -pe "s/${pattern1}/${pattern2}/is" $core_site
                pattern3="<name>hadoop.proxyuser([^\s]*?).groups<\/name>\n(.*?)<value>(.*?)<\/value>"
                pattern4="<name>hadoop.proxyuser.$2.groups<\/name>\n\t<value>*<\/value>"
                #Edit namenode in core-site.xml file
                perl -0777 -i.original -pe "s/${pattern3}/${pattern4}/is" $core_site
        fi
;;

*)
        usage
esac
