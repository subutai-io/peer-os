#!/bin/bash
set -e
usage() {
	echo "Usage: $0 {product_name}"
	echo "Example: $0 mongo"
	exit 1
}

initializeVariables() {
	product_name=$1
	product_name_uppercase="$(tr '[:lower:]' '[:upper:]' <<< ${product_name:0:1})${product_name:1}"
	BASE=/var/lib/jenkins/jobs/master.bigdata.$product_name/$product_name_uppercase
	SOURCE="$BASE/../workspace/big-data/$product_name/$product_name"
	TARGET="/var/lib/jenkins/Automation/Bigdata/$product_name"

	hadoopTarFile=hadoop-1.2.1-bin.tar.gz
	echo "BASE" $BASE
	echo "SOURCE" $SOURCE
	echo "TARGET" $TARGET

	pushd $BASE
        if ls *.deb ; then
                rm  *.deb
        fi
        fileName=`ls | grep ksks | awk '{print $1}' | head -1`
        echo "FILENAME: " $fileName
	popd
}
getSourcesToRelevantDirectories() {
	pushd $BASE
	rm -rf $BASE/$fileName/opt/*
	cp -a $SOURCE/DEBIAN/ $BASE/$fileName/
	cp -a $SOURCE/etc/ $BASE/$fileName/
	cp -a $SOURCE/opt/* $BASE/$fileName/opt/
	mkdir -p $BASE/$fileName/etc/hadoop
	popd
}
generateDebianPackage() {
	pushd $BASE
	# Clear the previous contents of the directory
	if [ -d $BASE/$fileName ]; then
		rm -r $BASE/$fileName/*
	fi
	# Copy the sources that are pulled from the version control system
	cp -a -r $SOURCE/* $BASE/$fileName

	lineNumberVersion=$(sed -n '/Version:/=' $fileName/DEBIAN/control)
	lineNumberPackage=$(sed -n '/Package:/=' $fileName/DEBIAN/control)
	lineVersion=$(sed $lineNumberVersion!d $fileName/DEBIAN/control)
	linePackage=$(sed $lineNumberPackage!d $fileName/DEBIAN/control)

	version=$(echo $lineVersion | awk -F":" '{split($2,a," ");print a[1]}')
	package=$(echo $linePackage | awk -F":" '{split($2,a," ");print a[1]}')

	versionFirst=$(echo $version | awk -F"." '{print $1}')
	versionSecond=$(echo $version | awk -F"." '{print $2}')
	versionThird=$(echo $version | awk -F"." '{print $3}')

	# Increment the least significant version by 1
	#updatedVersion=$(echo `expr $versionThird + 1`)
	updatedVersion=$versionThird

	updatedRelease=$versionFirst.$versionSecond.$updatedVersion
	replaceVersion="Version: $updatedRelease"

	packageName=$package-$updatedRelease

	# Update control file with the incremented version
	sed -i $fileName/DEBIAN/control -e $lineNumberVersion's!.*!'"$replaceVersion"'!'

	if [ "$fileName" != "$packageName" ] ;then
		mv $fileName $packageName
	fi

	find ./$packageName -name "*~" -print0 | xargs -0 rm -rf
	if [ -f "$packageName/DEBIAN/md5sums" ]; then
	       rm $packageName/DEBIAN/md5sums
	fi
	md5sum `find ./$packageName -type f | awk '/.\//{ print substr($0, 3) }'` >> $packageName/DEBIAN/md5sums
	dpkg-deb -z8 -Zgzip --build $packageName/
	cp $packageName".deb" $TARGET/
	popd
}

downloadHadoopAndMakeChanges() {
	pushd $BASE
	mkdir temp
	wget http://www.apache.org/dist/hadoop/core/hadoop-1.2.1/$hadoopTarFile -P $BASE/$fileName/opt/temp
	rm $BASE/$fileName/opt/README

	# unpack tar ball and make changes 
	pushd temp
	tar -xpf *.tar.gz -C .
	rm *.tar.gz
	mv hadoop*/conf/* $BASE/$fileName/etc/hadoop
	rm -r hadoop*/conf
	tar -cpzf hadoop-1.2.1-bin.tar.gz hadoop*
	mv hadoop-1.2.1-bin.tar.gz $BASE/$fileName/opt
	popd

	popd
}

# Check if the product name is provided as a parameter
if [[ $1 == "" ]];
then
        usage
fi
# TODO 1st, 2nd and 4th methods are generic for all big-data packages. So, it is better to move those methods to a common place so that every job can call it instead of implementing them!
# 1) Initialize the variables
initializeVariables $1
# 2) Get the sources which are downloaded from version control system to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories
# 3) Download hadoop tar file and make necessary changes
downloadHadoopAndMakeChanges
# 4) Create the Debian package
generateDebianPackage
