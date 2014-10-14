#!/bin/sh
hipiHome="/opt/hipi"
export HIPI_HOME=$hipiHome

path_content=$(echo $PATH)
pattern="$hipiHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi
