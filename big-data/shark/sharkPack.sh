#!/bin/sh
set -e

name="shark"
BASE=$(pwd)
cd ../workspace
SOURCE=$(pwd)"/big-data/$name/$name"
TARGET="/var/lib/jenkins/Automation/Bigdata/$name"
cd $BASE

# Check if any deb file exists!!!
if ls *.deb ; then
	rm  *.deb
fi

fileName=`ls | awk '{print $1}' | head -1`
echo "FILENAME: " $fileName

cp -a $SOURCE/DEBIAN/ $BASE/$fileName/
cp -a $SOURCE/etc/ $BASE/$fileName/ 
rm -rf $BASE/$fileName/opt/*
cp -a $SOURCE/opt/* $BASE/$fileName/opt/

wget http://s3.amazonaws.com/spark-related-packages/shark-0.9.1-bin-hadoop1.tgz -P $fileName/opt/
tar -xvzf $fileName/opt/*.tgz -C $fileName/opt
cp -a -r $fileName/opt/shark-0.9.1-bin-hadoop1/* $fileName/opt/shark-0.9.1/
cp $fileName/opt/shark-0.9.1/conf/shark-env.sh.template $fileName/opt/shark-0.9.1/conf/shark-env.sh
rm -rf $fileName/opt/shark-0.9.1-bin-hadoop1
rm $fileName/opt/*.tgz

# download derby client jar file
wget http://repo1.maven.org/maven2/org/apache/derby/derbyclient/10.4.2.0/derbyclient-10.4.2.0.jar -P $fileName/opt/shark-0.9.1/lib/

if [ -f "$fileName/opt/README" ]; then
	rm $fileName/opt/README
fi

lineNumberVersion=$(sed -n '/Version:/=' $fileName/DEBIAN/control)
lineNumberPackage=$(sed -n '/Package:/=' $fileName/DEBIAN/control)
lineVersion=$(sed $lineNumberVersion!d $fileName/DEBIAN/control)
linePackage=$(sed $lineNumberPackage!d $fileName/DEBIAN/control)

version=$(echo $lineVersion | awk -F":" '{split($2,a," ");print a[1]}')
package=$(echo $linePackage | awk -F":" '{split($2,a," ");print a[1]}')

versionFirst=$(echo $version | awk -F"." '{print $1}')
versionSecond=$(echo $version | awk -F"." '{print $2}')
versionThird=$(echo $version | awk -F"." '{print $3}')

#updatedVersion=$(echo `expr $versionThird + 1`)
updatedRelease=$versionFirst.$versionSecond.$versionThird
replaceVersion="Version: $updatedRelease"
sed -i $fileName/DEBIAN/control -e $lineNumberVersion's!.*!'"$replaceVersion"'!'
packageName=$package-$updatedRelease"-amd64"

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

