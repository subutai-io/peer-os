#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=spark
mode=bdproduct
downloadFileAndMakeChanges() {
	initializeVariables $1

	tempDirectory=$BASE/$fileName/opt
	confDirectory=$BASE/$fileName/etc/spark

	sparkVersion=1.0.0

	# Create directories that are required for the debian package
    mkdir -p $tempDirectory
    mkdir -p $confDirectory

	# download spark which is compatible with hadoop1 version. 
	wget http://archive.apache.org/dist/spark/spark-$sparkVersion/spark-$sparkVersion-bin-hadoop1.tgz -P $tempDirectory

	pushd $tempDirectory
	tar -xzpf spark-*.tgz

	# remove tar file
	rm spark-*.tgz

	# copy downloaded spark files
	cp -a spark-$sparkVersion-bin*/* spark-$sparkVersion/
	rm -r spark-$sparkVersion-bin*
	
	# move configuration files 
	mv spark-$sparkVersion/conf/* $confDirectory
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
