#!/bin/sh
sparkHome="/opt/spark-1.0.0"
export SPARK_HOME=$sparkHome

path_content=$(echo $PATH)

pattern="$sparkHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi

pattern="$sparkHome/sbin"
if test "${path_content#*$pattern}" = "$path_content"
then
        export PATH=$PATH:$pattern
fi

