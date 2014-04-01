#!/bin/bash
set -e

case "$1" in
start)
        sh /opt/zookeeper-3.4.5/bin/zkServer.sh start
;;
stop)
        sh /opt/zookeeper-3.4.5/bin/zkServer.sh stop
;;
*)
        echo "Usage: $0 {start|stop}"
        exit 1
esac
