#!/bin/bash

#------------------------------------------------------
#(0) exit if there are uncommitted or unstaged files
#(1) check if there are local commits, return if there is no commit
#(2) read the version number
#(3) increment the patch version number (X) in the memory and in the file
#(4) commit and push with incremented patch version number (X+1)
#(5) if commit and push worked then generate the package with the new (X+1) version number which must be unique, else exit
#------------------------------------------------------

package_name="subutai-cli"
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
  branch_name="$(git symbolic-ref HEAD 2>/dev/null)" ||
  branch_name="(unnamed branch)"     # detached HEAD
  branch_name=${branch_name##refs/heads/}
  
  # Check if there are local commits
  git_diff=$(git diff origin/$branch_name..HEAD)
  isDiffEmpty=$(isEmpty $git_diff)
  echo "isDiffEmpty: $isDiffEmpty"
  echo "Checking if there are local commits for branch: $branch_name"
  if [ $isDiffEmpty = "true" ]; then
    echo "No change is made on debian package"
    exit 0
  fi
}


require_clean_work_tree () {
    # Update the index
    git update-index -q --ignore-submodules --refresh
    err=0

    # Disallow unstaged changes in the working tree
    if ! git diff-files --quiet --ignore-submodules --
    then
        echo >&2 "cannot $1: you have unstaged changes."
        git diff-files --name-status -r --ignore-submodules -- >&2
        err=1
    fi

    # Disallow uncommitted changes in the index
    if ! git diff-index --cached --quiet HEAD --ignore-submodules --
    then
        echo >&2 "cannot $1: your index contains uncommitted changes."
        git diff-index --cached --name-status -r --ignore-submodules HEAD -- >&2
        err=1
    fi

    if [ $err = 1 ]
    then
        echo >&2 "Please commit or stash them."
        exit 1
    fi
}


changelogFile="debian/changelog"
# Ignore changes inside changelog file
git checkout -- $changelogFile > /dev/null 2>&1

#------------------------------------------------------
#(0) exit if there are uncommitted or unstaged files
#------------------------------------------------------
require_clean_work_tree
#------------------------------------------------------
#(1) check if there are local commits, return if there is no commit
#------------------------------------------------------
exitIfNoChange $changelogFile

#----------------UPDATE_VERSION-----------------------
#------------------------------------------------------
# (2) read the version number
#------------------------------------------------------
versionLineNumber=$(sed -n '/'$package_name' (/=' $changelogFile)
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
#(3) increment the patch version number (X) in the memory and in the file
#------------------------------------------------------
updatedVersion=$versionFirst.$versionSecond.$versionThird-$updatedPatch
echo "Updated version:" $updatedVersion
sed -i "s/$version/$updatedVersion/1" $changelogFile


#------------------------------------------------------
#(4) commit and push with incremented patch version number (X+1) if there are uncommitted changes
#------------------------------------------------------
git add .
git commit -m "Auto commit while building $package_name package"
isSuccesful=$?
git push origin $branch_name
isSuccesful=`expr $? + $isSuccesful`

#------------------------------------------------------
#(5) if commit and push worked then generate the package with the new (X+1) version number which must be unique, else exit
#------------------------------------------------------
if [ $isSuccesful != 0 ]; then
  echo "Commit or push is not succesful, please fix the errors first!"
  exit 1
fi
