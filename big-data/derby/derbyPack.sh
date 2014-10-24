#!/bin/sh

set -e
if ls | grep .deb ; then
        rm  *.deb
fi

rm -rf subutai-derby*/*
cp -r  ../workspace/big-data/derby/derby/* subutai-derby*/
cd subutai-derby*
mkdir opt
wget -P opt http://archive.apache.org/dist/db/derby/db-derby-10.4.2.0/db-derby-10.4.2.0-bin.tar.gz
cd -

fileName=`ls | grep subutai | awk '{print $1}' | head -1`

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
updatedFileName="subutai-derby-"$updatedRelease"-amd64"

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

cp *.deb ~/Automation/Bigdata/derby
