#!/bin/bash
set -e
# Check if any deb file exists!!!
if ls *.deb ; then
        rm  *.deb
fi
# Get the directory name
fileName=`ls | grep subutai-host-script | awk '{print $1}' | head -1`

# Get line numbers of version and package information 
lineNumberVersion=$(sed -n '/Version:/=' $fileName/DEBIAN/control)
lineNumberPackage=$(sed -n '/Package:/=' $fileName/DEBIAN/control)

# Get the values of version and package information
lineVersion=$(sed $lineNumberVersion!d $fileName/DEBIAN/control)
linePackage=$(sed $lineNumberPackage!d $fileName/DEBIAN/control)

# Get rid of unrelated information from version and package information 
version=$(echo $lineVersion | awk -F":" '{split($2,a," ");print a[1]}')
package=$(echo $linePackage | awk -F":" '{split($2,a," ");print a[1]}')

# Parse version information
versionFirst=$(echo $version | awk -F"." '{print $1}')
versionSecond=$(echo $version | awk -F"." '{print $2}')
versionThird=$(echo $version | awk -F"." '{print $3}')

# Increment least significant version number automatically by one
#updatedVersionThird=$(echo `expr $versionThird + 1`)
updatedVersionThird=$versionThird
updatedRelease=$versionFirst.$versionSecond.$updatedVersionThird
replaceVersion="Version: $updatedRelease"

packageName=$package

# Update control file with the incremented version
sed -i $fileName/DEBIAN/control -e $lineNumberVersion's!.*!'"$replaceVersion"'!'

if [ "$fileName" != "$packageName" ] ;then
	mv $fileName $packageName
fi

find ./$packageName -name "*~" -print0 | xargs -0 rm -rf
if [ -f "$packageName/DEBIAN/md5sums" ]; then
	rm $packageName/DEBIAN/md5sums 
fi
md5sum `find ./$packageName -type f | awk '/.\//{ print substr($0, 3) }'` >> $packageName/DEBIAN/md5sums
dpkg-deb -z8 -Zgzip --build $packageName/
