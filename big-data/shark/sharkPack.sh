#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=shark
downloadFileAndMakeChanges() {
	initializeVariables $1

	tempDirectory=$BASE/$fileName/opt
	confDirectory=$BASE/$fileName/etc/shark

	sharkVersion=0.9.1

	# Create directories that are required for the debian package
	mkdir -p $tempDirectory
	mkdir -p $confDirectory

	# download shark which is compatible with hadoop1 version. 
	wget http://s3.amazonaws.com/spark-related-packages/shark-$sharkVersion-bin-hadoop1.tgz -P $tempDirectory
	
	pushd $tempDirectory
	tar -xzpf shark-*.tgz

	# remove tar file
	rm shark-*.tgz

	# copy downloaded shark files
	cp -a shark-$sharkVersion-bin*/* shark-$sharkVersion/
	rm -r shark-$sharkVersion-bin*

	# download derby client jar file
	wget http://repo1.maven.org/maven2/org/apache/derby/derbyclient/10.4.2.0/derbyclient-10.4.2.0.jar -P $tempDirectory/shark-$sharkVersion/lib
	
	# move configuration files 
	mv shark-$sharkVersion/conf/* $confDirectory
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
