#!/bin/bash
set -e
accumuloVer="1.6.1"

function usage()
{
    echo "Example usage:"
    echo "accumulo-property.sh JAVA_HOME /usr/lib/jvm/jdk-1.7.0 -> Adds JAVA_HOME to accumulo-env.sh file"
    exit 0
}

if [[ "$1" == ""  || "$2" == "" ]];
then
	usage
fi
if [[ "$1" == "help" ]];
then
	usage	
fi

escape_characters()
{
	temp_name=$1
	escaped_name=$(echo $temp_name | sed -e 's/\//\\\//g')
	#echo "Escaped Name:" $escaped_name
	echo $escaped_name
}
. /etc/profile
ACCUMULO_HOME="/opt/accumulo-$accumuloVer"
file="/etc/accumulo/accumulo-env.sh"
path=$(escape_characters $2)
sed -i "s/export $1=.*/export $1=$path/g" $file
