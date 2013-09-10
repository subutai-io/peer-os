/*
 * responsePack.cpp
 *
 *  Created on: Sep 4, 2013
 *      Author: qt-test
 */

#include "responsePack.h"

using namespace std;
responsePack::responsePack() {
	// TODO Auto-generated constructor stub

}

responsePack::~responsePack() {
	// TODO Auto-generated destructor stub
}

string responsePack::createResponseMessage(string uuid,int requestSeqNum,int responseSeqNum,string error,string output)
{
	this->setType("execute-response");
	this->setUuid(uuid);
	this->setRequestSeqnum(requestSeqNum);
	this->setResponseSeqnum(responseSeqNum);
	this->setStdout(output);
	this->setStderr(error);
	this->Serialize(sendout);
	return sendout;
}

string responsePack::createExitMessage(string uuid,int requestSeqNum,int responseSeqNum)
{
	this->setType("response-execute-done");
	this->setUuid(uuid);
	this->setRequestSeqnum(requestSeqNum);
	this->setResponseSeqnum(responseSeqNum);
	this->setExitcode("100");
	this->SerializeDone(sendout);
	return sendout;
}
string responsePack::createRegMessage(string uuid,string mac)
{
	this->setType("registration");
	this->setUuid(uuid);
	this->setMacID(mac);
	this->setRequestSeqnum(0);
	this->setExitcode("100");
	this->SerializeDone(sendout);
	return sendout;
}



