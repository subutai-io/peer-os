#!/bin/bash
set -e

. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=hive
downloadHiveAndMakeChanges() {
	initializeVariables $1
        optDirectory=$BASE/$fileName/opt
	binDirectory=$BASE/$fileName/opt/bin
	confDirectory=$BASE/$fileName/etc/$productName
	tempDirectory=$optDirectory/temp
	tarFile=apache-hive-0.13.1-bin.tar.gz
	# Create directories that are required for the debian package
        mkdir -p $confDirectory
	mkdir -p $tempDirectory	
	mkdir -p $binDirectory
	wget http://archive.apache.org/dist/hive/hive-0.13.1/$tarFile -P $tempDirectory
	if [ -f $BASE/$fileName/opt/README ]; then
	        rm $BASE/$fileName/opt/README
	fi
	# unpack tar ball and make changes 
        pushd $optDirectory

        # Rename existing directory if any
        fileName=`ls | grep $productName`
	if [[ $fileName != $productName ]];
        then
    		mv *$productName* $productName
	fi
	popd
	pushd $tempDirectory
	tar -xpf $tarFile -C .
	rm $tarFile
	mv *$productName*/conf/* $confDirectory
	mv *$productName*/bin/* $binDirectory
        rm -r *$productName*/conf
	rm -r *$productName*/bin
	# Move extracted tar file contents under relevant directory
	mv *$productName*/* $optDirectory/$productName
	popd
        rm -r $tempDirectory
	# Rename files that have space characters
	mv $optDirectory/$productName/examples/files/person\ age.txt $optDirectory/$productName/examples/files/person_age.txt	
}

# 2) Get the sources which are downloaded from version control system to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download hadoop tar file and make necessary changes
downloadHiveAndMakeChanges $productName
# 4) Create the Debian package
generateDebianPackage $productName
