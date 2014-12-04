#!/bin/bash

main() {
zfs_user=$1
zfs_ip_address=$2
base_template_name=$3
target_template_name=$4
list_of_packages_to_be_installed_on_target_container=$5
version=$6

# Variables
remote_machine=$zfs_user@$zfs_ip_address
directory_to_copy_package="/var/lib/jenkins/Automation/Automation_ISO/Packages/$target_template_name-subutai-template/"

assert_passwordless_ssh_to_remote
create_template_on_remote
get_template_package_from_remote

}


assert_passwordless_ssh_to_remote() {
  ssh -o BatchMode=yes $remote_machine true
  local status=$?
  if [ $status == "0" ]; then
    echo "Jenkins zfs is able to access to jenkins without password"
    return
  else
    echo "Make sure user(`whoami`) on this machine can access to zfs machine($remote_machine) without password"
    exit 1
  fi
}


create_template_on_remote() {
  ssh $remote_machine /bin/bash << EOF
  # Become superuser to be able to run commands that require sudo permissions
  # TODO make sure you added NOPASSWD to sudo on remote machine to disable prompt while becoming sudo
  sudo su
  . /home/$zfs_user/jenkins/scripts/create_template_package.sh $base_template_name $target_template_name $list_of_packages_to_be_installed_on_target_container $version
EOF

}

get_template_package_from_remote() {
  rm -f $directory_to_copy_package/$target_template_name*_"$version"_*.deb
  mkdir -p $directory_to_copy_package
  pushd $directory_to_copy_package > /dev/null

  sftp $remote_machine << EOF
  get /lxc-data/tmpdir/$target_template_name-*_"$version"_*.deb .
  bye
EOF

  if [ ! -f $directory_to_copy_package/$target_template_name*_"$version"_*.deb ]; then
    echo "Could not find $target_template_name package with version $version under $directory_to_copy_package. Aborting!"
    exit 1
  fi
  popd
  echo "Debian package of $target_template_name template with version $version is downloaded under $directory_to_copy_package"
}

main "$@"
