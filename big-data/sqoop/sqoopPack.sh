#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=sqoop
mode=bdproduct
downloadFileAndMakeChanges() {
	initializeVariables $1

	tempDirectory=$BASE/$fileName/opt
	confDirectory=$BASE/$fileName/etc/sqoop

	sqoopVersion=1.4.4

	# Create directories that are required for the debian package
    mkdir -p $tempDirectory
    mkdir -p $confDirectory

    # download sqoop
	wget http://archive.apache.org/dist/sqoop/$sqoopVersion/sqoop-$sqoopVersion.bin__hadoop-1.0.0.tar.gz -P $tempDirectory

	pushd $tempDirectory
	# unpack tar file
	tar -xzpf sqoop-*.tar.gz

	# remove tar file
	rm sqoop-*.tar.gz

	# remane downloaded file -- remove hadoop-1 from file name -- 
	mv sqoop-$sqoopVersion.bin__hadoop-1* sqoop-$sqoopVersion

	# download ojdbc and mysql jar files and place them under sqoop/lib folder
	# ojdbc 
	wget http://www.java2s.com/Code/JarDownload/ojdbc6/ojdbc6.jar.zip -P $tempDirectory
	unzip ojdbc6.jar.zip
	cp ojdbc6.jar $tempDirectory/sqoop-$sqoopVersion/lib
	rm -r ojdbc6.jar
	rm -r ojdbc6.jar.zip
	# mysql
	wget http://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.0.8.tar.gz -P $tempDirectory
	tar -xzpf mysql-connector-java-5.0.8.tar.gz
	pushd mysql-connector-java-5.0.8
	cp mysql-connector-java-5.0.8-bin.jar $tempDirectory/sqoop-$sqoopVersion/lib
	popd
	rm -r mysql-connector-java-5.0.8.tar.gz 
	rm -r mysql-connector-java-5.0.8

	# move configuration files of sqoop under etc/
	mv sqoop-*/conf/* $confDirectory/
	popd
}

# 1) Check if the version is changed or not. If not changed, dont create a new debian.
checkVersion $productName $mode
# 2) Get the sources which are downloaded from version control system
#    to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download tar file and make necessary changes
downloadFileAndMakeChanges $productName
# 4) Create the Debian package
generateDebianPackage $productName
# 5) Create the Wrapper Repo Debian Package
generateRepoPackage $productName
