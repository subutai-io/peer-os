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
echo "Reset all Virtual Machines and delete vagrant directory"
