#!/bin/sh
set -e
# Check if any deb file exists!!!

if [ -f ../autoconf/src/SubutaiAgent.o ] ; then
        rm ../autoconf/src/*.o
fi

if [ -f ../autoconf/src/subutai-agent ] ; then
        rm ../autoconf/src/subutai-agent
fi

if [ -f ../autoconf/src/SubutaiAgent.cpp ] ; then
        rm ../autoconf/src/*.cpp
fi
if [ -f ../autoconf/src/SubutaiThread.h ] ; then
        rm ../autoconf/src/*.h
fi
if [ -f ../autoconf/src/pugixml.hpp ] ; then
        rm ../autoconf/src/*.hpp
fi

cp $1/SubutaiAgent/src/* ../../Agent/autoconf/src/
cp $1/SubutaiAgent/debian/Agent/autoconf/* ../../Agent/autoconf/src/

sh -c 'cd ../autoconf && aclocal && autoconf && automake && autoreconf --force --install && ./configure &&  make'
if ls *.deb ; then
        rm  *.deb
fi

fileName=`ls | grep subutai|  awk '{print $1}' | head -1`

strip ../autoconf/src/subutai-agent
cp ../autoconf/src/subutai-agent $fileName/sbin/
cp $1/SubutaiAgent/config/agent.xml $fileName/etc/subutai-agent/

if [ -f $fileName/DEBIAN/control ] ; then
        rm $fileName/DEBIAN/*
fi
cp $1/SubutaiAgent/debian/Agent/DEBIAN/* $fileName/DEBIAN/

echo $fileName
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

packageName=$package"_"$version"_amd64"
echo $packageName $fileName

if [ "$packageName" != "$fileName" ] ; then
   mv $fileName $packageName
fi

find ./$packageName -name "*~" -print0 | xargs -0 rm -rf
md5sum `find ./$packageName -type f | awk '/.\//{ print substr($0, 3) }'` >> $packageName/DEBIAN/md5sums
dpkg-deb -z8 -Zgzip --build $packageName/

