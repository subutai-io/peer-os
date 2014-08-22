#!/bin/sh
flumeHome="/opt/flume-1.5.0"
export FLUME_HOME=$flumeHome

path_content=$(echo $PATH)
pattern="$flumeHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi

