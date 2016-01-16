#!/bin/bash
username=$1
if [[ -z $username ]]; then
username="admin"
fi
ssh-keygen -f "/home/ubuntu/.ssh/known_hosts" -R [localhost]:8101
ssh -p8101 -o "StrictHostKeyChecking no" $username@localhost
