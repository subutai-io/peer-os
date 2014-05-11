#!/bin/sh
set -e
# Check if any deb file exists!!!
BASE=$(pwd)
sh -c 'cd $BASE'
echo $BASE
cd ../workspace
SOURCE=$(pwd)"/big-data/oozie-server/oozie-server"
TARGET="/var/lib/jenkins/Automation/Bigdata/oozie-server"
echo $SOURCE
echo $TARGET
cd $BASE

if ls *.deb ; then
	rm  *.deb
fi

fileName=`ls | awk '{print $1}' | head -1`
echo "FILENAME: " $fileName

cp -a $SOURCE/DEBIAN/ $BASE/$fileName/
cp -a $SOURCE/etc     $BASE/$fileName/
rm -rf $BASE/$fileName/opt/*
cp -a $SOURCE/opt/* $BASE/$fileName/opt/

#getting hadoop and oozie packages
wget https://www.apache.org/dist/hadoop/core/hadoop-1.2.1/hadoop-1.2.1-bin.tar.gz -P $fileName/opt/
wget https://archive.apache.org/dist/oozie/3.3.2/oozie-3.3.2.tar.gz -P $fileName/opt/
wget extjs.com/deploy/ext-2.2.zip -P $fileName/opt/

cd $fileName/opt/
unzip ext-2.2.zip
rm ext-2.2.zip
tar -cvpzf ext-2.2.tar.gz ext-2.2/
rm -rf ext-2.2/
cd $BASE

tar -xvpzf $BASE/$fileName/opt/hadoop-1.2.1-bin.tar.gz -C .
tar -xvpzf $BASE/$fileName/opt/oozie-3.3.2.tar.gz -C .
mv hadoop-1.2.1 $fileName/opt/
mv oozie-3.3.2 $fileName/opt/
rm $fileName/opt/hadoop-1.2.1-bin.tar.gz
rm $fileName/opt/oozie-3.3.2.tar.gz
rm $fileName/opt/README.md

#Creating libext.tar.gz
mkdir $fileName/opt/libext
cp $fileName/opt/hadoop-1.2.1/*.jar $fileName/opt/libext/
cp $fileName/opt/hadoop-1.2.1/lib/*.jar $fileName/opt/libext/

cd $fileName/opt/
tar -cvpzf libext.tar.gz libext/
rm -rf libext/
cd $BASE

#Creating oozie distro
rm -rf $fileName/opt/hadoop-1.2.1/
$fileName/opt/oozie-3.3.2/bin/mkdistro.sh -DskipTests
rm -rf $fileName/local.repository
cp $fileName/opt/oozie-3.3.2/distro/target/oozie-3.3.2-distro.tar.gz $fileName/opt/
rm -rf $fileName/opt/oozie-3.3.2/

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

updatedVersion=$(echo `expr $versionThird + 1`)
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
