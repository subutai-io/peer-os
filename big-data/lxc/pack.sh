#!/bin/bash
set -e
BASE="/var/lib/jenkins/jobs/master.bigdata.lxc/Lxc"
SOURCE="/var/lib/jenkins/jobs/master.bigdata.lxc/workspace/big-data/lxc/lxc"
TARGET="/var/lib/jenkins/Automation/Bigdata/lxc"
echo $BASE

LXCSOURCE="/var/lib/lxc"
pattern="base-container"

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
#Giving static lxcbro ip address : 172.16.9.100
echo "" > $pattern/rootfs/etc/network/interfaces
echo "auto lo" >> $pattern/rootfs/etc/network/interfaces
echo "iface lo inet loopback\n" >> $pattern/rootfs/etc/network/interfaces
echo "auto eth0" >> $pattern/rootfs/etc/network/interfaces
echo "iface eth0 inet static" >> $pattern/rootfs/etc/network/interfaces
echo "address 172.16.1.100" >> $pattern/rootfs/etc/network/interfaces
echo "netmask 255.255.248.0" >> $pattern/rootfs/etc/network/interfaces
echo "gateway 172.16.0.1" >> $pattern/rootfs/etc/network/interfaces
echo "dns-nameservers 8.8.8.8" >> $pattern/rootfs/etc/network/interfaces

BRIDGE="br0"
sed -i 's/lxcbr0/'"$BRIDGE"'/g' $LXCSOURCE/$pattern/config

#Copying logstash&jmxtrans deb files.
cp $BASE/repo/ksks-logstash-1.0.1-amd64.deb $LXCSOURCE/$pattern/rootfs/root/
cp $BASE/repo/jmxtrans-1.0.1-amd64.deb $LXCSOURCE/$pattern/rootfs/root/

#start base-container
sudo lxc-start -n $pattern -d
sleep 30
#connect base-container
/$BASE/presettings.exp 172.16.1.100 ubuntu ubuntu
/$BASE/postsettings.exp 172.16.1.100 root root

#removing logstash and jmxtrans deb
rm $LXCSOURCE/$pattern/rootfs/root/ksks-logstash-1.0.1-amd64.deb
rm $LXCSOURCE/$pattern/rootfs/root/jmxtrans-1.0.1-amd64.deb

#closing base container
sudo lxc-stop -n $pattern
#updating original interfaces file
mv $pattern/rootfs/etc/network/interfaces.org $pattern/rootfs/etc/network/interfaces
#updating gmond.conf file
mv $BASE/repo/gmond.conf $pattern/rootfs/etc/ganglia/

cd $BASE
fileName=`ls | awk '{print $1}' | head -1`
echo $fileName

cp -a $LXCSOURCE/$pattern $BASE/$fileName/var/lib/lxc/
cp $SOURCE/var/lib/lxc/check.sh $BASE/$fileName/var/lib/lxc/
cp $SOURCE/var/lib/lxc/README.md $BASE/$fileName/var/lib/lxc/

if ls *.deb ; then
        rm  *.deb
fi

cd $BASE/$fileName/var/lib/lxc/

cp check.sh $BASE/$fileName/var/lib/lxc/$pattern/rootfs/etc/init.d/
cd $BASE/$fileName/var/lib/lxc/$pattern/rootfs/etc/
sed -i '10 a\/etc/init.d/check.sh' rc.local
sed -i '13 a\/. usr/bin/modifyHostname.sh' rc.local
sed -i '14 a\/service logstash start' rc.local

cd $BASE/$fileName/var/lib/lxc/
tar -cvpzf $pattern".tar.gz" $pattern/
rm -rf $pattern/

cd $BASE
cp -a $SOURCE/DEBIAN $BASE/$fileName/
cp -a $SOURCE/var $BASE/$fileName/
rm $BASE/$fileName/var/lib/lxc/README.md
rm $BASE/$fileName/var/lib/lxc/check.sh

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
