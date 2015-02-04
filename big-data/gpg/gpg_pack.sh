#!/bin/bash
set -e
. /var/lib/jenkins/jobs/master.get_branch_repo/workspace/big-data/pack-funcs

productName=gpg
mode=bdproduct
downloads=gpg/opt/subutai-gpg
  
libgpg_error_url="ftp://ftp.gnupg.org/gcrypt/libgpg-error/libgpg-error-1.17.tar.bz2"
libgcrypt_url="ftp://ftp.gnupg.org/gcrypt/libgcrypt/libgcrypt-1.6.2.tar.bz2"
libksba_url="ftp://ftp.gnupg.org/gcrypt/libksba/libksba-1.3.2.tar.bz2"
libassuan_url="ftp://ftp.gnupg.org/gcrypt/libassuan/libassuan-2.2.0.tar.bz2"
npth_url="ftp://ftp.gnupg.org/gcrypt/npth/npth-1.1.tar.bz2"
gnupg_url="ftp://ftp.gnupg.org/gcrypt/gnupg/gnupg-2.1.1.tar.bz2"

downloadAndMakeChanges() {
	initializeVariables $1

  assert_root_user
  # apt-get update
  # assert_packages_available $packages_should_be_installed
  # install_latest_packages $packages_should_be_installed

  mkdir -p $downloads

  download_n_compile $libgpg_error_url
  download_n_compile $libgcrypt_url
  download_n_compile $libksba_url
  download_n_compile $libassuan_url
  download_n_compile $npth_url
  download_n_compile $gnupg_url
}


download_n_compile(){
  url="$1"
  tarFile=`echo $url | awk -F "/" {'print $NF'}`
  fileName=`echo $url | awk -F "/" {'print $(NF-1)'}`
  if [ ! -f $downloads/$tarFile ]; then
    wget $url -P $downloads
  fi
  pushd $downloads
  tar -xvpf $tarFile > /dev/null 2>&1
  pushd $fileName*
  ./configure
  make
  # make install
  popd
  popd
}


assert_root_user() {
  # Make sure only root can run our script
  if [ "$(id -u)" != "0" ]; then
     echo "This script must be run as root" 1>&2
     exit 1
  fi
}


assert_packages_available() {
  if [ -z $1 ]; then
    echo "No packages provided to check for master template installation procedure"
    return
  fi
  local packages_should_be_available=$1
  IFS=', ' read -a debian_packages <<< "$packages_should_be_available"
  for debian_package in "${debian_packages[@]}"
  do
    echo "Checking package $debian_package"
    apt-cache show $debian_package > /dev/null 2>&1
    status=$?
    if [ $status == "0" ]; then
      echo "$debian_package exists..."
    else
      echo "Please add package $debian_package to the apt repository first!"
      exit 1
    fi
  done
}


install_latest_packages() {
  echo "installing latest debian packages"
  local packages_should_be_installed=$1
  IFS=', ' read -a debian_packages <<< "$packages_should_be_installed"
  for debian_package in "${debian_packages[@]}"
  do
    echo "Installing latest package of $debian_package"
    apt-get install --assume-yes --force-yes $debian_package
  done
}

# 1) Check if the version is changed or not. If not changed, dont create a new debian.
checkVersion $productName $mode
# 2) Get the sources which are downloaded from version control system
#    to local machine to relevant directories to generate the debian package
getSourcesToRelevantDirectories $productName
# 3) Download tar file and make necessary changes
downloadAndMakeChanges $productName
# 4) Create the Debian package
generateDebianPackage $productName
# 5) Create the Wrapper Repo Debian Package
generateRepoPackage $productName
