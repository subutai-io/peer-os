#!/bin/bash

#Setting SOURCE and BASE folder paths
BASE=$(pwd)
sh -c 'cd $BASE'
cd ..
MANAGEMENT_BASE=$(pwd)
SOURCE=$MANAGEMENT_BASE"/workspace/management/server/server-karaf/target"
BASE_SOURCE=$MANAGEMENT_BASE"/workspace/management"
echo "BASE:"$BASE
echo "MANAGEMENT_BASE:"$MANAGEMENT_BASE
echo "SOURCE:"$SOURCE
echo "BASE_SOURCE:"$BASE_SOURCE

#removing debian file
cd $BASE
sh -c 'cd $BASE'
if ls *.deb ; then
        rm  *.deb
fi

#getting fileName
fileName=`ls | awk '{print $1}' | grep subutai | awk '{print $1}'`
echo "fileName:"$fileName

#copying template files from repository
rm -rf $BASE/$fileName/DEBIAN
rm -rf $BASE/$fileName/opt

cp -a $BASE_SOURCE/debian/management/DEBIAN $BASE/$fileName/DEBIAN
cp -a $BASE_SOURCE/debian/management/opt $BASE/$fileName/opt

#copying subutai.tar.gz file from maven output target folder
echo "copying subutai-management snapshot files.."

cd $SOURCE
#getting the name of subutai distro for instance: subutai-2.0.0.tar.gz
distroName=`ls | grep tar.gz`
#subutai distro's folder name for instance: subutai-2.0.0 
distroFolderName=`ls | grep tar.gz | awk '{print substr($0, 0, length($0)-7)}'`
cd $MANAGEMENT_BASE

echo "distroName:"$distroName
echo "distroFolderName:"$distroFolderName

cp $SOURCE/$distroName $BASE/
cd $BASE
tar xzvf $distroName
cp -a $BASE/$distroFolderName/* $BASE/$fileName/opt/subutai-management/

#removing subutai and subutai.tar.gz fodler and files
rm $BASE/$distroName
rm -rf $BASE/$distroFolderName

#packaging subutai-management
cd $BASE
lineNumberVersion=$(sed -n '/Version:/=' $fileName/DEBIAN/control)
lineNumberPackage=$(sed -n '/Package:/=' $fileName/DEBIAN/control)
echo $lineNumberVersion
echo $lineNumberPackage

lineVersion=$(sed $lineNumberVersion!d $fileName/DEBIAN/control)
linePackage=$(sed $lineNumberPackage!d $fileName/DEBIAN/control)
echo $lineVersion
echo $linePackage

version=$(echo $lineVersion | awk -F":" '{split($2,a," ");print a[1]}')
package=$(echo $linePackage | awk -F":" '{split($2,a," ");print a[1]}')
echo $version
echo $package

versionFirst=$(echo $version | awk -F"." '{print $1}')
versionSecond=$(echo $version | awk -F"." '{print $2}')
echo $versionFirst
echo $versionSecond

updatedVersion=$(echo `expr $versionSecond`)
echo $updatedVersion
updatedRelease=$versionFirst.$updatedVersion
echo $updatedRelease

replaceVersion="Version: $updatedRelease"
echo $replaceVersion
sed -i $fileName/DEBIAN/control -e $lineNumberVersion's!.*!'"$replaceVersion"'!'

packageName=$package-$updatedRelease"_amd64"
echo $packageName

if [ $fileName = $packageName ]
then
   dpkg-deb -z8 -Zgzip --build $fileName/
else
   mv $fileName $packageName
   dpkg-deb -z8 -Zgzip --build $packageName/
fi

#copying subutai-management.deb file to our pool
cp $packageName".deb" /var/lib/jenkins/Automation/Bigdata/management/

#editing package properties 
chown jenkins:jenkins *.deb
chmod -R 644 *.deb
