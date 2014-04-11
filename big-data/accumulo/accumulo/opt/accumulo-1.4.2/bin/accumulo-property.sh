#!/bin/bash
set -e
accumuloVer="1.4.2"

function usage()
{
    echo "Example usage:"
    echo "accumulo-property.sh clear JAVA_HOME /usr/lib/jvm/jdk-1.7.0 -> Clears JAVA_HOME from accumulo-env.sh file"
    echo "accumulo-property.sh add JAVA_HOME   -> Adds JAVA_HOME to accumulo-env.s file"
    exit 0
}

if [[ "$1" == "" ]];
then
	usage
fi
if [[ "$1" == "help" ]];
then
	usage	
fi

file="${ACCUMULO_HOME}/conf/accumulo-env.sh"

case "$1" in
	clear)
		sed -i "/$2/d" $file
		exit 0
	;;
	add)
		if grep -q "$2=" $file
		then
				echo "$2 already exists. Overriding existing property."
				sed -i "/$2/d" $file
				echo "export $2=$3" >> $file
				exit 0
		else
				echo "$2 is being added to accumulo-env.sh"
				echo "export $2=$3" >> $file
				exit 0
		fi
	;;
	*)
		usage
esac
