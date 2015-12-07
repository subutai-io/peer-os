#!/usr/bin/env bash

pushd ~/vagrant/resource-host
vagrant halt -f
vagrant destroy -f
popd
pushd ~/vagrant/management-host
vagrant halt -f
vagrant destroy -f
popd

rm -rf ~/vagrant
rm -rf .vagrant.d/boxes/subutai-VAGRANTSLASH-management-host/
rm -rf .vagrant.d/boxes/subutai-VAGRANTSLASH-resource-host/
rm -rf .vagrant.d/tmp/*

echo "Reset all Virtual Machines and delete vagrant directory"
