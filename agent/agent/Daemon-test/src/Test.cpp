/*
 * Test.cpp
 *
 *  Created on: Sep 9, 2013
 *      Author: Emin inal
 */
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <syslog.h>
#include <stdarg.h>
#include <iostream>

#include "cute.h"
#include "ide_listener.h"
#include "cute_runner.h"
#include "DaemonConnection.h"
#include "responsePack.h"
#include "Command.h"
#include "ThreadOperation.h"
#include "UserID.h"

void DaemonCommunicationTest() {
	string url =  "172.16.33.7:5672";
	string connectionOptions = "{reconnect:false}";
	bool state = false;
	DaemonConnection connection(url, connectionOptions);

	try{
		connection.open();
		state=true;
	}
	catch(const std::exception& error)
	{
		connection.close();
		state=false;
	}
	ASSERT_EQUAL(true,state);
	connection.close();
}
void getMacIDTest(){
	responsePack response;
	string mac = response.getMacID();
	ASSERTM("MAC ID Could not be read !!", ! mac.empty());
}
int DaemonSendMessageTest() {
	string url =  "172.16.33.7:5672";
	string connectionOptions = "{reconnect:false}";
	DaemonConnection connection(url, connectionOptions);
	responsePack response;

	if(! connection.openMySession(response.getMacID())){ //connection is closed
		ASSERTM("Connection is closed", connection.openMySession(response.getMacID()));
		return 100;
	}
	try{
		connection.sendMessage("SENDTESTING");
		Receiver test = connection.getMySession().createReceiver("service_queue; {create: always}");

		Message received = test.fetch();
		ASSERT_EQUAL("SENDTESTING", received.getContent());
	}
	catch(const std::exception& error)
	{
		connection.close();
	}
	connection.close();
	return 0;
}
int DaemonGetMessageTest() {
	string url =  "172.16.33.7:5672";
	string connectionOptions = "{reconnect:false}";
	DaemonConnection connection(url, connectionOptions);
	responsePack response;

	if(! connection.openMySession(response.getMacID())){ //connection is closed
		ASSERTM("Connection is closed", connection.openMySession(response.getMacID()));
		return 100;
	}
	try{
		Sender test = connection.getMySession().createSender(response.getMacID()+"; {create: always}");
		Message sendout;
		sendout.setContent("RECEIVETESTING");
		test.send(sendout);
		string incoming = connection.getMessage();

		ASSERT_EQUAL("RECEIVETESTING", incoming);
	}
	catch(const std::exception& error)
	{
		connection.close();
	}
	connection.close();
	return 0;
}
void DaemonSerializeResponseTest(){
	responsePack sendresponse, receiveresponse;

	sendresponse.setType("TestResponse");
	sendresponse.setMacID("11:22:33:44:55:66");
	sendresponse.setRequestSeqnum(0);
	sendresponse.setResponseSeqnum(0);
	sendresponse.setStdout("default value");
	sendresponse.setStderr("default value");
	sendresponse.setUuid("TESTUUID");

	string output ;
	sendresponse.Serialize(output);

	ASSERTM("Response Serialization is unsuccessfull", receiveresponse.Deserialize(output));
	ASSERT_EQUAL(sendresponse.getMacID(),receiveresponse.getMacID());
	ASSERT_EQUAL(sendresponse.getRequestSeqnum(),receiveresponse.getRequestSeqnum());
	ASSERT_EQUAL(sendresponse.getType(),receiveresponse.getType());
	ASSERT_EQUAL(sendresponse.getResponseSeqnum(),receiveresponse.getResponseSeqnum()+1);
	ASSERT_EQUAL(sendresponse.getStderr(),receiveresponse.getStderr());
	ASSERT_EQUAL(sendresponse.getStdout(),receiveresponse.getStdout());
	ASSERT_EQUAL(sendresponse.getUuid(),receiveresponse.getUuid());
}
void DaemonSerializeResponseDoneTest(){
	responsePack sendresponse, receiveresponse;

	sendresponse.setType("TestResponse");
	sendresponse.setMacID("11:22:33:44:55:66");
	sendresponse.setRequestSeqnum(0);
	sendresponse.setResponseSeqnum(0);
	sendresponse.setUuid("TESTUUID");
	sendresponse.setExitcode("100");

	string output ;
	sendresponse.SerializeDone(output);

	ASSERTM("ResponseDone Serialization is unsuccessfull", receiveresponse.Deserialize(output));
	ASSERT_EQUAL(sendresponse.getMacID(),receiveresponse.getMacID());
	ASSERT_EQUAL(sendresponse.getRequestSeqnum(),receiveresponse.getRequestSeqnum());
	ASSERT_EQUAL(sendresponse.getType(),receiveresponse.getType());
	ASSERT_EQUAL(sendresponse.getResponseSeqnum(),receiveresponse.getResponseSeqnum()+1);
	ASSERT_EQUAL(sendresponse.getUuid(),receiveresponse.getUuid());
	ASSERT_EQUAL(sendresponse.getExitcode(),receiveresponse.getExitcode());
}
void DaemonSerializeCommandTest(){
	Command sendcommand,receivecommand;

	sendcommand.setType("TestCommand");
	sendcommand.setCwd("/");
	sendcommand.setUuid("TESTUUID");
	sendcommand.setRequestSeqnum(0);
	sendcommand.setStdout("default value");
	sendcommand.setStderr("default value");
	sendcommand.setRunAs("root");
	sendcommand.setProgram("/usr/bin/apt-get");
	sendcommand.getArguments().push_back("install");
	sendcommand.getArguments().push_back("synaptic");
	pair <string,string> dummy;
	dummy.first = "LD_PATH";
	dummy.second = "/usr/bin/";
	sendcommand.getEnv().push_back(dummy);

	string output;
	sendcommand.Serialize(output);


	ASSERTM("Command Serialization is unsuccessfull", 	receivecommand.Deserialize(output));
	ASSERT_EQUAL(sendcommand.getType(),receivecommand.getType());
	ASSERT_EQUAL(sendcommand.getRequestSeqnum(),receivecommand.getRequestSeqnum()+1);
	ASSERT_EQUAL(sendcommand.getUuid(),receivecommand.getUuid());
	ASSERT_EQUAL(sendcommand.getCwd(),receivecommand.getCwd());
	ASSERT_EQUAL(sendcommand.getStderr(),receivecommand.getStderr());
	ASSERT_EQUAL(sendcommand.getStdout(),receivecommand.getStdout());
	ASSERT_EQUAL(sendcommand.getRunAs(),receivecommand.getRunAs());
	ASSERT_EQUAL(sendcommand.getProgram(),receivecommand.getProgram());

	for(unsigned int index=0; index < sendcommand.getArguments().size(); index++)
	{
		ASSERT_EQUAL(sendcommand.getArguments()[index],receivecommand.getArguments()[index]);
	}

	std::list<pair<string,string> >::iterator rec;
	rec = receivecommand.getEnv().begin();
	for(std::list<pair<string,string> >::iterator it = sendcommand.getEnv().begin(); it != sendcommand.getEnv().end(); it++ ){
		ASSERT_EQUAL(it->first,rec->first);
		ASSERT_EQUAL(it->second,rec->second);
		rec ++;
	}
}
void DaemonThreadTest(){
	pool tp(2);
	ThreadOperation thread;
	Command sendcommand;
	string url =  "172.16.33.7:5672";
	string connectionOptions = "{reconnect:false}";
	DaemonConnection connection(url, connectionOptions);
	responsePack response;

	connection.openMySession(response.getMacID());


	sendcommand.setType("TestCommand");
	sendcommand.setCwd("/");
	sendcommand.setUuid("TESTUUID");
	sendcommand.setRequestSeqnum(0);
	sendcommand.setStdout("default value");
	sendcommand.setStderr("default value");
	sendcommand.setRunAs("root");
	sendcommand.setProgram("/usr/bin/apt-get");
	sendcommand.getArguments().push_back("install");
	sendcommand.getArguments().push_back("synaptic");
	pair <string,string> dummy;
	dummy.first = "LD_PATH";
	dummy.second = "/usr/bin/";
	sendcommand.getEnv().push_back(dummy);

	tp.schedule(boost::bind(&ThreadOperation::threadFunction,&thread,sendcommand ,connection));

}
void DaemonUidTest(){
	UserID uid;
	uid_t ruid, euid;

	ASSERT_EQUAL(true,uid.getIDs(ruid ,euid,"emin"));
}

void runSuite(){
	cute::suite daemon;
	//TODO add your test here
	daemon.push_back(CUTE(getMacIDTest));
	daemon.push_back(CUTE(DaemonCommunicationTest));
	daemon.push_back(CUTE(DaemonSendMessageTest));
	daemon.push_back(CUTE(DaemonGetMessageTest));
	daemon.push_back(CUTE(DaemonSerializeResponseTest));
	daemon.push_back(CUTE(DaemonSerializeResponseDoneTest));
	daemon.push_back(CUTE(DaemonSerializeCommandTest));
	//daemon.push_back(CUTE(DaemonThreadTest));
	daemon.push_back(CUTE(DaemonUidTest));

	cute::ide_listener lis;
	cute::makeRunner(lis)(daemon, "DaemonTestSuite");
}

int main(){
	runSuite();
}
