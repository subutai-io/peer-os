#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=solr
downloadFileAndMakeChanges() {
        initializeVariables $1
        optDirectory=$BASE/$fileName/opt
	tarFile=solr-4.4.0.tgz
	mkdir -p $optDirectory

	wget http://archive.apache.org/dist/lucene/solr/4.4.0/$tarFile -P $optDirectory
        if  ls $optDirectory/README* ; then
                rm $optDirectory/README*
        fi
	# unpack tar ball and make changes 
        pushd $optDirectory
        tar -xpf $tarFile -C .
        rm $tarFile
        popd
}

# 1) Check if the version is cahnged or not. If not changed, dont create a new debian.
checkPackageVersion $productName
# 2) Get the sources which are downloaded from version control system to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download hadoop tar file and make necessary changes
downloadFileAndMakeChanges $productName
# 4) Create the Debian package
generateDebianPackage $productName
