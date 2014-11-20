#!/bin/sh
accumuloHome="/opt/accumulo-1.6.1"
export ACCUMULO_HOME=$accumuloHome

path_content=$(echo $PATH)
pattern="$accumuloHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi
