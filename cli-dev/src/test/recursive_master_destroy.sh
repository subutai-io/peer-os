#!/bin/bash
#
# Description: This is a sample test using shunit2
#

subutai_lib_base=/usr/share/subutai-cli/subutai/lib
subutai_conf_base=/etc/subutai
. $subutai_lib_base/funcs
oneTimeSetUp()
{
 (lxc-ls | grep master) > /dev/null 2>&1
 if [ $? != 0 ]
 then
 subutai master_import
 fi

 subutai clone master test2
 subutai clone master foo2
 subutai clone master bar2
 subutai clone master go2

 subutai promote foo2
 subutai promote bar2

 subutai clone foo2 foo3
 subutai clone bar2 bar3

 subutai promote foo3

 subutai clone foo3 foo4
 subutai promote foo4
 subutai clone foo4 foo5
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

