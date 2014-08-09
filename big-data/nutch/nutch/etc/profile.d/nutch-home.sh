#!/bin/sh
nutchHome="/opt/nutch-1.8"
export NUTCH_HOME=$nutchHome

path_content=$(echo $PATH)
pattern="$nutchHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi