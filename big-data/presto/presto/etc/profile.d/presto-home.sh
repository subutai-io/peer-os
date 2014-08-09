#!/bin/sh
prestoHome="/opt/presto-server-0.73"
export PRESTO_HOME=$prestoHome

path_content=$(echo $PATH)
pattern="$prestoHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi
