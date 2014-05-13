#!/bin/bash

set -e
wget http://archive.apache.org/dist/cassandra/2.0.4/apache-cassandra-2.0.4-bin.tar.gz

tar -xvpf *.tar.gz
rm -rf *.tar.gz
rm -rf ksks-cassandra*/opt/*

cp -r apache-cassandra* ksks-cassandra*/opt/
cd ksks-cassandra*/opt/
mv apache-cassandra* cassandra-2.0.4
cd -
rm -rf apache-cassand*

cp ../workspace/big-data/cassandra/cassandra/cassandra/DEBIAN/*  ksks-cassandra*/DEBIAN
cp -r ../workspace/big-data/cassandra/cassandra/cassandra/etc/* ksks-cassandra*/etc

cp ../workspace/big-data/cassandra/cassandra/cassandra/opt/cassandra-2.0.4/bin/* ksks-cassandra*/opt/cassandra-2.0.4/bin/
cp ../workspace/big-data/cassandra/cassandra/cassandra/opt/cassandra-2.0.4/conf/* ksks-cassandra*/opt/cassandra-2.0.4/conf/
cp ../workspace/big-data/cassandra/cassandra/cassandra/opt/cassandra-2.0.4/ksks-Changes.txt ksks-cassandra*/opt/cassandra-2.0.4/
cd ksks-cassandra*/opt/cassandra-2.0.4
mkdir  logs
cd -

./pack.sh
cp *.deb /var/lib/jenkins/Automation/Bigdata/cassandra
