#!/bin/sh
set -e
if ls | grep .deb ; then
        rm  *.deb
fi
esVersion=1.2.1
wget https://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-esVersion.deb

fileName=`ls | grep ksks | awk '{print $1}' | head -1`
rm -rf $fileName/*
dpkg-deb -e elasticsearch-esVersion.deb $fileName/DEBIAN/
dpkg-deb -x elasticsearch-esVersion.deb $fileName/

lineNumberVersion=$(sed -n '/Version:/=' $fileName/DEBIAN/control)
lineNumberMaintainer=$(sed -n '/Maintainer:/=' $fileName/DEBIAN/control)
lineNumberPackage=$(sed -n '/Package:/=' $fileName/DEBIAN/control)


versionOne=`ls | grep ksks | awk -F "-" '/1/ {print $3}'| awk -F "." '/1/ {print $1}'`
versionTwo=`ls | grep ksks | awk -F "-" '/1/ {print $3}'| awk -F "." '/1/ {print $2}'`
versionThree=`ls | grep ksks | awk -F "-" '/1/ {print $3}'| awk -F "." '/1/ {print $3}'`
updatedVersionThree=$(echo `expr $versionThree + 1`)
updatedRelease=$versionOne.$versionTwo.$updatedVersionThree
updatedFileName="ksks-mongo-"$updatedRelease"-amd64"

replaceVersion="Version: $updatedRelease"
sed -i $fileName/DEBIAN/control -e $lineNumberVersion's!.*!'"$replaceVersion"'!'

replaceMaintainer="Maintainer: kiskis"
replacePackage="Package: ksks-elasticsearch"
sed -i $fileName/DEBIAN/control -e $lineNumberMaintainer's!.*!'"$replaceMaintainer"'!'
sed -i $fileName/DEBIAN/control -e $lineNumberPackage's!.*!'"$replacePackage"'!'

mv $fileName $updatedFileName
find ./$updatedFileName -name "*~" -print0 | xargs -0 rm -rf
rm $updatedFileName/DEBIAN/md5sums
md5sum `find ./$updatedFileName -type f | awk '/.\//{ print substr($0, 3) }'` >> $updatedFileName/DEBIAN/md5sums
dpkg-deb -z8 -Zgzip --build $updatedFileName/


cp ksks*.deb /var/lib/jenkins/Automation/Bigdata/elasticsearch
