#!/bin/bash

#------------------------------------------------------
#(0) check if there is a change inside cli directory
#(1) read the version number
#(2) increment the patch version number (X) in the memory and in the file
#(3) commit and push with incremented patch version number (X+1)
#(4) if commit and push worked then generate the package with the new (X+1) version number which must be unique, else exit
#------------------------------------------------------

#------------------------------------------------------
#(0) check if there is a change inside cli directory
#------------------------------------------------------

# This function returns 0 if variable is empty, 1 if not empty
function isChanged {

  variable=$1
  if [ -n "$variable" ]; then
    return 0
  else
    return 1
  fi
}

# Switch to root .git directory to see changes properly
pushd ..
changelogFile="debian/changelog"
cli_git_status=$(git status | grep "cli-dev/")
isChanged $cli_git_status
isCommitNecessary=$?
if [ $isCommitNecessary = "1" ]; then
  git chechout -- $changelogFile 
  exit 0
fi
popd


#------------------------------------------------------
# (1) read the version number
#------------------------------------------------------
versionLineNumber=$(sed -n '/subutai-cli-dev (/=' $changelogFile)
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
git add .
git commit -m "Auto commit while building subutai-cli-dev package"
isSuccesful=$?
git push
isSuccesful=`expr $? + $isSuccesful`

#------------------------------------------------------
#(4) if commit and push worked then generate the package with the new (X+1) version number which must be unique, else exit
#------------------------------------------------------
if [ $isSuccesful = 0 ]; then
  echo "commit and push is succesful, generating the debian package"
else
  echo "commit or push is not succesful, please fix the errors first!"
  exit 1
fi
