#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=zookeeper
downloadFileAndMakeChanges() {
        initializeVariables $1
	tempDirectory=$BASE/$fileName/opt
        optDirectory=$BASE/$fileName/opt
        confDirectory=$BASE/$fileName/etc/$productName
        tarFile=zookeeper-3.4.5.tar.gz
	extractedZookeeperDirectory=zookeeper-3.4.5
        mkdir -p $optDirectory
	mkdir -p $confDirectory

	wget  http://archive.apache.org/dist/zookeeper/zookeeper-3.4.5/$tarFile -P $tempDirectory
        if  ls $optDirectory/README* ; then
                rm $optDirectory/README*
        fi
        # unpack tar ball and make changes 
        pushd $tempDirectory
        tar -xpf $tarFile -C .
        rm $tarFile
	mv zookeeper*/conf/* $confDirectory
        popd
	
	# Create zookeeper related folders into "/var" directory
	pushd $optDirectory/..
	if [ -d "var/zookeeper" ]; then
	        mkdir -p "var/zookeeper"
	fi

	# edit zkServer.sh script to clean warning "[: /var/zookeeper/: unexpected operator"
	sed -i "s/if \[ \-f \$ZOOPIDFILE \]; then/    if [ -f \"\$ZOOPIDFILE\" ]; then/g" $optDirectory/$extractedZookeeperDirectory/bin/zkServer.sh
	popd

}

# 1) Check if the version is cahnged or not. If not changed, dont create a new debian.
checkPackageVersion $productName
# 2) Get the sources which are downloaded from version control system to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download hadoop tar file and make necessary changes
downloadFileAndMakeChanges $productName
# 4) Create the Debian package
generateDebianPackageWithoutMD5 $productName
