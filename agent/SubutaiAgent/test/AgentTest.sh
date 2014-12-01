#!/bin/bash
#copying the all cpp and source files from main repository
BASE=$(pwd)
cd $1
SOURCE=$(pwd)
echo $BASE
echo $SOURCE

cd $BASE
rm *.xml
rm $BASE/bin/*
rm $BASE/debug/*

cp $SOURCE/agent/SubutaiAgent/src/* $BASE/src/ 
cp $SOURCE/agent/SubutaiAgent/test/* $BASE/src/ 

echo "copying source files.."
echo "starting source compilation"

#Starting generation of Object Files
g++ -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I/usr/include/c++/4.6/backward -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/SubutaiException.cpp

g++ -I/usr/include  -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I/usr/include/c++/4.6/backward -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/SubutaiAgentTest.cpp 

g++ -I/usr/include  -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I/usr/include/c++/4.6/backward -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/SubutaiEnvironment.cpp 

g++ -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I/usr/include/c++/4.6/backward  -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/SubutaiResponse.cpp 

g++ -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I/usr/include/c++/4.6/backward  -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/SubutaiContainer.cpp 

g++ -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I/usr/include/c++/4.6/backward  -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/SubutaiContainerManager.cpp 

g++ -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I/usr/include/c++/4.6/backward  -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/SubutaiHelper.cpp 

g++ -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I/usr/include/c++/4.6/backward  -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/SubutaiTimer.cpp 

g++ -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I/usr/include/c++/4.6/backward  -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/Message.pb.cpp 

g++ -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I/usr/include/c++/4.6/backward -ffunction-sections -fdata-sections -g3 -Wall -c  $BASE/src/SubutaiCommand.cpp
 
g++ -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I$BASE/src/ -I/usr/include/c++/4.6/backward -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/SubutaiConnection.cpp

g++ -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I/usr/include/c++/4.6/backward -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/SubutaiLogger.cpp

g++ -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I/usr/include/c++/4.6/backward -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/SubutaiResponsePack.cpp

g++ -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I/usr/include/c++/4.6/backward -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/SubutaiStreamReader.cpp 

g++ -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I/usr/include/c++/4.6/backward -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/SubutaiThread.cpp

g++ -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I/usr/include/c++/4.6/backward -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/SubutaiUserID.cpp

g++ -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib/cpp -I/usr/include/c++/4.6/backward  -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/mosquittopp.cpp

g++ -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib/cpp -I/$BASE/src -I/usr/include/c++/4.6/backward  -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/pugixml.cpp

g++ -I/usr/include -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include-fixed -I/usr/lib/gcc/x86_64-linux-gnu/4.6/include -I/usr/include/x86_64-linux-gnu -I/usr/local/include -I/usr/include/c++ -I/usr/include/c++/4.6 -I/var/lib/jenkins//AgentInstallation/mosquitto-1.3.1/lib -I/usr/include/c++/4.6/backward -ffunction-sections -fdata-sections -g3 -Wall -c $BASE/src/SubutaiWatch.cpp


echo "moving object files"
mv *.o $BASE/debug

#Generation of the TestAgent executable
g++ -L/lib/x86_64-linux-gnu -L"$BASE/libs" -o "SubutaiAgentTest"   $BASE/debug/SubutaiCommand.o $BASE/debug/SubutaiConnection.o $BASE/debug/SubutaiContainer.o $BASE/debug/SubutaiEnvironment.o $BASE/debug/SubutaiContainerManager.o $BASE/debug/SubutaiHelper.o $BASE/debug/SubutaiTimer.o $BASE/debug/SubutaiLogger.o $BASE/debug/SubutaiResponse.o $BASE/debug/SubutaiResponsePack.o $BASE/debug/SubutaiStreamReader.o $BASE/debug/SubutaiThread.o $BASE/debug/SubutaiUserID.o $BASE/debug/mosquittopp.o $BASE/debug/pugixml.o $BASE/debug/SubutaiAgentTest.o $BASE/debug/SubutaiWatch.o $BASE/debug/SubutaiException.o -lcppunit -ljson_linux-gcc-4.6_libmt -lmosquitto -lcrypto -lmosquittopp -lssl -lpthread -lboost_thread -lcares -lrt -llxc -lprotobuf -lboost_system
echo "finishing AgentTest Executables"
strip -s SubutaiAgentTest
mv SubutaiAgentTest $BASE/bin
echo "starting AgentTest.."
sudo $BASE/bin/SubutaiAgentTest
rm $SOURCE/AgentUnitTestReport.xml
cp $BASE/AgentUnitTestReport.xml $BASE/../../workspace/

