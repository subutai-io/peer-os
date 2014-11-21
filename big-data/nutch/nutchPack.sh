#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=nutch
downloadFileAndMakeChanges() {
	initializeVariables $1

	tempDirectory=$BASE/$fileName/opt
	confDirectory=$BASE/$fileName/etc/nutch

	nutchVersion=1.8

	# Create directories that are required for the debian package
    mkdir -p $tempDirectory
    mkdir -p $confDirectory

	# download nutch  
	wget http://archive.apache.org/dist/nutch/$nutchVersion/apache-nutch-$nutchVersion-bin.tar.gz -P $tempDirectory
	pushd $tempDirectory
	tar -xzpf apache-nutch-*.tar.gz

	# remove tar file
	rm apache-nutch-*.tar.gz

	# rename folder --remove hadoop1 from file name --
	mv apache-nutch-* nutch-$nutchVersion
	
	# move configuration files 
	mv nutch-$nutchVersion/conf/* $confDirectory
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
