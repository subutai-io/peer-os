#!/bin/bash

set -e

if ls | grep .deb ; then
        rm  *.deb
fi

rm -rf ksks-hbase*/*
cp -r ~/jobs/master.bigdata.hbase/workspace/big-data/hbase/hbase/* ksks-hbase*/
cd ksks-hbase*
rm -rf opt
mkdir opt
wget -P opt http://archive.apache.org/dist/hbase/hbase-0.94.16/hbase-0.94.16.tar.gz
tar -xvpf opt/*.tar.gz -C opt/
rm -rf opt/*.tar.gz
mkdir opt/hbase-0.94.16/zookeeper
cd -
cp -r ~/jobs/master.bigdata.hbase/workspace/big-data/hbase/hbase/opt/hbase-0.94.16/conf/* ksks-hbase*/opt/hbase-0.94.16/conf/
cp -r ~/jobs/master.bigdata.hbase/workspace/big-data/hbase/hbase/opt/hbase-0.94.16/scripts ksks-hbase*/opt/hbase-0.94.16/
rm ksks-hbase*/opt/hbase-0.94.16/lib/libthrift-0.8.0.jar
cp libthrift-0.9.0.jar ksks-hbase*/opt/hbase-0.94.16/lib/
chmod +x ksks-hbase*/opt/hbase-0.94.16/conf/hbase-env.sh
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
updatedFileName="ksks-hbase-"$updatedRelease"-amd64"

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

cp *.deb ~/Automation/Bigdata/hbase

