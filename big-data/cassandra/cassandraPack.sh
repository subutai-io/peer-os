#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=cassandra
mode=bdproduct
downloadHadoopAndMakeChanges() {
	initializeVariables $1

	tempDirectory=$BASE/$fileName/opt
	confDirectory=$BASE/$fileName/etc/$productName

	cassVersion=2.0.9
	cassandraTarFile=apache-cassandra-$cassVersion-bin.tar.gz

	# Create directories that are required for the debian package
    mkdir -p $confDirectory
	mkdir -p $tempDirectory

	wget http://archive.apache.org/dist/cassandra/$cassVersion/$cassandraTarFile -P $tempDirectory

	if [ -f $BASE/$fileName/opt/README ]; then
	        rm $BASE/$fileName/opt/README
	fi
	# unpack tar ball and make changes 
	pushd $tempDirectory
	tar -xpf $cassandraTarFile -C .
	rm $cassandraTarFile

	# move conf directory
	cp -a apache-cassandra*/* cassandra-$cassVersion/
	rm -rf apache-cassandra*
	mv cassandra*/conf/* $confDirectory
	popd
}

# 1) Check if the version is changed or not. If not changed, dont create a new debian.
checkVersion $productName $mode
# 2) Get the sources which are downloaded from version control system
#    to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download tar file and make necessary changes
downloadHadoopAndMakeChanges $productName
# 4) Create the Debian package
generateDebianPackage $productName
# 5) Create the Wrapper Repo Debian Package
generateRepoPackage $productName
