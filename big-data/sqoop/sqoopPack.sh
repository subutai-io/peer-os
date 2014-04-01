#!/bin/sh

set -e

if ls | grep .deb ; then
        rm  *.deb
fi

rm -rf ksks-sqoop*/*
cp -r ~/jobs/master.bigdata.sqoop/workspace/big-data/sqoop/sqoop/* ksks-sqoop*/
cd ksks-sqoop*
mkdir opt
wget -P opt http://archive.apache.org/dist/sqoop/1.4.3/sqoop-1.4.3.bin__hadoop-1.0.0.tar.gz
tar -xvpf opt/*.tar.gz -C opt/
rm -rf opt/*.tar.gz
mv opt/* opt/sqoop-1.4.3
cd -
cp extraJar/*.jar ksks-sqoop*/opt/sqoop-1.4.3/lib


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
updatedFileName="ksks-sqoop-"$updatedRelease"-amd64"

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

cp *.deb ~/Automation/Bigdata/sqoop

