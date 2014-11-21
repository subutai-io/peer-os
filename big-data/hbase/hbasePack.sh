#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=hbase
downloadFileAndMakeChanges() {
	initializeVariables $1

	tempDirectory=$BASE/$fileName/opt
	confDirectory=$BASE/$fileName/etc/hbase

	hbaseVersion=0.98.3

	# Create directories that are required for the debian package
	mkdir -p $tempDirectory
	mkdir -p $confDirectory

	# download hbase which is compatible with hadoop1 version. 
	wget https://archive.apache.org/dist/hbase/hbase-0.98.3/hbase-0.98.3-hadoop1-bin.tar.gz -P $tempDirectory 
	pushd $tempDirectory
	tar -xpf hbase-*.tar.gz

	# remove tar file
	rm hbase-*.tar.gz

	# move configuration files 
	mv hbase-$hbaseVersion*/conf/* $BASE/$fileName/etc/hbase/
	rm -rf hbase-$hbaseVersion*/docs
	touch $BASE/$fileName/etc/hbase/backup-masters


	# rename folder --remove hadoop1 from file name --
	cp -a hbase-$hbaseVersion-hadoop1/* hbase-$hbaseVersion
	rm -rf hbase-$hbaseVersion-hadoop1

	# update libthirft jar file to make hbase compatible with sqoop
	# these operations were needed while using hbase version 0.94.16 ( still we may need them !!! )
	# rm $tempDirectory/hbase-$hbaseVersion/lib/libthrift-0.8.0.jar
	# cp $BASE/$fileName/libthrift-0.9.0.jar $tempDirectory/hbase-$hbaseVersion/lib

	chmod +x $BASE/$fileName/etc/hbase/hbase-env.sh
	popd
}

# 1) Check if the version is changed or not. If not changed, dont create a new debian.
checkPackageVersion $productName
# 2) Get the sources which are downloaded from version control system
#    to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download tar file and make necessary changes
downloadFileAndMakeChanges $productName
# 4) Create the Debian package
generateDebianPackage $productName
# 5) Create the Wrapper Repo Debian Package
generateRepoPackage $productName
