#!/bin/sh
luceneHome="/opt/lucene-4.4.0"
export LUCENE_HOME=$luceneHome

path_content=$(echo $PATH)

pattern="$luceneHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi
