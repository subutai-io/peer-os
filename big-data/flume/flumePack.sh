#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=flume
mode=bdproduct
downloadFileAndMakeChanges() {
	initializeVariables $1

	tempDirectory=$BASE/$fileName/opt
	confDirectory=$BASE/$fileName/etc/flume

	flumeVersion=1.5.0

	# Create directories that are required for the debian package
    mkdir -p $tempDirectory
    mkdir -p $confDirectory

	# download flume
	wget http://archive.apache.org/dist/flume/$flumeVersion/apache-flume-$flumeVersion-bin.tar.gz -P $tempDirectory
	
	pushd $tempDirectory
	tar -xzpf apache-flume-*.tar.gz

	# remove tar file
	rm apache-flume-*.tar.gz

	# rename folder
	mv apache-flume-$flumeVersion-bin* flume-$flumeVersion

	# move configuration files 
	mv flume-$flumeVersion/conf/* $confDirectory/

	# remove old dependencies
	# rm $tempDirectory/flume-$flumeVersion/lib/lucene*

	# download dependencies and place them under /flume/lib folder.
	luceneVersion=4.6.0
	wget http://central.maven.org/maven2/org/apache/lucene/lucene-analyzers-common/$luceneVersion/lucene-analyzers-common-$luceneVersion.jar -P $tempDirectory/flume-$flumeVersion/lib/
	wget http://central.maven.org/maven2/org/apache/lucene/lucene-analyzers-kuromoji/$luceneVersion/lucene-analyzers-kuromoji-$luceneVersion.jar -P $tempDirectory/flume-$flumeVersion/lib/
	wget http://central.maven.org/maven2/org/apache/lucene/lucene-analyzers-phonetic/$luceneVersion/lucene-analyzers-phonetic-$luceneVersion.jar -P $tempDirectory/flume-$flumeVersion/lib/
	wget http://central.maven.org/maven2/org/apache/lucene/lucene-codecs/$luceneVersion/lucene-codecs-$luceneVersion.jar -P $tempDirectory/flume-$flumeVersion/lib/
	wget http://central.maven.org/maven2/org/apache/lucene/lucene-core/$luceneVersion/lucene-core-$luceneVersion.jar -P $tempDirectory/flume-$flumeVersion/lib/
	wget http://central.maven.org/maven2/org/apache/lucene/lucene-grouping/$luceneVersion/lucene-grouping-$luceneVersion.jar -P $tempDirectory/flume-$flumeVersion/lib/
	wget http://central.maven.org/maven2/org/apache/lucene/lucene-highlighter/$luceneVersion/lucene-highlighter-$luceneVersion.jar -P $tempDirectory/flume-$flumeVersion/lib/
	wget http://central.maven.org/maven2/org/apache/lucene/lucene-memory/$luceneVersion/lucene-memory-$luceneVersion.jar -P $tempDirectory/flume-$flumeVersion/lib/
	wget http://central.maven.org/maven2/org/apache/lucene/lucene-misc/$luceneVersion/lucene-misc-$luceneVersion.jar -P $tempDirectory/flume-$flumeVersion/lib/
	wget http://central.maven.org/maven2/org/apache/lucene/lucene-queries/$luceneVersion/lucene-queries-$luceneVersion.jar -P $tempDirectory/flume-$flumeVersion/lib/
	wget http://central.maven.org/maven2/org/apache/lucene/lucene-queryparser/$luceneVersion/lucene-queryparser-$luceneVersion.jar -P $tempDirectory/flume-$flumeVersion/lib/
	wget http://central.maven.org/maven2/org/apache/lucene/lucene-spatial/$luceneVersion/lucene-spatial-$luceneVersion.jar -P $tempDirectory/flume-$flumeVersion/lib/
	wget http://central.maven.org/maven2/org/apache/lucene/lucene-suggest/$luceneVersion/lucene-suggest-$luceneVersion.jar -P $tempDirectory/flume-$flumeVersion/lib/
	wget http://central.maven.org/maven2/org/apache/hbase/hbase/0.94.16/hbase-0.94.16.jar -P $tempDirectory/flume-$flumeVersion/lib/
	wget http://central.maven.org/maven2/org/elasticsearch/elasticsearch/0.90.9/elasticsearch-0.90.9.jar -P $tempDirectory/flume-$flumeVersion/lib/ 

	# create logs directory
	mkdir -p $tempDirectory/flume-$flumeVersion/logs
	
	popd
}

# 1) Check if the version is changed or not. If not changed, dont create a new debian.
checkVersion $productName $mode
# 2) Get the sources which are downloaded from version control system
#    to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download tar file and make necessary changes
downloadFileAndMakeChanges $productName
# 4) Create the Debian package
generateDebianPackage $productName
# 5) Create the Wrapper Repo Debian Package
generateRepoPackage $productName
