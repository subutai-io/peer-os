#!/usr/bin/env bash

pushd ~/vagrant/resource-host
vagrant halt -f
vagrant destroy
popd
pushd ~/vagrant/management-host
vagrant halt -f
vagrant destroy
popd
echo "Reset all Virtual Machines and delete vagrant directory"
