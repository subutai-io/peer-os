#!/bin/sh
sharkHome="/opt/shark-0.9.1"
export SHARK_HOME=$sharkHome

path_content=$(echo $PATH)
pattern="$sharkHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi

