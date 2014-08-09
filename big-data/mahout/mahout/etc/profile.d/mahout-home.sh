#!/bin/sh
mahoutHome="/opt/mahout"
export MAHOUT_HOME=$mahoutHome

path_content=$(echo $PATH)

pattern="$mahoutHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi
