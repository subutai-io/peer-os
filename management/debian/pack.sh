#!/bin/bash
checkPackageVersion() {

  CONTROL="/var/lib/jenkins/jobs/master.get_branch_repo/workspace/management/debian/management/DEBIAN/control"

  
  lineNumberVersion=$(sed -n '/Version:/=' $CONTROL)
  lineVersion=$(sed $lineNumberVersion!d $CONTROL)
  version_new=$(echo $lineVersion | awk -F":" '{split($2,a," ");print a[1]}')
  versionFirst_new=$(echo $version_new | awk -F"." '{print $1}')
  versionSecond_new=$(echo $version_new | awk -F"." '{print $2}')

  version_tmp=$(echo $version_new | awk -F"." '{print $3}')
  versionThird_new=$(echo $version_tmp | awk -F"-" '{print $1}')
  patch_new=$(echo $version_tmp | awk -F"-" '{print $2}')

  deb_name=`ls -lt | awk '{ print $9 }' | grep .deb | head -1`
  version_prev=$(echo $deb_name | awk -F"_" '{print $2}')
  versionFirst_prev=$(echo $version_prev | awk -F"." '{print $1}')
  versionSecond_prev=$(echo $version_prev | awk -F"." '{print $2}')

  version_tmp_prev=$(echo $version_prev | awk -F"." '{print $3}')
  versionThird_prev=$(echo $version_tmp_prev | awk -F"-" '{print $1}')
  patch_prev=$(echo $version_tmp_prev | awk -F"-" '{print $2}')

  if [ -z $patch_prev ]; then
    patch_prev=0
  fi
  if [ -z $patch_new ]; then
    patch_new=0
  fi
  echo "version in control:$version_new current version:$version_prev "

  echo "new in order" $versionFirst_new $versionSecond_new $versionThird_new $patch_new
  echo "prev in order" $versionFirst_prev $versionSecond_prev $versionThird_prev $patch_prev 

  if [ $versionFirst_new -lt $versionFirst_prev ]; then
    echo "major: current version is greater than the version in control file. Version in control file:$version_new, current version:$version_prev"
    exit 0;
  elif [ $versionFirst_new -eq $versionFirst_prev ]; then
     if [ $versionSecond_new -lt $versionSecond_prev ]; then
        echo "minor: current version is greater than the version in control file. Version in control file:$version_new, current version:$version_prev"
        exit 0;
     elif [ $versionSecond_new -eq $versionSecond_prev ]; then
        if [ $versionThird_new -lt $versionThird_prev ]; then
            echo "micro: current version is greater than the version in control file. Version in control file:$version_new, current version:$version_prev"
            exit 0;
        elif [ $versionThird_new -eq $versionThird_prev ]; then 
          if [ $patch_new -lt $patch_prev  ]; then
               echo "patch: current version is greater than the version in control file. Version in control file:$version_new, current version:$version_prev"
           		 exit 0;
          elif [ $patch_new -eq $patch_prev  ]; then
               echo "patch: current version is equal to the version in control file. Version in control file:$version_new, current version:$version_prev"
           		 exit 0;
          fi
        fi
     fi
  fi
}

checkPackageVersion

#Setting SOURCE and BASE folder paths
BASE=$(pwd)
sh -c 'cd $BASE'
cd ..
ISOPATH="/var/lib/jenkins/Automation/Automation_ISO/work/custom-iso/pool/extras/"
MANAGEMENT_BASE=$(pwd)
SOURCE=$MANAGEMENT_BASE"/workspace/management/server/server-karaf/target"
BASE_SOURCE="/var/lib/jenkins/jobs/master.get_branch_repo/workspace/management/debian/management/"
echo "BASE:"$BASE
echo "MANAGEMENT_BASE:"$MANAGEMENT_BASE
echo "SOURCE:"$SOURCE
echo "BASE_SOURCE:"$BASE_SOURCE

#removing debian file
cd $BASE
sh -c 'cd $BASE'

if [ "`ls | grep deb`" != "" ]; then
   rm  *.deb
fi

#getting fileName
fileName=`ls | awk '{print $1}' | grep subutai | awk '{print $1}'`
echo "fileName:"$fileName

#copying template files from repository
rm -rf $BASE/$fileName/DEBIAN
rm -rf $BASE/$fileName/opt
rm -rf $BASE/$fileName/etc

cp -a $BASE_SOURCE/DEBIAN $BASE/$fileName/DEBIAN
cp -a $BASE_SOURCE/opt $BASE/$fileName/opt
cp -a $BASE_SOURCE/etc $BASE/$fileName/etc

#copying subutai.tar.gz file from maven output target folder
echo "copying subutai-management snapshot files.."

pushd $SOURCE
#getting the name of subutai distro for instance: subutai-2.0.0.tar.gz
distroName=`ls | grep tar.gz`
#subutai distro's folder name for instance: subutai-2.0.0 
distroFolderName=`ls | grep tar.gz | awk '{print substr($0, 0, length($0)-7)}'`
popd

if [ "$distroName" = "" ]; then
   echo "distroName is empty. Exiting."
   exit 1
fi

if [ "$distroFolderName" = "" ]; then
   echo "distroFolderName is empty. Exiting."
   exit 1
fi
 
pushd $MANAGEMENT_BASE
echo "distroName:"$distroName
echo "distroFolderName:"$distroFolderName
cp $SOURCE/$distroName $BASE/
popd

pushd $BASE
tar xzvf $distroName
cp -a $BASE/$distroFolderName/* $BASE/$fileName/opt/subutai-management/
#removing subutai and subutai.tar.gz fodler and files
rm $BASE/$distroName
rm -rf $BASE/$distroFolderName
popd

#packaging subutai-management
pushd $BASE
lineNumberVersion=$(sed -n '/Version:/=' $fileName/DEBIAN/control)
lineVersion=$(sed $lineNumberVersion!d $fileName/DEBIAN/control)
version=$(echo $lineVersion | awk -F":" '{split($2,a," ");print a[1]}')
packageName="subutai-management_"$version"_amd64"
echo "packageName:$packageName"
mv $fileName $packageName
dpkg-deb -z8 -Zgzip --build $packageName/

#copying subutai-management.deb file to our pool
cp $packageName".deb" /var/lib/jenkins/Automation/Bigdata/management/

#editing package properties 
chown jenkins:jenkins *.deb
chmod -R 644 *.deb

#get package latest package from ISOPATH to remove it
removedPackage=`ls -lt $ISOPATH | grep "subutai-management" | awk '{ print $9 }' | grep .deb | head -1`
echo "removedPackageName: $removedPackage" 
if [ "$removedPackage" != "" ]; then
	rm $ISOPATH/$removedPackage
fi
cp $packageName".deb" $ISOPATH

popd

