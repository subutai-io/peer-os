#!/bin/bash
set -e
accumuloVer="1.6.1"

function usage()
{
	echo "Example usage:"
    echo "accumuloSlavesConf.sh slaves clear           -> Clear slaves file"
    echo "accumuloSlavesConf.sh slaves clear node1     -> Removes node1 from slaves file"		
    echo "accumuloSlavesConf.sh slaves add node1 node2 -> Adds node1 and node2 to slaves file"
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

slavesFile="/etc/accumulo/slaves"

case "$1" in
	slaves)
		case "$2" in
			clear)
				if [ "x$3" == "x" ];then
					> $slavesFile
					exit 0
				else
					sed -i "/$2/d" $slavesFile
					exit 0
				fi
			;;
			add)
				for arg; do
					if [ "$arg" != "add" ] && [ "$arg" != "slaves" ];then
				   		echo "$arg" >> $slavesFile
					fi
				done
			;;
			*)
				usage
		esac
	;;
	*)
		usage
esac
