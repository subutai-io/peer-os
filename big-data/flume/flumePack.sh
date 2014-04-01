#!/bin/sh

set -e

if ls | grep .deb ; then
        rm  *.deb
fi

rm -rf ksks-flume*/*
cp -r ~/jobs/master.bigdata.flume/workspace/big-data/flume/flume/* ksks-flume*/
cd ksks-flume*
rm -rf opt
mkdir opt
wget -P opt http://archive.apache.org/dist/flume/1.4.0/apache-flume-1.4.0-bin.tar.gz
tar -xvpf opt/*.tar.gz -C opt/
rm -rf opt/*.tar.gz
mv opt/* opt/flume-1.4.0
cd -
rm ksks-flume*/opt/flume-1.4.0/lib/lucene*
cp extraJar/* ksks-flume*/opt/flume-1.4.0/lib/
cp -r ~/jobs/master.bigdata.flume/workspace/big-data/flume/flume/opt/flume-1.4.0/conf/* ksks-flume*/opt/flume-1.4.0/conf/
cd ksks-flume*/opt/flume-1.4.0
mkdir logs
cd -

fileName=`ls | grep ksks | awk '{print $1}' | head -1`

lineNumberVersion=$(sed -n '/Version:/=' $fileName/DEBIAN/control)
lineNumberPackage=$(sed -n '/Package:/=' $fileName/DEBIAN/control)


lineVersion=$(sed $lineNumberVersion!d $fileName/DEBIAN/control)
linePackage=$(sed $lineNumberPackage!d $fileName/DEBIAN/control)

version=$(echo $lineVersion | awk -F":" '{split($2,a," ");print a[1]}')

versionFirst=$(echo $version | awk -F"." '{print $1}')
versionSecond=$(echo $version | awk -F"." '{print $2}')
versionThird=$(echo $version | awk -F"." '{print $3}')
updatedVersion=$(echo `expr $versionThird + 1`)

updatedRelease=$versionFirst.$versionSecond.$updatedVersion
updatedFileName="ksks-flume-"$updatedRelease"-amd64"

replaceVersion="Version: $updatedRelease"
sed -i $fileName/DEBIAN/control -e $lineNumberVersion's!.*!'"$replaceVersion"'!'

{
        mv $fileName $updatedFileName
} || {
        echo "Version not changed"
}

find ./$updatedFileName -name "*~" -print0 | xargs -0 rm -rf
rm $updatedFileName/DEBIAN/md5sums
md5sum `find ./$updatedFileName -type f | awk '/.\//{ print substr($0, 3) }'` >> $updatedFileName/DEBIAN/md5sums
dpkg-deb -z8 -Zgzip --build $updatedFileName/

cp *.deb ~/Automation/Bigdata/flume

