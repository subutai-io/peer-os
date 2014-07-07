#!/bin/bash
set -e

. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=hive
downloadHiveAndMakeChanges() {
	initializeVariables $1
	confDirectory=$BASE/$fileName/etc/$productName
	tarFile=apache-hive-0.13.1-bin.tar.gz
	# Create directories that are required for the debian package
        mkdir -p $confDirectory

	wget http://archive.apache.org/dist/hive/hive-0.13.1/$tarFile -P $tempDirectory
	if [ -f $BASE/$fileName/opt/README ]; then
	        rm $BASE/$fileName/opt/README
	fi
	# unpack tar ball and make changes 
	pushd $tempDirectory
	# Rename existing directories if any
	mv *$productName* $productName
	tar -xpf $tarFile -C .
	rm $tarFile
	mv *$productName*/conf/* $confDirectory
	# Move extracted tar file contents under relevant directory
	mv *$productName*/* $productName
	popd
}

# 2) Get the sources which are downloaded from version control system to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download hadoop tar file and make necessary changes
downloadHadoopAndMakeChanges $productName
# 4) Create the Debian package
generateDebianPackage $productName
