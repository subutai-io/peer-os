#!/bin/sh
set -e
# Check if any deb file exists!!!
BASE=$(pwd)
sh -c 'cd $BASE'
echo $BASE
cd ../workspace
SOURCE=$(pwd)"/big-data/hive/hive"
TARGET="/var/lib/jenkins/Automation/Bigdata/hive"
echo $SOURCE
echo $TARGET
cd $BASE

if ls *.deb ; then
        rm  *.deb
fi
pwd
fileName=`ls | awk '{print $1}' | head -1`
echo "FILENAME: "$fileName

cp -a $SOURCE/DEBIAN $BASE/$fileName/
cp -a $SOURCE/etc $BASE/$fileName/
rm -rf $BASE/$fileName/opt/*
cp -a $SOURCE/opt $BASE/$fileName/ 
wget http://archive.apache.org/dist/hive/hive-0.11.0/hive-0.11.0-bin.tar.gz -P $BASE/$fileName/opt/

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

echo $versionFirst
echo $versionSecond
echo $versionThird

#updatedVersion=$(echo `expr $versionThird + 1`)
updatedRelease=$versionFirst.$versionSecond.$versionThird
replaceVersion="Version: $updatedRelease"
sed -i $fileName/DEBIAN/control -e $lineNumberVersion's!.*!'"$replaceVersion"'!'
packageName=$package-$updatedRelease"-amd64"

echo $packageName


if [ "$fileName" != "$packageName" ] ;then
echo "different!!"
mv $fileName $packageName
fi

find ./$packageName -name "*~" -print0 | xargs -0 rm -rf
rm $packageName/DEBIAN/md5sums
md5sum `find ./$packageName -type f | awk '/.\//{ print substr($0, 3) }'` >> $packageName/DEBIAN/md5sums
dpkg-deb -z8 -Zgzip --build $packageName/

cp $packageName".deb" $TARGET/
