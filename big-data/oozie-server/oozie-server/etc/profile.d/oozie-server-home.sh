#!/bin/sh
oozieHome="/opt/oozie-3.3.2"
export OOZIE_HOME=$oozieHome

path_content=$(echo $PATH)

pattern="$oozieHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi