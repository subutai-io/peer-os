#!/bin/sh
set -e
# Check if any deb file exists!!!
if ls *.deb ; then
	rm  *.deb
fi

versionOfCassandra=2.0.4

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

packageName=$package-$updatedRelease"-amd64"

{
	mv $fileName $packageName
} || {
	echo "Version not changed"
}

find ./$packageName -name "*~" -print0 | xargs -0 rm -rf
rm $packageName/DEBIAN/md5sums
md5sum `find ./$packageName -type f | awk '/.\//{ print substr($0, 3) }'` >> $packageName/DEBIAN/md5sums

sed -i s/cassandra-[0-9]*.[0-9]*.[0-9]*'\/'/cassandra-$versionOfCassandra'\/'/g $packageName/DEBIAN/conffile
sed -i s/cassVer=\"cassandra-[0-9]*.[0-9]*.[0-9]*\"/cassVer=\"cassandra-$versionOfCassandra\"/g $packageName/DEBIAN/postinst
sed -i s/cassVer=\"cassandra-[0-9]*.[0-9]*.[0-9]*\"/cassVer=\"cassandra-$versionOfCassandra\"/g $packageName/DEBIAN/postrm
sed -i s/cassVer=\"cassandra-[0-9]*.[0-9]*.[0-9]*\"/cassVer=\"cassandra-$versionOfCassandra\"/g $packageName/DEBIAN/prerm
sed -i s/cassVer=\"cassandra-[0-9]*.[0-9]*.[0-9]*\"/cassVer=\"cassandra-$versionOfCassandra\"/g $packageName/etc/init.d/cassandra
sed -i s/cassJAR=\"apache-cassandra-[0-9]*.[0-9]*.[0-9]*.jar\"/cassJAR=\"apache-cassandra-$versionOfCassandra.jar\"/g $packageName/etc/init.d/cassandra
sed -i s@log4j.appender.R.File=.*@log4j.appender.R.File=\/var\/log\/cassandra-$versionOfCassandra\/logs@g $packageName/opt/cassandra-$versionOfCassandra/conf/log4j-server.properties

dpkg-deb -z8 -Zgzip --build $packageName/




