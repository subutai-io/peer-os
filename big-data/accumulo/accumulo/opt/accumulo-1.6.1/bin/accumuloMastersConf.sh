#!/bin/bash
set -e
accumuloVer="1.6.1"

function usage()
{
  echo "Example usage:"
  echo "accumuloMastersConf.sh {masters|tracers} clear           -> Clear masters or tracers file"
  echo "accumuloMastersConf.sh {masters|tracers} clear node1     -> Removes node1 from masters or tracers file" 
  echo "accumuloMastersConf.sh {masters} add node1               -> Adds node1 to masters file" 
  echo "accumuloMastersConf.sh {tracers} add node1 node2         -> Adds node1 and node2 to tracers" 
  echo "accumuloMastersConf.sh {monitor|gc} clear                -> Removes node1 from monitor or gc(Garbage Collector) file"   
  echo "accumuloMastersConf.sh {monitor|gc} add node1            -> Adds node1 to monitor or gc(Garbage Collector) file"
  exit 0
}

if [[ "$1" == "" ]];
then
  usage
fi
if [[ "$1" == "help" ]];
then
  usage
fi

mastersFile="/etc/accumulo/masters"
tracersFile="/etc/accumulo/tracers"
monitorFile="/etc/accumulo/monitor"
gcFile="/etc/accumulo/gc"

case "$1" in
  masters)
    case "$2" in
      clear)
        if [ "x$3" == "x" ];then
          > $mastersFile
          exit 0
        else
          sed -i "/$3/d" $mastersFile
          exit 0
        fi
      ;;
      add)
        for arg; do
          if [ "$arg" != "add" ] && [ "$arg" != "masters" ];then
              echo "$arg" >> $mastersFile
          fi
        done
      ;;
      *)
        usage
    esac
  ;;
  tracers)
    case "$2" in
      clear)
        if [ "x$3" == "x" ];then
          > $tracersFile
          exit 0
        else
          sed -i "/$3/d" $tracersFile
          exit 0
        fi
      ;;
      add)
        for arg; do
          if [ "$arg" != "add" ] && [ "$arg" != "tracers" ];then
              echo "$arg" >> $tracersFile
          fi
        done
      ;;
      *)
        usage
    esac
  ;;
  monitor)
    case "$2" in
      clear)
        > $monitorFile
      ;;
      add)
        echo "$3" >> $monitorFile
      ;;
      *)
        usage
    esac
  ;;
  gc)
    case "$2" in
      clear)
        > $gcFile
      ;;
      add)
        echo "$3" >> $gcFile
      ;;
      *)
        usage
    esac
  ;;
  *)
    usage
esac
