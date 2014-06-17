#!/bin/sh
set -e
if ls | grep .deb ; then
        rm  *.deb
fi
esVersion=1.2.1

BASE=$(pwd)
sh -c 'cd $BASE'
echo $BASE
cd ../workspace
SOURCE=$(pwd)"/big-data/elasticsearch/elasticsearch"
TARGET="/var/lib/jenkins/Automation/Bigdata/elasticsearch"
echo $SOURCE
echo $TARGET
cd $BASE

fileName=`ls | grep ksks | awk '{print $1}' | head -1`
echo $fileName

if [ -d "$fileName/DEBIAN" ]; then
   rm -rf $fileName/DEBIAN
fi

if [ -d "$fileName/etc" ]; then
   rm -rf $fileName/etc
fi

if [ -d "$fileName/usr" ]; then
   rm -rf $fileName/usr
fi

wget https://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-$esVersion.deb -P $BASE/$fileName/

dpkg-deb -e $fileName/elasticsearch-$esVersion.deb $fileName/DEBIAN/
dpkg-deb -x $fileName/elasticsearch-$esVersion.deb $fileName/

cp -a $BASE/../workspace/big-data/elasticsearch/elasticsearch/* $BASE/$fileName

rm $BASE/$fileName/elasticsearch-$esVersion.deb
dpkg-deb -z8 -Zgzip --build $fileName
cp ksks*.deb /var/lib/jenkins/Automation/Bigdata/elasticsearch
