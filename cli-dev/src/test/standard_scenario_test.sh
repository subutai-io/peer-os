#!/bin/bash
#
# Description: This is a sample test using shunit2
#
subutai_lib_base=/usr/share/subutai-cli/subutai/lib
subutai_conf_base=/etc/subutai
. $subutai_lib_base/funcs

function rename_check()
{
  (lxc-info -n $1 \
  || zfs list | grep $1-var \
  || zfs list | grep $1-opt \
  || zfs list | grep $1-home \
  || lxc-ls | grep $1)  > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 1 ]
  then
      (lxc-info -n $2 \
      && zfs list | grep $2-var \
      && zfs list | grep $2-opt \
      && zfs list | grep $2-home) > /dev/null 2>&1
      returnCode=$?
  fi
  echo $returnCode
}


test_apt_master()
{
  response=$(apt-get --assume-yes --force-yes install master-subutai-template)
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    zfs list -t snapshot | grep master@template \
    && zfs list -t snapshot | grep master-var@template \
    && zfs list -t snapshot | grep master-opt@template \
    && zfs list -t snapshot | grep master-home@template \
    && ls /var/lib/lxc/master/rootfs
    returnCode=$?
  fi
  assertEquals "This is the message if apt-get master fails" 0 $returnCode
}


test_clone_test()
{
  response=$(subutai clone master test)
  (echo "$response" \
    | grep "Successfully cloned test container from master") > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    (zfs list | grep test \
    && zfs list | grep test-var \
    && zfs list | grep test-opt \
    && zfs list | grep test-home \
    && ls /var/lib/lxc/test/rootfs \
    && lxc-ls -f| grep test | grep RUNNING \
    && cmac="`cat /var/lib/lxc/test/config | grep hwaddr | cut -d ' ' -f3 `" \
    && pmac="`cat /var/lib/lxc/master/config | grep hwaddr | cut -d ' ' -f3`" \
    && [ "$cmac" != "$pmac" ] \
    && lxc-stop -n test \
    && lxc-ls -f| grep test | grep STOPPED) > /dev/null 2>&1
    returnCode=$?
  fi
  assertEquals "This is the message if clone fails" 0 $returnCode
}


test_subutai_promote()
{
  (response=$(subutai promote test) \
           && echo "$response"| grep "commit UUID extracted") > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
       zfs list -t snapshot | grep test@template \
       && zfs list -t snapshot | grep test-var@template \
       && zfs list -t snapshot | grep test-opt@template \
       && zfs list -t snapshot | grep test-home@template \
       && ls /var/lib/lxc/test/rootfs
       returnCode=$?
  fi
  assertEquals "This is the message if promote fails" 0 $returnCode
}


test_subutai_demote()
{
  (response=$(subutai demote test) \
           && (echo "$response" \
           | grep "Demoting template \"test\" with no children.")) > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
       (zfs list -t snapshot | grep test@template \
       || zfs list -t snapshot | grep test-var@template \
       || zfs list -t snapshot | grep test-opt@template \
       || zfs list -t snapshot | grep test-home@template) > /dev/null
       returnCode=$?
  fi
  assertEquals "This is the message if demote fails" 1 $returnCode
}


test_subutai_promote()
{
  (response=$(subutai promote test) \
           && echo "$response"| grep "commit UUID extracted") > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
       zfs list -t snapshot | grep test@template \
       && zfs list -t snapshot | grep test-var@template \
       && zfs list -t snapshot | grep test-opt@template \
       && zfs list -t snapshot | grep test-home@template \
       && ls /var/lib/lxc/test/rootfs
       returnCode=$?
  fi
  assertEquals "This is the message if promote fails" 0 $returnCode
}


test_subutai_export()
{
  (response=$(subutai export test) \
           && (echo "$response" | grep "Generated debian package")) > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
       (ls /lxc-data/tmpdir/test-subutai-template*) > /dev/null
       returnCode=$?
  fi
  if [ $returnCode != 0 ]
  then
    assertEquals "This is the message if export test fails" 0 $returnCode
    return $SHUNIT_ERROR
  fi
}


test_register_test()
{
  response=$(subutai register test)
  returnCode=$?
  if [ $returnCode == 0 ]
  then
     resp=$(curl -s -XGET http://gw.intra.lan:8181/cxf/registry/get_template/test)
    if [[ -n "$resp" && "$resp" == *\"test\"* ]]; then
      returnCode=0
    else
      returnCode=1
    fi
  fi
  assertEquals "This is the message if register fails" 0 $returnCode
}

test_subutai_demote_master()
{
  (response=$(subutai demote master)) > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
       (zfs list -t snapshot | grep master@template \
       || zfs list -t snapshot | grep master-var@template \
       || zfs list -t snapshot | grep master-opt@template \
       || zfs list -t snapshot | grep master-home@template) > /dev/null
       returnCode=$?
  fi
  assertEquals "This is the message if master demoted" 1 $returnCode
}


test_subutai_destroy()
{
  (response=$(subutai destroy test) && (echo "$response" \
            | grep "Destruction of \"test\" completed successfully")) > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
       (zfs list | grep test \
       || zfs list | grep test-var \
       || zfs list | grep test-opt \
       || zfs list | grep test-home \
       || lxc-ls | grep test \
       || ls /var/lib/lxc/test/)  > /dev/null 2>&1
       returnCode=$?
  fi
  assertEquals "This is the message if test destroy fails" 2 $returnCode
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
}


test_subutai_import()
{
  (response=$(subutai master_import) && (echo "$response" \
   | grep "master-subutai-template is installed succesfully")) > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
       (zfs list -t snapshot | grep master@template \
       && zfs list -t snapshot | grep master-var@template \
       && zfs list -t snapshot | grep master-opt@template \
       && zfs list -t snapshot | grep master-home@template \
       && ls /var/lib/lxc/master/rootfs)  > /dev/null
       returnCode=$?
  fi
  assertEquals "This is the message if import master_create fails" 0 $returnCode
  (response=$(subutai import test) && (echo "$response" \
     | grep "Debian package of test-subutai-template is installed succesfully")) > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
       (zfs list -t snapshot | grep test@template \
       && zfs list -t snapshot | grep test-var@template \
       && zfs list -t snapshot | grep test-opt@template \
       && zfs list -t snapshot | grep test-home@template \
       && ls /var/lib/lxc/test/rootfs)  > /dev/null
       returnCode=$?
  fi
  assertEquals "This is the message if import test fails" 0 $returnCode
}


test_subutai_list()
{
  response=$(subutai list | tail -n +3 | grep -v "-")
  returnCode=$?
  if [ $returnCode == 0 ]
  then
       [ "$response" == "`ls -l /var/lib/lxc/ | tail -n +2 | cut -d ' ' -f10`" ]
       returnCode=$?
  fi
  assertEquals "This is the message if subutai list fails" 0 $returnCode
}


test_subutai_rename_fail()
{
  response=$(subutai rename master master1)
  returnCode=$?
  if [ $returnCode == 1 ]
  then
       echo "$response" | grep "Awe dude come on. Lay off the master"
       returnCode2=$?
  fi
  assertEquals "This is the message if rename_fail master fails" 0 $returnCode2

  response=$(subutai rename test test1)
  returnCode=$?
  if [ $returnCode == 1 ]
  then
       echo "$response" | grep "Container \"test\" is a template. Aborting ..."
       returnCode2=$?
  fi
  assertEquals "This is the message if rename_fail template fails" 0 $returnCode2
}


test_clone_foo()
{
  response=$(subutai clone master foo)
  (echo "$response" \
    | grep "Successfully cloned foo container from master") > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    (zfs list | grep foo \
    && zfs list | grep foo-var \
    && zfs list | grep foo-opt \
    && zfs list | grep foo-home \
    && ls /var/lib/lxc/foo/rootfs \
    && lxc-ls -f| grep foo | grep RUNNING \
    && cmac="`cat /var/lib/lxc/foo/config | grep hwaddr | cut -d ' ' -f3 `" \
    && pmac="`cat /var/lib/lxc/master/config | grep hwaddr | cut -d ' ' -f3`" \
    && [ "$cmac" != "$pmac" ] \
    && lxc-stop -n foo \
    && lxc-ls -f| grep foo | grep STOPPED) > /dev/null 2>&1
    returnCode=$?
  fi
  assertEquals "This is the message if foo clone fails" 0 $returnCode
}


test_subutai_rename_pass()
{
  response1=$(subutai rename foo foo1 | grep ip | awk -F " = " '{print $2}')
  returnCode=$?
  if [ $returnCode == 0 ]
  then
       returnCode=$(rename_check foo foo1)
  fi
  if [ $returnCode == "0" ]
  then
       response2=$(subutai rename foo1 foo | grep ip \
                     | awk -F " = " '{print $2}')
       returnCode=$?
       [ "$returnCode" == "0" ] && returnCode=$(rename_check foo1 foo)
  fi
  if [ [$returnCode != 0 ] || [ "$response1" != "$response2" ] ]; then
     assertEquals "This is the message if rename_pass fails" 0 $returnCode
  fi
}


test_subutai_destroy()
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
       || ls /var/lib/lxc/master/ \
       || zfs list | grep test \
       || zfs list | grep test-var \
       || zfs list | grep test-opt \
       || zfs list | grep test-home \
       || lxc-ls | grep test \
       || ls /var/lib/lxc/test/)  > /dev/null
       returnCode=$?
  fi
  assertEquals "This is the message if master_destroy fails" 2 $returnCode
  (dpkg -s master-subutai-template) > /dev/null 2>&1
  returnCode=$?
  assertEquals "This is the message if dpkg master query fail" 1 $returnCode
  (dpkg -s test-subutai-template) > /dev/null 2>&1
  returnCode=$?
  assertEquals "This is the message if dpkg test query fail" 1 $returnCode
}


. shunit2

