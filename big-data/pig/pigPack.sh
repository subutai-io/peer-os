#!/bin/sh

set -e

if ls | grep .deb ; then
        rm  *.deb
fi

versionOfPig="0.12.0"

rm -rf ksks-pig*/*
cp -r ../workspace/big-data/pig/pig/* ksks-pig*/
cd ksks-pig*
mkdir opt
wget -P opt http://archive.apache.org/dist/pig/pig-0.12.0/pig-0.12.0.tar.gz
tar -xvpf opt/*.tar.gz -C opt/
rm -rf opt/*.tar.gz
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
updatedFileName="ksks-pig-"$updatedRelease"-amd64"

replaceVersion="Version: $updatedRelease"
sed -i $fileName/DEBIAN/control -e $lineNumberVersion's!.*!'"$replaceVersion"'!'

{
        mv $fileName $updatedFileName
} || {
        echo "Version not changed"
}


sed -i s/pig-[0-9]*.[0-9]*.[0-9]*'\/'/pig-$versionOfPig'\/'/g $updatedFileName/DEBIAN/conffile
sed -i s/pig=\"pig-[0-9]*.[0-9]*.[0-9]*\"/pig=\"pig-$versionOfPig\"/g $updatedFileName/DEBIAN/postinst
sed -i s/pig=\"pig-[0-9]*.[0-9]*.[0-9]*\"/pig=\"pig-$versionOfPig\"/g $updatedFileName/DEBIAN/postrm
sed -i s/pig=\"pig-[0-9]*.[0-9]*.[0-9]*\"/pig=\"pig-$versionOfPig\"/g $updatedFileName/etc/init.d/pig
#sed -i s/pig-[0-9]*.[0-9]*.[0-9]*/pig-$versionOfPig/g $updatedFileName/opt/pig-$versionOfPig/conf/pig-env.sh



find ./$updatedFileName -name "*~" -print0 | xargs -0 rm -rf
rm $updatedFileName/DEBIAN/md5sums
md5sum `find ./$updatedFileName -type f | awk '/.\//{ print substr($0, 3) }'` >> $updatedFileName/DEBIAN/md5sums
dpkg-deb -z8 -Zgzip --build $updatedFileName/

cp *.deb ~/Automation/Bigdata/pig
