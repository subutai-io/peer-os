#!/usr/bin/env bash

mkdir -p ~/vagrant/resource-host/
cd ~/vagrant/resource-host/

touch Vagrantfile
cat ~/subutai/playbooks-newui/src/test/resources/files/VagrantFileRH > ~/vagrant/resource-host/Vagrantfile

vagrant up

vagrant exec sudo subutai import master
vagrant exec sudo subutai clone master container1
vagrant exec sudo subutai clone master container2
vagrant exec sudo subutai list -i

ipContainer1=`vagrant exec sudo subutai list -i | grep container1 | awk '{print $4}'`

vagrant exec sudo lxc-attach -n container2 -- timeout 3 ping ${ipContainer1}

vagrant exec sudo subutai destroy container1
vagrant exec sudo subutai destroy container2

mkdir ~/vagrant/management-host
cd ~/vagrant/management-host

touch Vagrantfile
cat ~/subutai/playbooks-newui/src/test/resources/files/VagrantFileMH > ~/vagrant/management-host/Vagrantfile

vagrant up

VBoxManage controlvm SubutaiMGMT poweroff

VBoxManage modifyvm SubutaiMGMT --nic2 intnet --intnet2 intnetSnappy1  --nicpromisc2 allow-all --macaddress2 auto

VBoxManage modifyvm SubutaiMGMT --nic1 bridged --bridgeadapter1 <interface to bridge> --nicpromisc1 allow-all --macaddress1 auto

VBoxManage modifyvm SubutaiMGMT --vrde on --vrdeport 3490

VBoxManage startvm SubutaiMGMT --type headless

VBoxManage controlvm SubutaiRH poweroff

VBoxManage modifyvm SubutaiRH --nic1 intnet --intnet1 intnetSnappy1  --nicpromisc1 allow-all --macaddress1 auto

VBoxManage modifyvm SubutaiRH --vrde on --vrdeport 3491

VBoxManage startvm SubutaiRH --type  headless