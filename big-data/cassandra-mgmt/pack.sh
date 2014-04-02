#!/bin/bash
set -e
BASE="/var/lib/jenkins/jobs/master.bigdata.cassandra-mgmt/Cassandra-mgmt"
SOURCE="/var/lib/jenkins/jobs/master.bigdata.cassandra-mgmt/workspace/big-data/cassandra-mgmt/cassandra-mgmt"
TARGET="/var/lib/jenkins/Automation/Bigdata/cassandra-mgmt"
kskssql="/var/lib/jenkins/jobs/master.bigdata.cassandra-mgmt/workspace/keyspace"
echo $BASE

LXCSOURCE="/var/lib/lxc"
pattern="cassandra"

cd $LXCSOURCE
pwd

#mkdir base-container

#Creating lxc package
mylxc=$(sudo ls | grep $pattern)
echo $mylxc

if [ -z "$mylxc" ] ;then
  echo "not found a pattern"
  sudo lxc-create -t ubuntu -n $pattern
else
  echo "pattern is found"
  sudo lxc-stop -n $pattern
  sudo lxc-destroy -n $mylxc
  sudo lxc-create -t ubuntu -n $pattern
fi

#Editing lxc package
cp $pattern/rootfs/etc/network/interfaces $pattern/rootfs/etc/network/interfaces.org
#backup interfaces file
#Giving static lxcbro ip address : 172.16.1.101
echo "" > $pattern/rootfs/etc/network/interfaces
echo "auto lo" >> $pattern/rootfs/etc/network/interfaces
echo "iface lo inet loopback\n" >> $pattern/rootfs/etc/network/interfaces
echo "auto eth0" >> $pattern/rootfs/etc/network/interfaces
echo "iface eth0 inet static" >> $pattern/rootfs/etc/network/interfaces
echo "address 172.16.1.101" >> $pattern/rootfs/etc/network/interfaces
echo "netmask 255.255.248.0" >> $pattern/rootfs/etc/network/interfaces
echo "gateway 172.16.0.1" >> $pattern/rootfs/etc/network/interfaces
echo "dns-nameservers 8.8.8.8" >> $pattern/rootfs/etc/network/interfaces

BRIDGE="br0"
sed -i 's/lxcbr0/'"$BRIDGE"'/g' $LXCSOURCE/$pattern/config

#Copying logstash&jmxtrans deb files.
cp $BASE/repo/ksks-cassandra-mgmt-1.0.8-amd64.deb $LXCSOURCE/$pattern/rootfs/root/
cp $kskssql/kiskis.sql $LXCSOURCE/$pattern/rootfs/root/

#start base-container
sudo lxc-start -n $pattern -d
sleep 30
#connect cassandra lxc container
/$BASE/presettings.exp 172.16.1.101 ubuntu ubuntu
/$BASE/postsettings.exp 172.16.1.101 root root

#removing cassandra-mgmt and kiskis.sql
rm $LXCSOURCE/$pattern/rootfs/root/ksks-cassandra-mgmt-1.0.8-amd64.deb
rm $LXCSOURCE/$pattern/rootfs/root/kiskis.sql

#closing base container
sudo lxc-stop -n $pattern
#updating original interfaces file
mv $pattern/rootfs/etc/network/interfaces.org $pattern/rootfs/etc/network/interfaces

cd $BASE
fileName=`ls | awk '{print $1}' | head -1`
echo $fileName

cp -a -p $LXCSOURCE/$pattern/rootfs/var/lib/cassandra $BASE/$fileName/var/lib/

if ls *.deb ; then
        rm  *.deb
fi

cd $BASE
cp -a $SOURCE/DEBIAN $BASE/$fileName/
cp -a $SOURCE/etc $BASE/$fileName/
cp -a $SOURCE/opt $BASE/$filename/

lineNumberVersion=$(sed -n '/Version:/=' $fileName/DEBIAN/control)
lineNumberPackage=$(sed -n '/Package:/=' $fileName/DEBIAN/control)
lineVersion=$(sed $lineNumberVersion!d $fileName/DEBIAN/control)
linePackage=$(sed $lineNumberPackage!d $fileName/DEBIAN/control)

version=$(echo $lineVersion | awk -F":" '{split($2,a," ");print a[1]}')
package=$(echo $linePackage | awk -F":" '{split($2,a," ");print a[1]}')
echo $version
echo $package

versionFirst=$(echo $version | awk -F"." '{print $1}')
versionSecond=$(echo $version | awk -F"." '{print $2}')
versionThird=$(echo $version | awk -F"." '{print $3}')

echo $versionFirst
echo $versionSecond
echo $versionThird

updatedVersion=$(echo `expr $versionThird + 1`)
updatedRelease=$versionFirst.$versionSecond.$versionThird
replaceVersion="Version: $updatedRelease"
sed -i $fileName/DEBIAN/control -e $lineNumberVersion's!.*!'"$replaceVersion"'!'
packageName=$package-$updatedRelease"-amd64"
echo $packageName

if [ "$fileName" != "$packageName" ] ;then
echo "different!!"
mv $fileName $packageName
fi

find ./$packageName -name "*~" -print0 | xargs -0 rm -rf
rm $packageName/DEBIAN/md5sums
md5sum `find ./$packageName -type f | awk '/.\//{ print substr($0, 3) }'` >> $packageName/DEBIAN/md5sums
dpkg-deb -z8 -Zgzip --build $packageName/

cp $packageName".deb" $TARGET/
