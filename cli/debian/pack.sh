#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs
#generic pack script for all cli subutai scripts
productName=$1
mode=cli
# 1) Check if the version is changed or not. If not changed, dont create a new debian.
checkVersion $productName $mode
# 2) Create the Debian package
generateDebianCliPackage $productName
# 3) Create the Wrapper Repo Debian Package
generateRepoPackage $productName
