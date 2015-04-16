#!/bin/bash
set -e
echo "Running pre-promote-cleanup.sh"
rm -rf /var/log/*
echo "pre-promote-cleanup.sh script run succesfully" > /opt/pre-promote-output.txt
