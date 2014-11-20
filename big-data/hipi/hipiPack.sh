#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=hipi
downloadFileAndMakeChanges() {
	initializeVariables $1

	tempDirectory=$BASE/$fileName/opt
	confDirectory=$BASE/$fileName/etc/hipi

	# Create directories that are required for the debian package
	mkdir -p $tempDirectory
    	mkdir -p $confDirectory

	# download nutch 
	wget http://hipi.cs.virginia.edu/downloads/hipi.tar.gz -P $tempDirectory
	pushd $tempDirectory
	tar -xzpf hipi.tar.gz

	# remove tar file
	rm hipi.tar.gz
	
	popd
}

# 1) Check if the version is cahnged or not. If not changed, dont create a new debian.
checkPackageVersion $productName
# 2) Get the sources which are downloaded from version control system
#    to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download tar file and make necessary changes
downloadFileAndMakeChanges $productName
# 4) Create the Debian package
generateDebianPackage $productName
