#!/bin/bash

#------------------------------------------------------
#(0) exit if there are uncommitted or unstaged files under the specified directory
#(1) check if there are local commits and they are related with specified path
#(2) read the version number
#(3) increment the patch version number (X) in the memory and in the file
#(4) commit and push with incremented patch version number (X+1)
#(5) if commit and push worked then generate the package with the new (X+1) version number which must be unique, else exit
#------------------------------------------------------

package_name="subutai-cli-dev"
absPath="cli-dev"
currentDirectory=$(pwd)
# This function returns true if variable is empty, false if not empty
function isEmpty {
  if [ -z "$1" ]; then
    echo "true"
  else
    echo "false"
  fi
}


function exitIfNoCommits {
  local changelogFile=$1
  branch_name="$(git symbolic-ref HEAD 2>/dev/null)" ||
  branch_name="(unnamed branch)"     # detached HEAD
  branch_name=${branch_name##refs/heads/}
  
  # Check if there are local commits
  git_diff=$(git diff origin/$branch_name..HEAD)
  local status=$?
  if [ $status == "128" ]; then
    echo "[WARN] Your branch does not exist on your remote git repository."
    echo "[WARN] This causes make process to ignore version update!"
  fi
  isDiffEmpty=$(isEmpty $git_diff)
  echo "isDiffEmpty: $isDiffEmpty"
  echo "Checking if there are local commits for branch: $branch_name"
  if [ $isDiffEmpty = "true" ]; then
    echo "No change is made on debian package"
    exit 0
  fi
}


function require_clean_work_directory {
  if [ -z "$(git status --porcelain .)" ]; then
    echo "This directory $currentDirectory is clean"
  else
    echo "Please commit the changes first under $currentDirectory directory!"
    exit 1
  fi 
}


function getListofCommits {
  commitList=($(git log origin/$branch_name..HEAD | grep "^commit" | awk '{split($0,a," "); print a[2] }'))
}


function checkCommitsForPath {
  getListofCommits
  for commit in "${commitList[@]}"
  do
    # Get list of files of this commit
    files=$(git diff-tree --no-commit-id --name-only -r $commit)
    # Check if there are changed files under the specified path
    changedFiles=$(echo $files | grep $absPath/)
    if [ -n "$changedFiles" ]; then
      isChanged="true"
      break;
    fi
  done
  if [ -n "$isChanged" -a "$isChanged" == "true" ]; then
    echo "There are changed files for $package_name"
  else
    echo "No change for path $absPath, exiting..."
    exit 0
  fi
}

changelogFile="debian/changelog"
# Ignore changes inside changelog file
git checkout -- $changelogFile > /dev/null 2>&1

#------------------------------------------------------
#(0) exit if there are uncommitted or unstaged files under the specified directory
#------------------------------------------------------
require_clean_work_directory
#------------------------------------------------------
#(1) check if there are local commits and they are related with specified path
#------------------------------------------------------
exitIfNoCommits $changelogFile
checkCommitsForPath 

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
git commit -m "Incrementing patch version of $package_name package"
isSuccesful=$?
git push origin $branch_name
isSuccesful=`expr $? + $isSuccesful`

#------------------------------------------------------
#(5) if commit and push worked then generate the package with the new (X+1) version number which must be unique, else exit
#------------------------------------------------------
if [ $isSuccesful != 0 ]; then
  echo "Commit or push is not succesful, please fix the errors first!"
  # Revert back the auto updated version
  git reset HEAD^
  git checkout -- debian/changelog
  exit 1
fi
