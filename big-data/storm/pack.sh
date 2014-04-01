#!/bin/sh
set -e
# Check if any deb file exists!!!
echo $BASE

sh -c 'cd $BASE'

if ls *.deb ; then
	rm  *.deb
fi

BASE="/var/lib/jenkins/jobs/master.bigdata.storm/Storm"
SOURCE="/var/lib/jenkins/jobs/master.bigdata.storm/workspace/big-data/storm/storm"
TARGET="/var/lib/jenkins/Automation/Bigdata/storm"

fileName=`ls | awk '{print $1}' | head -1`
echo $fileName

cp -a $SOURCE/DEBIAN/ $BASE/$fileName/
rm -rf $BASE/$fileName/opt/*
cp -a $SOURCE/opt/* $BASE/$fileName/opt/

#getting storm packages
wget http://download.zeromq.org/zeromq-2.1.7.tar.gz  -P $fileName/opt/
wget https://dl.dropboxusercontent.com/s/fl4kr7w0oc8ihdw/storm-0.8.2.zip -P $fileName/opt/
mv $fileName/opt/storm-0.8.2.zip .
unzip storm-0.8.2.zip
tar -cvpzf storm-0.8.2.tar.gz storm-0.8.2/
mv storm-0.8.2.tar.gz  $fileName/opt/
rm storm-0.8.2.zip
rm -rf storm-0.8.2
git clone https://github.com/nathanmarz/jzmq.git
tar -cvpzf jzmq.tar.gz jzmq/
mv jzmq.tar.gz $fileName/opt/
rm -rf jzmq/
rm $fileName/opt/README.md

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
