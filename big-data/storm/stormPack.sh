#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=storm
downloadFileAndMakeChanges() {
        initializeVariables $1
        tempDirectory=$BASE/$fileName/opt
        optDirectory=$BASE/$fileName/opt
        confDirectory=$BASE/$fileName/etc/$productName
	stormZipFile=storm-0.8.2.zip
	zeromqTarFile=zeromq-2.1.7.tar.gz
	jzmqTarFile=jzmq.tar.gz
	jzmqDirectory=jzmq
        mkdir -p $tempDirectory
        mkdir -p $optDirectory
	mkdir -p $confDirectory

        # Get necessary files
        wget http://download.zeromq.org/$zeromqTarFile  -P $tempDirectory
        wget http://dl.dropboxusercontent.com/s/fl4kr7w0oc8ihdw/$stormZipFile -P $tempDirectory
	pushd $tempDirectory
        git clone https://github.com/nathanmarz/jzmq.git
	popd
	
	if  ls $optDirectory/README* ; then
                rm $optDirectory/README*
        fi
        
	# unpack tar ball and make changes 
        pushd $tempDirectory
        unzip storm-0.8.2.zip
	tar -xpzf $zeromqTarFile -C .
	rm $stormZipFile
	rm $zeromqTarFile
        mv storm*/conf/* $confDirectory
	popd
        # Create Storm related folders into "/var" directory
	pushd $optDirectory/..
        mkdir -p "var/lib/storm"
        popd
}

# 1) Check if the version is changed or not. If not changed, dont create a new debian.
checkPackageVersion $productName
# 2) Get the sources which are downloaded from version control system to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download hadoop tar file and make necessary changes
downloadFileAndMakeChanges $productName
# 4) Create the Debian package
generateDebianPackage $productName
# 5) Create the Wrapper Repo Debian Package
generateRepoPackage $productName
