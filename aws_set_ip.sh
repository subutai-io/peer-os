#!/usr/bin/env bash

ip1=$1;
ip2=$2;

echo "${ip1}" > ~/playbooks-newui/src/test/resources/parameters/awsmh1_IP
echo "${ip2}" > ~/playbooks-newui/src/test/resources/parameters/awsmh2_IP

~/playbooks-newui/node-approve ${ip1}
~/playbooks-newui/node-approve ${ip2}