#include "KAResponsePack.h"
/**
 *  \details   Default constructor of the KAResponsePack class.
 */
KAResponsePack::KAResponsePack()
{
	// TODO Auto-generated constructor stub
}
/**
 *  \details   Default destructor of the KAResponsePack class.
 */
KAResponsePack::~KAResponsePack()
{
	// TODO Auto-generated destructor stub
}
/**
 *  \details   This method creates default chunk message.
 */
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
/**
 *  \details   This method creates Exit done message.
 */
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
/**
 *  \details   This method creates Registration message.
 */
string KAResponsePack::createRegistrationMessage(string uuid)	//Creating Registration Message
{
	this->setType("REGISTRATION_REQUEST");
	this->setUuid(uuid);
	this->serialize(sendout);
	return sendout;
}
/**
 *  \details   This method creates HeartBeat message.
 */
string KAResponsePack::createHeartBeatMessage(string uuid,int requestSeqNum)	//Creating HeartBeat Message
{
	this->setType("HEARTBEAT_RESPONSE");
	this->setUuid(uuid);
	this->setRequestSequenceNumber(requestSeqNum);
	this->setResponseSequenceNumber(1);
	this->serialize(sendout);
	return sendout;
}
/**
 *  \details   This method creates Termination message.
 */
string KAResponsePack::createTerminateMessage(string uuid,int requestSeqNum)	//Creating Terminate Message
{
	this->setType("TERMINATE_RESPONSE_DONE");
	this->setUuid(uuid);
	this->setRequestSequenceNumber(requestSeqNum);
	this->setResponseSequenceNumber(1);
	this->serialize(sendout);
	return sendout;
}
/**
 *  \details   This method creates Timeout message.
 */
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
