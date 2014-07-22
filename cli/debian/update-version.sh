#!/bin/bash
set -e

#------------------------------------------------------
#(1) read the version number
#(2) increment the patch version number (X) in the memory and in the file
#(3) commit and push with incremented patch version number (X+1)
#(4) if commit and push worked then generate the package with the new (X+1) version number which must be unique, else exit
#------------------------------------------------------

#------------------------------------------------------
# (1) read the version number
#------------------------------------------------------
changelogFile="debian/changelog"

versionLineNumber=$(sed -n '/subutai-cli (/=' $changelogFile)
versionLineContent=$(sed $versionLineNumber!d $changelogFile)
version=$(echo $versionLineContent | awk -F" " '{split($2,a," ");print a[1]}')
firstParanthesisIndex=$(echo `expr index $version "\("`)
lastParantesisIndex=$(echo `expr index $version "\)"`)
lastParantesisIndex=`expr $lastParantesisIndex - 2 `
version=${version:$firstParanthesisIndex:$lastParantesisIndex}
echo "Current version:" $version

versionFirst=$(echo $version | awk -F"." '{print $1}')
versionSecond=$(echo $version | awk -F"." '{print $2}')
versionThird=$(echo $version | awk -F"-" '{print $1}')
versionThird=$(echo $versionThird | awk -F"." '{print $3}')
versionPatch=$(echo $version | awk -F"-" '{print $2}')
updatedPatch=$(echo `expr $versionPatch + 1`)


#------------------------------------------------------
#(2) increment the patch version number (X) in the memory and in the file
#------------------------------------------------------
updatedVersion=$versionFirst.$versionSecond.$versionThird-$updatedPatch
echo "Updated version:" $updatedVersion
sed -i "s/$version/$updatedVersion/1" $changelogFile


#------------------------------------------------------
#(3) commit and push with incremented patch version number (X+1)
#------------------------------------------------------
git add $changelogFile
git commit -m "subutai-cli package version change"
git push
isSuccesful=$?

#------------------------------------------------------
#(4) if commit and push worked then generate the package with the new (X+1) version number which must be unique, else exit
#------------------------------------------------------
if [ $isSuccesful = 0 ]; then
  echo "Push is succesful, generating the debian package"
else
  echo "Push is not succesful, please fix the errors first!"
  exit 1
fi
