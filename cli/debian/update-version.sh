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

# This function returns true if variable is empty, false if not empty
function isEmpty {
  if [ -z "$1" ]; then
    echo "true"
  else
    echo "false"
  fi
}


function exitIfNoChange {
  local changelogFile=$1
  # Ignore changes in this file before decision
  git checkout -- $changelogFile > /dev/null 2>&1
  branch_name="$(git symbolic-ref HEAD 2>/dev/null)" ||
  branch_name="(unnamed branch)"     # detached HEAD
  branch_name=${branch_name##refs/heads/}
  # Check if there are uncommitted changes
  git_status=$(git status | grep "cli/")
  isStatusEmpty=$(isEmpty $git_status)
  
  # Check if there are local commits not pushed
  git_diff=$(git diff origin/$branch_name..HEAD)
  isDiffEmpty=$(isEmpty $git_diff)
  echo "isStatusEmpty: $isStatusEmpty"
  echo "isDiffEmpty: $isDiffEmpty"
  echo "Checking if there is a change for branch: $branch_name"
  if [ $isStatusEmpty = "true" -a $isDiffEmpty = "true" ]; then
    echo "No change is made on debian package"
    exit 0
  fi
}

changelogFile="debian/changelog"
# Switch to root .git directory to see changes properly
pushd ..
exitIfNoChange $changelogFile
popd

#----------------UPDATE_VERSION-----------------------
#------------------------------------------------------
# (1) read the version number
#------------------------------------------------------
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
#(3) commit and push with incremented patch version number (X+1) if there are uncommitted changes
#------------------------------------------------------
if [ $isStatusEmpty = "false" ]; then
  git add .
  git commit -m "Auto commit while building subutai-cli package"
  isSuccesful=$?
else
  isSuccesful=0
fi
git push origin $branch_name
isSuccesful=`expr $? + $isSuccesful`
#------------------------------------------------------
#(4) if commit and push worked then generate the package with the new (X+1) version number which must be unique, else exit
#------------------------------------------------------
if [ $isSuccesful != 0 ]; then
  echo "Commit or push is not succesful, please fix the errors first!"
  exit 1
fi
