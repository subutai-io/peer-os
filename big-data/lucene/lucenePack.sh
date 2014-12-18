#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=lucene
mode=bdproduct
downloadFileAndMakeChanges() {
        initializeVariables $1
        tempDirectory=$BASE/$fileName/opt
        confDirectory=$BASE/$fileName/etc/$productName
        tarFile=lucene-4.4.0.tgz
        # Create directories that are required for the debian package
        mkdir -p $confDirectory
	wget http://archive.apache.org/dist/lucene/java/4.4.0/$tarFile -P $tempDirectory
        if [ -f $BASE/$fileName/opt/README* ]; then
                rm $BASE/$fileName/opt/README*
        fi
        # unpack tar ball and make changes 
        pushd $tempDirectory
        tar -xpf $tarFile -C .
        rm $tarFile
        popd
}

# 1) Check if the version is changed or not. If not changed, dont create a new debian.
checkVersion $productName $mode
# 2) Get the sources which are downloaded from version control system to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download file and make necessary changes
downloadFileAndMakeChanges $productName
# 4) Create the debian package
generateDebianPackage $productName
# 5) Create the Wrapper Repo Debian Package
generateRepoPackage $productName
