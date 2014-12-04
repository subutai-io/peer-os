#!/bin/bash

main() {
  # Environment specific variables
  #bridge_name=eth0
  jenkins_user="$1"
  jenkins_ip_address="$2"

  base_template_name=$3
  target_template_name=$4
  list_of_packages_to_be_installated_on_target_template=$5
  version=$6

  echo "Parameters: $@"
  export SUBUTAI_OFFLINE_MODE=true  

  # General Variables 
  # These packages should have the latest versions on this machine
  local packages_should_be_installed="subutai-cli,subutai-cli-dev,git,lxc,expect"
  # These packages should be available to be installed on master template
  local packages_should_be_available="$list_of_packages_to_be_installated_on_target_template"

  jenkins_machine=$jenkins_user@$jenkins_ip_address

  if [[ $2 == "" ]]; then
    usage
  fi

  assert_template_exists $base_template_name
  assert_root_user
  apt-get update
  assert_packages_available $packages_should_be_installed
  assert_packages_available $packages_should_be_available
  install_latest_packages
  destroy_template
  clone_container
  install_packages_to_containers
  promote_container
  export_template $version
}


usage() {
  echo "arg1 : jenkins user"
  echo "arg2 : jenkins ip address"
  echo "arg3 : template name which will be used as base during clone operation"
  echo "arg4 : template name which will be created"
  echo "arg5 : list of packages to be installed on target container"
  echo "arg6 : version of the template package"
  echo "Usage: $0 arg1 arg2 arg3 arg4 arg5 arg6"
  echo "Ex: $0 jenkins 172.16.9.15 master cassandra \"subutai-cassandra,openjdk-7-jre,expect\" 2.1.3"
  exit 1
}

# checks if template which will be used as base exists.
assert_template_exists(){
  result=`subutai -q list -t $1`
  if [[ "$result" == *not* ]]; then
     echo "There is no template with name \"$1\""
     exit;
  fi
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
    echo "Please provide the package list to assert_packages_available method!"
    exit 1
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
  IFS=', ' read -a debian_packages <<< "$packages_should_be_installed"
  for debian_package in "${debian_packages[@]}"
  do
    echo "Installing latest package of $debian_package"
    apt-get install --assume-yes --force-yes $debian_package
  done
}


# install bigdata package to container which will be a new template
install_packages_to_containers() {
  # without waiting network, getting errors during apt-get update and install commands
  lxc_wait_net $target_template_name 10
  lxc-attach -n $target_template_name -- apt-get update
  echo "Installing packages to container"
  IFS=', ' read -a debian_packages <<< "$list_of_packages_to_be_installated_on_target_template"
  for debian_package in "${debian_packages[@]}"
  do
    echo "Installing latest package of $debian_package"
    lxc-attach -n $target_template_name -- apt-get install --force-yes --assume-yes --force-yes $debian_package
  done
}


destroy_template() {
  echo "destroying $target_template_name template"
  subutai -q destroy $target_template_name
  rm -rf /lxc-data/tmpdir/$target_template_name*_"$version"_*.deb
}


clone_container() {
  echo "creating $target_template_name template"
  subutai -q clone $base_template_name $target_template_name
}


promote_container(){
  echo "promoting $target_template_name container"
  subutai -q promote $target_template_name
}


export_template() {
  version=$1
  echo "exporting $target_template_name template's debian package with version $version"
  subutai -q export $target_template_name -v $version
  echo "Size of $target_template_name template package is:" `du -hs /lxc-data/tmpdir/$target_template_name*_"$version"_*.deb`
}

# waits until the networking comes up on a container (arg $1)
# if container is off starts it up and waits
# sleeps for maximum of (arg $2) seconds: defaults to 10 seconds
lxc_wait_net() {
  local lxc="$1"
  lxc_assert_name "$lxc"

  local waitmax="$2"

  lxc-wait -n $lxc -s 'RUNNING' -t 5

  if [ -z "$waitmax" ]; then
    waitmax=10
  fi

  while [ $waitmax -gt 0 ]; do
    sleep 1
    ipv4=`lxc-ls -f | grep $lxc | awk '{print $3}'`

    if [ "$ipv4" != "-" ]; then
      echo "    - container \"$lxc\" online with ip = $ipv4"
      break
    fi

    waitmax=`expr $waitmax - 1`
  done
}

lxc_assert_name() {
  local lxc="$1"
  if [ -n "`echo $lxc | grep '-'`" ]; then
    msg_error "Arg \"$lxc\" contains illegal dash character."
    unlock_container_read $1
    exit 1
  fi
}

main "$@"
exit 0
