#!/bin/sh
oozieClientHome="/opt/oozie-client-3.3.2"
export OOZIE_CLIENT_HOME=$oozieClientHome

path_content=$(echo $PATH)

pattern="$oozieClientHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
        export PATH=$PATH:$pattern
fi
