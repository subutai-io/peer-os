#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=oozie-server
mode=bdproduct
downloadFileAndMakeChanges() {
        initializeVariables $1
        tempDirectory=$BASE/$fileName/opt/temp
        optDirectory=$BASE/$fileName/opt
        hadoopTarFile=hadoop-1.2.1-bin.tar.gz
	oozieTarFile=oozie-3.3.2.tar.gz
        distroTarFile=oozie-3.3.2-distro.tar.gz
	libextTarFile=libext.tar.gz
        extZipFile=ext-2.2.zip
	libextDirectory=libext
	extractedHadoopDirectory=hadoop-1.2.1
	extractedExtDirectory=ext-2.2
	extractedOozieDirectory=oozie-3.3.2
        mkdir -p $tempDirectory
        mkdir -p $optDirectory


	# Get necessary files
        wget http://archive.apache.org/dist/hadoop/core/hadoop-1.2.1/$hadoopTarFile -P $tempDirectory
        wget http://archive.apache.org/dist/oozie/3.3.2/$oozieTarFile -P $tempDirectory
#        wget extjs.com/deploy/$extZipFile -P $optDirectory
        wget http://archive.cloudera.com/gplextras/misc/$extZipFile -P $optDirectory
	if  ls $optDirectory/README* ; then
                rm $optDirectory/README*
        fi

        # Unpack tar ball and make changes 
        pushd $tempDirectory
        tar -xpzf $oozieTarFile -C .
        rm $oozieTarFile
	tar -xpzf $hadoopTarFile -C .

	#Creating libext directory
	mkdir -p $libextDirectory
	cp $extractedHadoopDirectory/*.jar $libextDirectory/
	cp $extractedHadoopDirectory/lib/*.jar $libextDirectory

	#Creating oozie distro
	$extractedOozieDirectory/bin/mkdistro.sh -DskipTests
	cp $extractedOozieDirectory/distro/target/$distroTarFile $optDirectory
	if [ -d "$tempDirectory/../local.repository" ]; then
                rm -rf "$tempDirectory/../local.repository"
        fi
	popd

	#Extract tar files under opt directory
	pushd $optDirectory
	tar -xpzf $distroTarFile -C .
	rm -rf $distroTarFile
        mv $tempDirectory/$libextDirectory $extractedOozieDirectory/
	unzip  $extZipFile
	mv $extractedExtDirectory $extractedOozieDirectory/
	rm -rf $extZipFile
	popd
	
	# Remove temp directory
        pushd $BASE
        rm -r $tempDirectory
        popd
}

# 1) Check if the version is changed or not. If not changed, dont create a new debian.
checkVersion $productName $mode
# 2) Get the sources which are downloaded from version control system to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download hadoop tar file and make necessary changes
downloadFileAndMakeChanges $productName
# 4) Create the Debian package
generateDebianPackage $productName
# 5) Create the Wrapper Repo Debian Package
generateRepoPackage $productName
