#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.agent/Agent/deb/pack-funcs

# 1) Check if the version is changed or not. If not changed, dont create a new debian.
checkPackageVersion $1
# 2) Remove sources under agent related directories
removePreviousVersionSource $1
# 3) Get new sources under agent related directories
getNewVersionSource $1
# 4) compile new agent sources
runMake $1
# 5) copy needed files under subutai-agent folder like agent.xml
getSourcesToRelevantDirectories $1
# 6) create debian package for agent
createDebian $1
# 7) Generate repo package for debian.
generateRepoPackage
