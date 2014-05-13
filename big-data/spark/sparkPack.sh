#!/bin/sh

set -e

if ls | grep .deb ; then
        rm  *.deb
fi

versionOfSpark="0.9.1"

rm -rf ksks-spark*/*
cp -r ../workspace/big-data/spark/spark/* ksks-spark*/
cd ksks-spark*
wget -P opt https://archive.apache.org/dist/spark/spark-0.9.1/spark-0.9.1-bin-hadoop1.tgz
tar -xvpf opt/*.tgz -C opt/
cp -a opt/spark-0.9.1-bin-hadoop1/* opt/spark-0.9.1/
rm -rf opt/spark-0.9.1-bin-hadoop1
rm -rf opt/*.tgz
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
updatedFileName="ksks-spark-"$updatedRelease"-amd64"

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

cp *.deb ~/Automation/Bigdata/spark
