#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=sqoop
downloadFileAndMakeChanges() {
	initializeVariables $1

	tempDirectory=$BASE/$fileName/opt
	confDirectory=$BASE/$fileName/etc/sqoop

	sqoopVersion=1.4.4

	# Create directories that are required for the debian package
    mkdir -p $tempDirectory
    mkdir -p $confDirectory

    # download sqoop
	wget http://archive.apache.org/dist/sqoop/$sqoopVersion/sqoop-$sqoopVersion.bin__hadoop-1.0.0.tar.gz -P $tempDirectory
	pushd $tempDirectory
	# unpack tar file
	tar -xpf sqoop-*.tar.gz

	# remove tar file
	rm sqoop-*.tar.gz

	# remane downloaded file -- remove hadoop-1 from file name -- 
	mv sqoop-$sqoopVersion.bin__hadoop-1* sqoop-$sqoopVersion

	# move configuration files of sqoop under etc/
	mv sqoop-*/conf/* $confDirectory/

	# copy extra jar files under sqoop/lib folder
	cp $BASE/extraJar/*.jar $tempDirectory/sqoop-$sqoopVersion/lib
	popd
}
# 1) Get the sources which are downloaded from version control system
#    to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 2) Download tar file and make necessary changes
downloadFileAndMakeChanges $productName
# 3) Create the Debian package
generateDebianPackage $productName