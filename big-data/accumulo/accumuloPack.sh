#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=accumulo
downloadFileAndMakeChanges() {
	initializeVariables $1

	tempDirectory=$BASE/$fileName/opt
	confDirectory=$BASE/$fileName/etc/accumulo

	accumuloVersion=0.9.1

	# Create directories that are required for the debian package
    mkdir -p $tempDirectory
    mkdir -p $confDirectory

	# download accumulo
	wget http://archive.apache.org/dist/accumulo/$accumuloVersion/accumulo-$accumuloVersion-bin.tar.gz -P $tempDirectory
	
	pushd $tempDirectory
	tar -xzpf accumulo-$accumuloVersion-bin*.tar.gz

	# remove tar file
	rm accumulo-$accumuloVersion-bin*.tar.gz


	# copy downloaded shark files
	cp -a accumulo-$accumuloVersion-bin*/* accumulo-$accumuloVersion/
	rm -r accumulo-$accumuloVersion-bin*
	
	# move configuration files 
	mv shark-$sharkVersion/conf/* $confDirectory
	popd
}
# 1) Get the sources which are downloaded from version control system
#    to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 2) Download tar file and make necessary changes
downloadFileAndMakeChanges $productName
# 3) Create the Debian package
generateDebianPackage $productName



#!/bin/sh
set -e
# Check if any deb file exists!!!
BASE=$(pwd)
sh -c 'cd $BASE'
echo $BASE
cd ../workspace
SOURCE=$(pwd)"/big-data/accumulo/accumulo"
TARGET="/var/lib/jenkins/Automation/Bigdata/accumulo"
echo $SOURCE
echo $TARGET
cd $BASE

if ls *.deb ; then
	rm  *.deb
fi

fileName=`ls | awk '{print $1}' | head -1`
echo "FILENAME: " $fileName

cp -a $SOURCE/DEBIAN/ $BASE/$fileName/
rm -rf $BASE/$fileName/opt/*
cp -a $SOURCE/opt/* $BASE/$fileName/opt/
cp -a $SOURCE/etc/* $BASE/$fileName/etc

wget http://archive.apache.org/dist/accumulo/1.4.2/accumulo-1.4.2-dist.tar.gz -P $fileName/opt/
tar xzvf $BASE/$fileName/opt/accumulo-1.4.2-dist.tar.gz
cp -a accumulo-1.4.2 $fileName/opt/
cp -a $fileName/opt/accumulo-1.4.2/conf/examples/1GB/standalone/* $fileName/opt/accumulo-1.4.2/conf/
chmod +x $fileName/opt/accumulo-1.4.2/conf/accumulo-env.sh
rm -rf accumulo-1.4.2
rm $BASE/$fileName/opt/accumulo-1.4.2-dist.tar.gz
rm -rf $fileName/opt/README.md

lineNumberVersion=$(sed -n '/Version:/=' $fileName/DEBIAN/control)
lineNumberPackage=$(sed -n '/Package:/=' $fileName/DEBIAN/control)
lineVersion=$(sed $lineNumberVersion!d $fileName/DEBIAN/control)
linePackage=$(sed $lineNumberPackage!d $fileName/DEBIAN/control)

version=$(echo $lineVersion | awk -F":" '{split($2,a," ");print a[1]}')
package=$(echo $linePackage | awk -F":" '{split($2,a," ");print a[1]}')
echo $version
echo $package

versionFirst=$(echo $version | awk -F"." '{print $1}')
versionSecond=$(echo $version | awk -F"." '{print $2}')
versionThird=$(echo $version | awk -F"." '{print $3}')

#updatedVersion=$(echo `expr $versionThird + 1`)
updatedRelease=$versionFirst.$versionSecond.$versionThird
replaceVersion="Version: $updatedRelease"
sed -i $fileName/DEBIAN/control -e $lineNumberVersion's!.*!'"$replaceVersion"'!'
packageName=$package-$updatedRelease"-amd64"

if [ "$fileName" != "$packageName" ] ;then
echo "different!!"
mv $fileName $packageName
fi

find ./$packageName -name "*~" -print0 | xargs -0 rm -rf
rm $packageName/DEBIAN/md5sums
md5sum `find ./$packageName -type f | awk '/.\//{ print substr($0, 3) }'` >> $packageName/DEBIAN/md5sums
dpkg-deb -z8 -Zgzip --build $packageName/
cp $packageName".deb" $TARGET/
