#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=hadoop
downloadHadoopAndMakeChanges() {
	echo "Downloading hadoop and making changes"
	initializeVariables $1
	echo "Local BASE:" $BASE
	hadoopTarFile=hadoop-1.2.1-bin.tar.gz
	mkdir -p $tempDirectory
	wget http://www.apache.org/dist/hadoop/core/hadoop-1.2.1/$hadoopTarFile -P $tempDirectory
	if [ -f $BASE/$fileName/opt/README ]; then
	        rm $BASE/$fileName/opt/README
	fi
	# unpack tar ball and make changes 
	pushd $tempDirectory
	tar -xpf $hadoopTarFile -C .
	rm $hadoopTarFile
	mv hadoop*/conf/* $BASE/$fileName/etc/hadoop
	rm -r hadoop*/conf
	tar -cpzf $hadoopTarFile hadoop*
	mv $hadoopTarFile $BASE/$fileName/opt
        rm -r $tempDirectory
	popd
}

# 2) Get the sources which are downloaded from version control system to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download hadoop tar file and make necessary changes
downloadHadoopAndMakeChanges $productName
# 4) Create the Debian package
generateDebianPackage $productName
