/*
 *============================================================================
 Name        : KAResponsePack.cpp
 Author      : Bilal BAL & Emin INAL
 Date		 : Sep 4, 2013
 Version     : 1.0
 Copyright   : Your copyright notice
 Description : KAResponsePack class is designed for sending response chunk messages to Broker
==============================================================================
 */
#include "KAResponsePack.h"
KAResponsePack::KAResponsePack()
{
	// TODO Auto-generated constructor stub
}
KAResponsePack::~KAResponsePack()
{
	// TODO Auto-generated destructor stub
}
string KAResponsePack::createResponseMessage(string uuid,string pid,int requestSeqNum,int responseSeqNum,string error,string output)
{
	this->setType("EXECUTE_RESPONSE");			//creating Response chunk message
	this->setUuid(uuid);
	this->setRequestSequenceNumber(requestSeqNum);
	this->setResponseSequenceNumber(responseSeqNum);
	this->setStandardOutput(output);
	this->setStandardError(error);
	this->setPid(pid);
	this->serialize(sendout);
	return sendout;
}
string KAResponsePack::createExitMessage(string uuid,string pid,int requestSeqNum,int responseSeqNum)	//Creating Exit message
{
	this->setType("EXECUTE_RESPONSE_DONE");
	this->setUuid(uuid);
	this->setPid(pid);
	this->setRequestSequenceNumber(requestSeqNum);
	this->setResponseSequenceNumber(responseSeqNum);
	this->setExitCode(0);
	this->serializeDone(sendout);
	return sendout;
}
string KAResponsePack::createRegistrationMessage(string uuid)	//Creating Registration Message
{
	this->setType("REGISTRATION_REQUEST");
	this->setUuid(uuid);
	this->serialize(sendout);
	return sendout;
}
string KAResponsePack::createHeartBeatMessage(string uuid,int requestSeqNum)	//Creating HeartBeat Message
{
	this->setType("HEARTBEAT_RESPONSE");
	this->setUuid(uuid);
	this->setRequestSequenceNumber(requestSeqNum);
	this->setResponseSequenceNumber(1);
	this->serialize(sendout);
	return sendout;
}
string KAResponsePack::createTerminateMessage(string uuid,int requestSeqNum)	//Creating Terminate Message
{
	this->setType("TERMINATE_RESPONSE_DONE");
	this->setUuid(uuid);
	this->setRequestSequenceNumber(requestSeqNum);
	this->setResponseSequenceNumber(1);
	this->serialize(sendout);
	return sendout;
}
string KAResponsePack::createTimeoutMessage(string uuid,string pid,int requestSeqNum,int responseSeqNum,string stdOut,string stdErr)	//Creating Timeout Message
{
	this->setType("EXECUTE_TIMEOUTED");
	this->setPid(pid);
	this->setUuid(uuid);
	this->setRequestSequenceNumber(requestSeqNum);
	this->setResponseSequenceNumber(responseSeqNum);
	this->setStandardOutput(stdOut);
	this->setStandardError(stdErr);
	this->serialize(sendout);
	return sendout;
}
