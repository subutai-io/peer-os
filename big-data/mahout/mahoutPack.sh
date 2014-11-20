#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=mahout
downloadFileAndMakeChanges() {
        initializeVariables $1
        tempDirectory=$BASE/$fileName/opt
        confDirectory=$BASE/$fileName/etc/$productName
        # Create directories that are required for the debian package
        mkdir -p $confDirectory
        mkdir -p $tempDirectory
	pushd $tempDirectory
	svn co http://svn.apache.org/repos/asf/mahout/trunk
	pushd trunk
	mvn install -DskipTests
	popd
	mv trunk $productName
	if [ -f $BASE/$fileName/opt/README ]; then
                rm $BASE/$fileName/opt/README
        fi
        popd
}

# 1) Check if the version is cahnged or not. If not changed, dont create a new debian.
checkPackageVersion $productName
# 2) Get the sources which are downloaded from version control system to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download file and make necessary changes
downloadFileAndMakeChanges $productName
# 4) Create the debian package
generateDebianPackageWithoutMD5 $productName
