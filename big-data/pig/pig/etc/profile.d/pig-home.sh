#!/bin/sh
pigHome="/opt/pig-0.13.0"
export PIG_HOME=$pigHome

path_content=$(echo $PATH)
pattern="$pigHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi

