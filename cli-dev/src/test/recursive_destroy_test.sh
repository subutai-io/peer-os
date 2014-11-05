#!/bin/bash
#
# Description: This is a sample test using shunit2
#
subutai_lib_base=/usr/share/subutai-cli/subutai/lib
subutai_conf_base=/etc/subutai
. $subutai_lib_base/funcs


test_generate()
{
  subutai master_import

  subutai clone master test1
  subutai clone master foo1
  subutai clone master bar1
  subutai clone master go1

  subutai promote test1
  subutai promote bar1
  subutai promote go1

  subutai clone bar1 bar2
  subutai clone test1 test2

  subutai promote test2
  subutai clone test2 test3

  subutai clone go1 go2
  subutai clone go1 go3
}


test_destroy_bar()
{
  subutai destroy -r bar1 > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
       (zfs list | grep bar1 \
       || zfs list | grep bar1-var \
       || zfs list | grep bar1-opt \
       || zfs list | grep bar1-home \
       || lxc-ls | grep bar1 \
       || ls /var/lib/lxc/bar1/ \
       || zfs list | grep bar2 \
       || zfs list | grep bar2-var \
       || zfs list | grep bar2-opt \
       || zfs list | grep bar2-home \
       || lxc-ls | grep bar2 \
       || ls /var/lib/lxc/bar2/)  > /dev/null 2>&1
       returnCode=$?
  fi
  assertEquals "This is the message if test destroy fails" 2 $returnCode
}


test_destroy_master()
{
  (echo -e "o\nY\n" | subutai master_destroy) > /dev/null
  returnCode=$?
  if [ $returnCode == 0 ]
  then
       (zfs list | grep master \
       || zfs list | grep master-var \
       || zfs list | grep master-opt \
       || zfs list | grep master-home \
       || lxc-ls | grep master \
       || ls /var/lib/lxc/master/)  > /dev/null
       returnCode=$?
  fi
  assertEquals "This is the message if master_destroy fails" 2 $returnCode
  (dpkg -s master-subutai-template) > /dev/null 2>&1
  returnCode=$?
  assertEquals "This is the message if dpkg master query fail" 1 $returnCode
}


. shunit2
