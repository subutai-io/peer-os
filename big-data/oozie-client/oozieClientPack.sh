#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=oozie-client
downloadFileAndMakeChanges() {
        initializeVariables $1
        tempDirectory=$BASE/$fileName/opt/temp
	optDirectory=$BASE/$fileName/opt
        tarFile=oozie-3.3.2.tar.gz
	clientTarFile=oozie-client-3.3.2.tar.gz
	distroTarFile=oozie-3.3.2-distro.tar.gz
	extractedDirectory=oozie-3.3.2
	mkdir -p $tempDirectory
	mkdir -p $optDirectory

	#getting hadoop and oozie packages
	wget http://archive.apache.org/dist/oozie/3.3.2/$tarFile -P $tempDirectory
	if  ls $optDirectory/README* ; then
  		rm $optDirectory/README*
	fi
	# unpack tar ball and make changes 
        pushd $tempDirectory
	tar -xpzf $tarFile -C .
	rm $tarFile

	#Creating oozie distro
	$extractedDirectory/bin/mkdistro.sh -DskipTests
	if [ -d "$tempDirectory/../local.repository" ]; then
                rm -rf "$tempDirectory/../local.repository"
        fi
	
	cp $extractedDirectory/distro/target/$distroTarFile $tempDirectory
	rm -rf $extractedDirectory
	tar -xpzf $distroTarFile -C .
	mv $extractedDirectory/$clientTarFile $optDirectory
	rm -rf $extractedDirectory
	rm $distroTarFile
	popd
	rm -r $tempDirectory
	pushd $optDirectory
        tar -xpzf $clientTarFile -C $optDirectory
	rm $clientTarFile
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
