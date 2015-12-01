#!/usr/bin/env bash

echo "Reset all Virtual Machines and delete vagrant directory"
VBoxManage unregistervm --delete "SubutaiRH"
VBoxManage unregistervm --delete "SubutaiMHMT"
rm -r ~/vagrant