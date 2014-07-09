#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=storm
downloadFileAndMakeChanges() {
        initializeVariables $1
        tempDirectory=$BASE/$fileName/opt
        optDirectory=$BASE/$fileName/opt
        stormZipFile=storm-0.8.2.zip
	zeromqTarFile=zeromq-2.1.7.tar.gz
	jzmqDirectory=jzmq
        extractedStormDirectory=storm-0.8.2
	extractedZeromqDirectory=zeromq-2.1.7
        mkdir -p $tempDirectory
        mkdir -p $optDirectory

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
	rm $stormZipFile
	tar -xzf $zeromqTarFile -C .
	rm $zeromqTarFile
	buildStormDependencies

        # Create Storm related folders into "/var" directory
	pushd $optDirectory/..
        mkdir -p "var/stormtmp"
        popd

        popd
}

buildStormDependencies() {
	export JAVA_HOME=$JAVA_HOME

	#Install ZeroMQ (Storm Native Dependency)
	pushd $optDirectory/$extractedZeromqDirectory
	sudo ./configure
	sudo make
	sudo make install
	popd

	# Install JZMQ(Storm Native Dependency)
	pushd $optDirectory/$jzmqDirectory
	sudo ./autogen.sh
	sudo ./configure
	pushd src
	touch classdist_noinst.stamp
	CLASSPATH=.:./.:$CLASSPATH javac -d . org/zeromq/ZMQ.java org/zeromq/ZMQException.java org/zeromq/ZMQQueue.java org/zeromq/ZMQForwarder.java org/zeromq/ZMQStreamer.java
	popd
	sudo make
	sudo make install
	popd
}

# 2) Get the sources which are downloaded from version control system to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download hadoop tar file and make necessary changes
downloadFileAndMakeChanges $productName
# 4) Create the Debian package
generateDebianPackage $productName
