#!/bin/bash
#
# Description: This is a sample test using shunit2
#
subutai_lib_base=/usr/share/subutai-cli/subutai/lib
subutai_conf_base=/etc/subutai
. $subutai_lib_base/funcs

test_system_lock()
{
  lock_subutai_system > /dev/null 2>&1
  ls /var/lock/subutai/system_lock > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode != 0 ]
  then
    assertEquals "System is not locked!" 0 $returnCode
  fi
  if [ "`is_system_locked`" != "true" ]
  then
    assertEquals "is_system_locked function false but system is locked!" 0 1
  fi
  (lxc-ls | grep master) > /dev/null 2>&1
  if [ $? != 0 ]
  then
  subutai master_import
  fi
  subutai clone master test > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "System is locked but clone is able to work!" 1 $returnCode
  fi
  subutai promote test > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "System is locked but promote is able to work!" 1 $returnCode
  fi
  subutai demote test > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "System is locked but demote is able to work!" 1 $returnCode
  fi
  subutai rename test test1 > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "System is locked but rename is able to work!" 1 $returnCode
  fi
  subutai import test > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "System is locked but import is able to work!" 1 $returnCode
  fi
  subutai export test > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "System is locked but export is able to work!" 1 $returnCode
  fi
  subutai register test > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "System is locked but register is able to work!" 1 $returnCode
  fi
  unlock_subutai_system > /dev/null 2>&1
  ls /var/lock/subutai/system_lock > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "System not be able to unlocked!" 1 $returnCode
  fi
  if [ "`is_system_locked`" == "true" ]
  then
    assertEquals "is_system_locked function true but system is unlocked!" 0 1
  fi
}


test_write_lock()
{
  lock_container_write test
  ls /var/lock/subutai/write/test- > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode != 0 ]
  then
    assertEquals "Test Container not be able to locked!" 0 $returnCode
  fi
  subutai promote test > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "Test Container is locked by write but promote is able to work!" 1 $returnCode
  fi
  subutai demote test > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "Test Container is locked by write but demote is able to work!" 1 $returnCode
  fi
  subutai rename test test1 > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "Test Container is locked by write but rename is able to work!" 1 $returnCode
  fi
  subutai destroy test > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "Test Container is locked by write but destroy is able to work!" 1 $returnCode
  fi
  unlock_container_write test > /dev/null 2>&1
  subutai clone master test > /dev/null 2>&1
  subutai promote test > /dev/null 2>&1
  subutai export test > /dev/null 2>&1
  subutai destroy test1 > /dev/null 2>&1
  subutai destroy test > /dev/null 2>&1
  lock_container_write test > /dev/null 2>&1
  subutai import test > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "Test Container is locked by write but import is able to work!" 1 $returnCode
  fi
  unlock_container_write test
  ls /var/lock/subutai/write/test- > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "Test Container not be able to unlocked!" 1 $returnCode
  fi
}


test_read_lock()
{
  subutai destroy test > /dev/null 2>&1
  subutai clone master test > /dev/null 2>&1
  subutai promote test > /dev/null 2>&1
  lock_container_read test > /dev/null 2>&1
  ls /var/lock/subutai/read/test- > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode != 0 ]
  then
    assertEquals "Test Container not be able to locked!" 0 $returnCode
  fi
  subutai clone test test1 #> /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "Test Container is locked by read but clone is able to work!" 1 $returnCode
  fi
  rm /lxc-data/tmpdir/test*
  subutai export test > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "Test Container is locked by read but export is able to work!" 1 $returnCode
  fi
  subutai register test > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "Test Container is locked by read but register is able to work!" 1 $returnCode
  fi
  subutai destroy test > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "Test Container is locked by read but destroy is able to work!" 1 $returnCode
  fi
  subutai demote test > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "Test Container is locked by read but demote is able to work!" 1 $returnCode
  fi
  subutai promote test > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "Test Container is locked by read but promote is able to work!" 1 $returnCode
  fi
  unlock_container_read test
  ls /var/lock/subutai/read/test- > /dev/null 2>&1
  returnCode=$?
  if [ $returnCode == 0 ]
  then
    assertEquals "Test Container not be able to unlocked!" 1 $returnCode
  fi
  subutai destroy test > /dev/null 2>&1
  rm /lxc-data/tmpdir/test* > /dev/null 2>&1
}


. shunit2
