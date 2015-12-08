#!/usr/bin/env bash

echo
echo "1. Create folder with command: "
echo
mkdir -p ~/vagrant/resource-host/
echo
echo "and go to this directory: "
cd ~/vagrant/resource-host/

echo
echo "2. Create file named \"Vagrantfile\":"
echo

touch Vagrantfile
cat ~/playbooks-newui/src/test/resources/files/VagrantFileRH > ~/vagrant/resource-host/Vagrantfile

echo
echo "3. Start your Resource Host box: vagrant up"
echo
vagrant up
VBoxManage controlvm SubutaiRH poweroff
VBoxManage modifyvm SubutaiRH --natdnshostresolver1 on
VBoxManage startvm SubutaiRH --type headless
sleep 20

echo
echo "4. Now import master template to offline resource host with command: vagrant exec sudo subutai import master"
echo
vagrant exec sudo subutai import master

echo
echo "5. Create container host from master template: vagrant exec sudo subutai clone master container1"
echo
vagrant exec sudo subutai clone master container1

echo
echo "6. Now try to create simple sand-boxed environment: vagrant exec sudo subutai clone master container2"
echo
vagrant exec sudo subutai clone master container2

echo
echo "7. Run list command to see containers information: vaghow to use wait in bashrant exec sudo subutai list -i"
echo
vagrant exec sudo subutai list -i

ipContainer1=`vagrant exec sudo subutai list -i | grep container1 | awk '{print $4}'`

echo
echo "8. Now ping first container by logging into second container: vagrant exec sudo lxc-attach -n container2 -- timeout 3 ping <container1 IP>"

vagrant exec sudo lxc-attach -n container2 -- timeout 3 ping ${ipContainer1}

echo
echo "   Destroy created containers: vagrant exec sudo subutai destroy container1 / container2"

vagrant exec sudo subutai destroy container1
vagrant exec sudo subutai destroy container2

echo
echo "9. Create directory for management host: mkdir ~/vagrant/management-host"

mkdir ~/vagrant/management-host
echo
echo "and go there: cd ~/vagrant/management-host"
cd ~/vagrant/management-host

echo
echo "10. Create file named \"Vagrantfile\": touch Vagrantfile"
touch Vagrantfile
cat ~/playbooks-newui/src/test/resources/files/VagrantFileMH > ~/vagrant/management-host/Vagrantfile

echo
echo "Start your Management Host box with: vagrant up"

vagrant up

echo
echo "12. Edit management VM network settings:
VBoxManage controlvm SubutaiMGMT poweroff
VBoxManage modifyvm SubutaiMGMT --nic2 intnet --intnet2 intnetSnappy1  --nicpromisc2 allow-all --macaddress2 auto
VBoxManage modifyvm SubutaiMGMT --natpf1 subutai,tcp,127.0.0.1,8888,,8443
VBoxManage modifyvm SubutaiMGMT --natpf1 karaf,tcp,127.0.0.1,8888,,8101
VBoxManage startvm SubutaiMGMT --type headless"
echo

VBoxManage controlvm SubutaiMGMT poweroff
VBoxManage modifyvm SubutaiMGMT --natdnshostresolver1 on
VBoxManage modifyvm SubutaiMGMT --nic2 intnet --intnet2 intnetSnappy1  --nicpromisc2 allow-all --macaddress2 auto
VBoxManage modifyvm SubutaiMGMT --natpf1 subutai,tcp,127.0.0.1,8888,,8443
VBoxManage modifyvm SubutaiMGMT --natpf1 karaf,tcp,127.0.0.1,8889,,8101
VBoxManage startvm SubutaiMGMT --type headless
sleep 20

echo
echo "13. Edit management VM network settings:
VBoxManage controlvm SubutaiRH poweroff
VBoxManage modifyvm SubutaiRH --nic1 intnet --intnet1 intnetSnappy1  --nicpromisc1 allow-all --macaddress1 auto
VBoxManage startvm SubutaiRH --type  headless"
echo

VBoxManage controlvm SubutaiRH poweroff
VBoxManage modifyvm SubutaiRH --nic1 intnet --intnet1 intnetSnappy1  --nicpromisc1 allow-all --macaddress1 auto
VBoxManage startvm SubutaiRH --type  headless

sleep 120

echo "14. Login in karaf and approve requests: "

~/playbooks-newui/node-approve 127.0.0.1 8888

echo "Continue run test on the Web UI of Management Host ... "
