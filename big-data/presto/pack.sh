#!/bin/sh
set -e
# Check if any deb file exists!!!
BASE=$(pwd)
sh -c 'cd $BASE'
echo $BASE
cd ../workspace
SOURCE=$(pwd)"/big-data/presto/presto"
TARGET="/var/lib/jenkins/Automation/Bigdata/presto"
echo $SOURCE
echo $TARGET
cd $BASE

if ls *.deb ; then
	rm  *.deb
fi

fileName=`ls | awk '{print $1}' | head -1`
echo "FILENAME: " $fileName

cp -a $SOURCE/etc  $BASE/$fileName/
cp -a $SOURCE/usr  $BASE/$fileName/
cp -a $SOURCE/DEBIAN/ $BASE/$fileName/
rm -rf $BASE/$fileName/opt/*
cp -a $SOURCE/opt/* $BASE/$fileName/opt/

#getting presto packages
wget http://central.maven.org/maven2/com/facebook/presto/presto-server/0.69/presto-server-0.69.tar.gz -P $fileName/opt/
wget http://central.maven.org/maven2/io/airlift/discovery/discovery-server/1.16/discovery-server-1.16.tar.gz -P $fileName/opt/
wget http://central.maven.org/maven2/com/facebook/presto/presto-cli/0.69/presto-cli-0.69-executable.jar -P $fileName/opt/

tar -xvpzf $BASE/$fileName/opt/presto-server-0.69.tar.gz -C .
tar -xvpzf $BASE/$fileName/opt/discovery-server-1.16.tar.gz -C .

# find diff of discovery and presto-server tar files, copy diff to presto-server lib directory
# this is required since dependency confliction do not let presto-server start properly

if [ -f "$fileName/opt/a" ]; then
        echo "Delete a"
        rm $fileName/opt/a;
fi

if [ -f "$fileName/opt/b" ]; then
        echo "Delete b"
        rm $fileName/opt/b;
fi

`ls presto-server-0.69/lib | sed 's/-[0-9].*//' >> $fileName/opt/a`
`ls discovery-server-1.16/lib | sed 's/-[0-9].*//' >> $fileName/opt/b`

fileNames=$(diff $fileName/opt/b $fileName/opt/a  | grep -i -- '<' |  cut -c 3-)
IFS=' ' read -a array <<< "$fileNames"
for index in $fileNames
do
        A=$index
        cp discovery-server-1.16/lib/$A* presto-server-0.69/lib
done

rm $fileName/opt/a 
rm $fileName/opt/b

mv presto-server-0.69/* $fileName/opt/presto-server-0.69/
rm -rf presto-server-0.69/
rm $fileName/opt/presto-server-0.69.tar.gz
rm $fileName/opt/discovery-server-1.16.tar.gz
mv $fileName/opt/presto-cli-0.69-executable.jar $fileName/opt/presto-server-0.69/
rm $fileName/opt/README.md

#Adding jar flies from discovery server
cp $fileName/opt/discovery-server-1.16/lib/*.jar $fileName/opt/presto-server-0.69/lib/
rm -rf $fileName/opt/discovery-server-1.16

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

versionOfPresto=0.69

sed -i s/presto-server-[0-9]*.[0-9]*'\/'/presto-server-$versionOfPresto'\/'/g $packageName/DEBIAN/conffile
sed -i s/prestoVer=\"[0-9]*.[0-9]*\"/prestoVer=\"$versionOfPresto\"/g $packageName/DEBIAN/postrm
sed -i s/prestoVer=\"[0-9]*.[0-9]*\"/prestoVer=\"$versionOfPresto\"/g $packageName/etc/init.d/presto
sed -i s/prestoVer=\"[0-9]*.[0-9]*\"/prestoVer=\"$versionOfPresto\"/g $packageName/etc/init.d/presto-client
sed -i s/prestoVer=\"[0-9]*.[0-9]*\"/prestoVer=\"$versionOfPresto\"/g $packageName/usr/bin/presto-config.sh

dpkg-deb -z8 -Zgzip --build $packageName/
cp $packageName".deb" $TARGET/


