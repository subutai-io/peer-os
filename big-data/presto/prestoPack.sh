#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=presto
downloadFileAndMakeChanges() {
	initializeVariables $1

	tempDirectory=$BASE/$fileName/opt
	confDirectory=$BASE/$fileName/etc/presto

	prestoVersion=0.73
	discoveryServerVersion=1.18

	# Create directories that are required for the debian package
    mkdir -p $tempDirectory
    mkdir -p $confDirectory

	wget http://central.maven.org/maven2/com/facebook/presto/presto-server/$prestoVersion/presto-server-$prestoVersion.tar.gz -P $tempDirectory
	wget http://central.maven.org/maven2/com/facebook/presto/presto-cli/$prestoVersion/presto-cli-$prestoVersion-executable.jar -P $tempDirectory
	wget http://central.maven.org/maven2/io/airlift/discovery/discovery-server/$discoveryServerVersion/discovery-server-$discoveryServerVersion.tar.gz -P $tempDirectory

	pushd $tempDirectory

	# unpack tar file
	tar -xpzf $tempDirectory/presto-server*.tar.gz -C .
	tar -xpzf $tempDirectory/discovery-server*.tar.gz -C .

	# remove tar files after extracting
	rm $tempDirectory/presto-server*.tar.gz
	rm $tempDirectory/discovery-server*.tar.gz

	# presto-server and discovery-server tar files have almost same content except a couple of dependency jar files. 
	# Therefore we need to find diff of discovery and presto-server tar files, copy diff to presto-server lib directory
	# this is required since dependency confliction do not let presto-server start properly

	if [ -f "$tempDirectory /a" ]; then
        rm $tempDirectory/a;
	fi

	if [ -f "tempDirectory/b" ]; then
        rm $tempDirectory/b;
	fi

	`ls presto-server-$prestoVersion/lib | sed 's/-[0-9].*//' >> $tempDirectory/a`
	`ls discovery-server-$discoveryServerVersion/lib | sed 's/-[0-9].*//' >> $tempDirectory/b`

	fileNames=$(diff tempDirectory/b tempDirectory/a  | grep -i -- '<' |  cut -c 3-)
	echo "---------------------------------------"
	echo " file names : " $fileNames
	echo "---------------------------------------"

	for index in $fileNames
	do
        A=$index
        cp discovery-server-1.16/lib/$A* presto-server-0.69/lib
		echo "copying $A jar file to presto-server-$prestoVersion/lib folder"
	done

	rm $tempDirectory/a 
	rm $tempDirectory/b

	# remove discovery server folder after copying dependencies
	rm -r $tempDirectory/discovery-server-$discoveryServerVersion

	# move presto-cli jar 
	mv presto-cli-$prestoVersion-executable.jar $tempDirectory/presto-server-$prestoVersion/

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
