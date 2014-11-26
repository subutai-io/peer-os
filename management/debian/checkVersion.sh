#!/bin/bash
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
    exit 1;
  elif [ $versionFirst_new -eq $versionFirst_prev ]; then
     if [ $versionSecond_new -lt $versionSecond_prev ]; then
        echo "minor: current version is greater than the version in control file. Version in control file:$version_new, current version:$version_prev"
        exit 1;
     elif [ $versionSecond_new -eq $versionSecond_prev ]; then
        if [ $versionThird_new -lt $versionThird_prev ]; then
            echo "micro: current version is greater than the version in control file. Version in control file:$version_new, current version:$version_prev"
            exit 1;
        elif [ $versionThird_new -eq $versionThird_prev ]; then 
          if [ $patch_new -lt $patch_prev  ]; then
               echo "patch: current version is greater than the version in control file. Version in control file:$version_new, current version:$version_prev"
           		 exit 1;
          elif [ $patch_new -eq $patch_prev  ]; then
               echo "patch: current version is equal to the version in control file. Version in control file:$version_new, current version:$version_prev"
           		 exit 1;
          fi
        fi
     fi
  fi
}

