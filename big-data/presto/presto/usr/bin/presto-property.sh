#!/bin/bash
set -e

usage()
{
        echo "Usage: $0 {add|remove}"
        echo "Usage: $0 add fileName name_field value_field --> adds property with provided name and value fields to file provided by fileName"
        echo "Usage: $0 remove fileName name_field --> removes provided property by name_field from provided file by fileName"
        echo "Example: $0 add config.properties task.max-memory 1GB"
        echo "Example: $0 remove config.properties task.max-memory"
        exit 1
}

. /etc/profile
add_prop="";
DEFAULT_PRESTO_HOME=/opt/presto-server-0.69

# Check if there are at least 3 parameters are passed to the methods!
if [[ $1 == "" || $2 == "" || $3 == "" ]];
then
        usage
else
        name_field=$3
fi
if [ "x$PRESTO_HOME" = "x" ];
then
       PRESTO_HOME=$DEFAULT_PRESTO_HOME 
fi
fileName="$PRESTO_HOME/etc/$2"

do_add()
{
	escape_characters $name_field $value_field
        # Add the property if does not exist!
        if grep -q $name_field"=" $fileName
        then
                :
        else
                add_prop=$add_prop"\n$escaped_name=$escaped_value"
        fi
        # Check if the file already has that property in it
        if [[ $add_prop == "" ]];
        then
                echo "Already has" $name_field "property! Changing the value of it to" $value_field
                configure
        else
                echo "Adding property with name:" $name_field "and value:" $value_field
                echo -e "$add_prop" >> $fileName
        fi
	#Remove empty lines
	sed -i "/^\s*$/d" $fileName

}

configure()
{
        # Change the value of property if exists!
	pattern1="$escaped_name\=[^\n]*"
        pattern2="$escaped_name\=$escaped_value"
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
        if [[ $4 == "" ]];
        then
                usage
        else
                value_field=$4
                do_add
        fi
;;

remove)
	escape_characters $name_field
	pattern1="$escaped_name\=.*"
        #Remove configuration with $name_field in $fileName
        perl -0777 -i.original -pe "s/${pattern1}/""/is" $fileName
;;
*)
        usage
esac
