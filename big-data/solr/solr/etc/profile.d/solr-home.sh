#!/bin/sh
solrHome="/opt/solr-4.4.0"
export SOLR_HOME=$solrHome

path_content=$(echo $PATH)
pattern="$solrHome/bin"
if test "${path_content#*$pattern}" = "$path_content"
then
	export PATH=$PATH:$pattern
fi
