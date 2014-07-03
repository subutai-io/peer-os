#!/bin/bash
set -e

# Check if any deb file exists!!!
if ls *.deb ; then
	rm  *.deb
fi

#############################################################################################
# 1) download cassandra and make required configurations
#############################################################################################

# version of cassandra 
cassVersion=2.0.9

# download cassandra tar ball 
wget http://archive.apache.org/dist/cassandra/$cassVersion/apache-cassandra-$cassVersion-bin.tar.gz

# unpack tar ball 
tar -xpf *.tar.gz

# remove tar ball and ksks-cassandra directory
echo "Removing tar ball..."

rm -rf *.tar.gz
rm -rf ksks-cassandra*/opt/*

# copy files of downloaded cassadnra to ksks-cassandra/opt directory 
cp -r apache-cassandra* ksks-cassandra*/opt/
rm -r apache-cassandra*
cd ksks-cassandra*/opt/

# change name of downloaded cassandra to cassandra-${cassVersion}
mv apache-cassandra* cassandra-$cassVersion
cd -

# copy files under DEBIAN folder
cp ../workspace/big-data/cassandra/cassandra/DEBIAN/*  ksks-cassandra*/DEBIAN

# copy files under etc folder
cp -r ../workspace/big-data/cassandra/cassandra/etc/* ksks-cassandra*/etc

# migrate configuration files under /etc/cassandra folder. 
cp -a ksks-cassandra*/opt/cassandra*/conf/* ksks-cassandra*/etc/cassandra/

# remove old conf directory
rm -rf ksks-cassandra*/opt/cassandra*/conf

#############################################################################################
# 2) debian package cretaion phase 
#############################################################################################

fileName=`ls | grep ksks | awk '{print $1}' | head -1`

lineNumberVersion=$(sed -n '/Version:/=' $fileName/DEBIAN/control)
lineNumberPackage=$(sed -n '/Package:/=' $fileName/DEBIAN/control)

lineVersion=$(sed $lineNumberVersion!d $fileName/DEBIAN/control)
linePackage=$(sed $lineNumberPackage!d $fileName/DEBIAN/control)

version=$(echo $lineVersion | awk -F":" '{split($2,a," ");print a[1]}')
package=$(echo $linePackage | awk -F":" '{split($2,a," ");print a[1]}')

versionFirst=$(echo $version | awk -F"." '{print $1}')
versionSecond=$(echo $version | awk -F"." '{print $2}')
versionThird=$(echo $version | awk -F"." '{print $3}')

updatedVersion=$(echo `expr $versionThird + 1`)

updatedRelease=$versionFirst.$versionSecond.$updatedVersion

replaceVersion="Version: $updatedRelease"

sed -i $fileName/DEBIAN/control -e $lineNumberVersion's!.*!'"$replaceVersion"'!'

# append amd64 to package name
packageName=$package-$updatedRelease"-amd64"

{ 
	mv $fileName $packageName 
} || 
{ 
	echo "Version not changed" 
}

# delete all files containing ~ inside its file name. 
find ./$packageName -name "*~" -print0 | xargs -0 rm -rf

# delete md5sums file
rm $packageName/DEBIAN/md5sums

# create md5sums file
md5sum `find ./$packageName -type f | awk '/.\//{ print substr($0, 3) }'` >> $packageName/DEBIAN/md5sums

# change version of cassandra accordingly inside scripts
sed -i s/cassandra-[0-9]*.[0-9]*.[0-9]*'\/'/cassandra-$cassVersion'\/'/g $packageName/DEBIAN/conffile
sed -i s/cassVer=\"cassandra-[0-9]*.[0-9]*.[0-9]*\"/cassVer=\"cassandra-$cassVersion\"/g $packageName/DEBIAN/postinst
sed -i s/cassVer=\"cassandra-[0-9]*.[0-9]*.[0-9]*\"/cassVer=\"cassandra-$cassVersion\"/g $packageName/DEBIAN/postrm
sed -i s/cassVer=\"cassandra-[0-9]*.[0-9]*.[0-9]*\"/cassVer=\"cassandra-$cassVersion\"/g $packageName/DEBIAN/prerm
sed -i s/cassVer=\"cassandra-[0-9]*.[0-9]*.[0-9]*\"/cassVer=\"cassandra-$cassVersion\"/g $packageName/etc/init.d/cassandra
sed -i s/cassJAR=\"apache-cassandra-[0-9]*.[0-9]*.[0-9]*.jar\"/cassJAR=\"apache-cassandra-$cassVersion.jar\"/g $packageName/etc/init.d/cassandra
sed -i s@log4j.appender.R.File=.*@log4j.appender.R.File=\/var\/log\/cassandra\/logs@g $packageName/etc/cassandra/log4j-server.properties

# create debian package
dpkg-deb -z8 -Zgzip --build $packageName/

# copy newly created debian package under Automation folder
cp *.deb /var/lib/jenkins/Automation/Bigdata/cassandra

