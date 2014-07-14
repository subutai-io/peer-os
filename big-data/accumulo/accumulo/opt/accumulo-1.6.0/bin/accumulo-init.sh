#!/usr/bin/expect -f

# get instanceName and password from user
set instanceName [lindex $argv 1];
set pass [lindex $argv 2];

# invoke accumulo init command 
spawn accumulo init
expect "Instance name :"
send "$instanceName\r"
expect "Enter initial password for root:"
send "$pass\r"
expect "Confirm initial password for root:"
send "$pass\r"
expect "$ "
