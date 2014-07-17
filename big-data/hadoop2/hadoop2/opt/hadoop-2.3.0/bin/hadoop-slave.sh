#!/bin/bash
set -e

function usage()
{
	echo "Example usage:"
        echo "$0 clear -> Clears the content of the slaves file"
        echo "$0 clear localhost -> Removes localhost from slaves file"
        echo "$0 add localhost -> Adds localhost to slaves file"
        echo "$0 rollback -> Takes the last modification made on the slaves file back"
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
. /etc/profile

file="/etc/hadoop/slaves"

if [[ "$1" == "rollback" ]];
then
        if [ -f "${file}.original" ]; then
                rm $file
                mv "${file}.original" ${file}
        fi
        exit 0
fi

#Keep a backup file of the original one
if [ -f "${file}.original" ]; then
        rm "${file}.original"
fi
cp -a ${file} "${file}.original"

#Clear the whole file or remove one machine from the slaves file
if [[ "$1" == "clear" ]];then
	#Remove the whole file
	if [[ "$2" == "" ]];
	then
		cat /dev/null > $file
		exit 0
	fi
	#Else remove the specific machine from the slaves file
	sed -i "/$2/d" $file
	exit 0
#or just add the machine to the slaves file
elif [ "$1" == "add" ];then
	if [[ "$2" == "" ]];
	then
		usage
		exit 0
	else 
		echo $2 >> $file	
	fi
else 
	usage
	exit 0
fi
