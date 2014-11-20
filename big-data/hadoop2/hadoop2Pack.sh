#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=hadoop2
downloadFileAndMakeChanges() {
	initializeVariables $1

	tempDirectory=$BASE/$fileName/opt
	confDirectory=$BASE/$fileName/etc/hadoop

	hadoopVersion=2.3.0

	# Create directories that are required for the debian package
    	mkdir -p $tempDirectory
    	mkdir -p $confDirectory

	wget http://www.apache.org/dist/hadoop/core/hadoop-$hadoopVersion/hadoop-$hadoopVersion-src.tar.gz -P $tempDirectory/

	tar -xpf $tempDirectory/hadoop-$hadoopVersion-src.tar.gz -C $tempDirectory
	rm $tempDirectory/hadoop-$hadoopVersion-src.tar.gz
	pushd $tempDirectory/hadoop-$hadoopVersion-src
	mvn package -Pdist -DskipTests -Dtar
	
	# copy newly created tart file under tempDirectory
	cp hadoop-dist/target/hadoop-$hadoopVersion.tar.gz $tempDirectory
	popd

	# move configuration files under /etc folder
	pushd $tempDirectory
	
	# remove source files
	rm -rf hadoop-$hadoopVersion-src

	# unpack tar file
	tar -xpf hadoop-$hadoopVersion.tar.gz 

	# remove tar file
	rm -rf hadoop-$hadoopVersion.tar.gz 

	# move conf files to /etc
	mv hadoop*/etc/hadoop/* $BASE/$fileName/etc/hadoop

	# replace /etc/hadoop directory with conf/ directory
	rm -r hadoop-$hadoopVersion/etc/
	mkdir -p hadoop-$hadoopVersion/conf
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
