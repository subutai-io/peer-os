#!/bin/bash
set -e

usage()
{
        echo "Usage: $0 {add|remove}"
        echo "Usage: $0 add fileName name_field value_field --> adds property with provided name and value fields to file provided by fileName"
        echo "Usage: $0 remove fileName name_field --> removes provided property by name_field from provided file by fileName"
        echo "Example: $0 add core-site.xml fs.default.name hdfs://localhost:8020"
        echo "Example: $0 remove core-site.xml fs.default.name"
        exit 1
}
. /etc/profile
add_prop="";

# Check if there are at least 2 parameters are passed to the methods!
if [[ $1 == "" || $2 == "" || $3 == "" ]];
then
        usage
else
        fileName="/etc/hadoop/$2"
        name_field=$3
fi

hadoopConf="/etc/hadoop"
if [[ "x$HADOOP_CONF_DIR" != "x" ]];
then
        hadoopConf=$HADOOP_CONF_DIR
fi

fileName="$hadoopConf/$2"

do_add()
{
	escape_characters $name_field $value_field
        # Add the property if does not exist!
        if grep -q $name_field"<\/name>" $fileName
        then
                :
        else
                add_prop=$add_prop"\t<property>\n\t <name>$escaped_name<\/name>\n\t <value>$escaped_value<\/value>\n\t<\/property>\n"
        fi
        # Check if the file already has that property in it
        if [[ $add_prop == "" ]];
        then
                echo "Already has" $name_field "property! Changing the value of it to" $value_field
                configure
        else
                echo "Adding property with name:" $name_field "and value: " $value_field
                sed -i "s/<\/configuration>/$add_prop\n<\/configuration>/g" $fileName
        fi
}

configure()
{
        # Change the value of property if exists!
        pattern1="<name>([\s]*?)$escaped_name([\s]*?)<\/name>\n(.*?)<value>(.*?)<\/value>"
        pattern2="<name>$escaped_name<\/name>\n\t<value>$escaped_value<\/value>"
        #Edit configuration with $name_field in $fileName
        perl -0777 -i.original -pe "s/${pattern1}/${pattern2}/is" $fileName
}
escape_characters()
{
	temp_name=$1
	temp_value=$2
        #echo "Escaping some special characters like '/' for name:" $temp_name "and value:" $temp_value	
	escaped_name=$(echo $temp_name | sed -e 's/\//\\\//g')
	escaped_value=$(echo $temp_value | sed -e 's/\//\\\//g')
	#echo "Escaped Name:" $escaped_name ", Escaped Value:" $escaped_value  
}
case "$1" in

add)
        if [[ $2 == "" || $3 == "" || $4 == "" ]];
        then
                usage
        else
                value_field=$4
                do_add
        fi
;;

remove)
	escape_characters $name_field
        pattern1="<property>([^\w]*?)<name>([\s]*?)$escaped_name([\s]*?)<\/name>([^\w]*?)<value>(.*?)<\/value>([^\w]*?)<\/property>"
        pattern2="([\s]*?)<\/configuration>"
	pattern3="([\s]*?)<property>"
        #Remove configuration with $name_field in $fileName
        perl -0777 -i.original -pe "s/${pattern1}/""/is" $fileName
	#Remove empty before </configuration>
        perl -0777 -i.original -pe "s/${pattern2}/\n<\/configuration>/is" $fileName
	#Remove empty lines before <property>
        perl -0777 -i.original -pe "s/${pattern3}/\n\t<property>/isg" $fileName
;;
*)
        usage
esac
