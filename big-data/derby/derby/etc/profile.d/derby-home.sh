#!/bin/sh
derbyHome="/opt/db-derby-10.4.2.0-bin"
export DERBY_HOME=$derbyHome

path_content=$(echo $PATH)

pattern="$derbyHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi
