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
string KAResponsePack::createResponseMessage(string uuid,int pid,int requestSeqNum,int responseSeqNum,
		string error,string output,string source,string taskuuid)
{
	this->setType("EXECUTE_RESPONSE");			//creating Response chunk message
	this->setSource(source);
	this->setTaskUuid(taskuuid);
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
string KAResponsePack::createExitMessage(string uuid,int pid,int requestSeqNum,int responseSeqNum,
		string source, string taskuuid,int exitcode)	//Creating Exit message
{
	this->setType("EXECUTE_RESPONSE_DONE");
	this->setSource(source);
	this->setTaskUuid(taskuuid);
	this->setUuid(uuid);
	this->setPid(pid);
	this->setRequestSequenceNumber(requestSeqNum);
	this->setResponseSequenceNumber(responseSeqNum);
	this->setExitCode(exitcode);
	this->serializeDone(sendout);
	return sendout;
}
/**
 *  \details   This method creates Registration message.
 */
string KAResponsePack::createRegistrationMessage(string uuid,string macaddress,string hostname,bool islxc)
{	//Creating Registration Message
	this->setType("REGISTRATION_REQUEST");
	this->setMacAddress(macaddress);
	this->setHostname(hostname);
	this->setIsLxc(islxc);
	this->setUuid(uuid);
	this->serialize(sendout);
	return sendout;
}
/**
 *  \details   This method creates HeartBeat message.
 */
string KAResponsePack::createHeartBeatMessage(string uuid,int requestSeqNum,string macaddress,
		string hostname,bool islxc,string source,string taskuuid)	//Creating HeartBeat Message
{
	this->setType("HEARTBEAT_RESPONSE");
	this->setSource(source);
	this->setTaskUuid(taskuuid);
	this->setMacAddress(macaddress);
	this->setHostname(hostname);
	this->setIsLxc(islxc);
	this->setUuid(uuid);
	this->setRequestSequenceNumber(requestSeqNum);
	this->setResponseSequenceNumber(1);
	this->serialize(sendout);
	return sendout;
}
/**
 *  \details   This method creates  SuccessTermination message.
 */
string KAResponsePack::createTerminateMessage(string uuid,int requestSeqNum,string source)	//Creating Terminate Message
{
	this->setType("TERMINATE_RESPONSE_DONE");
	this->setSource(source);
	this->setExitCode(0);
	this->setUuid(uuid);
	this->setRequestSequenceNumber(requestSeqNum);
	this->setResponseSequenceNumber(1);
	this->serialize(sendout);
	return sendout;
}
/**
 *  \details   This method creates Fail Termination message.
 */
string KAResponsePack::createFailTerminateMessage(string uuid,int requestSeqNum,string source)	//Creating Failed Terminate Message
{
	this->setType("TERMINATE_RESPONSE_FAILED");
	this->setSource(source);
	this->setExitCode(1);
	this->setUuid(uuid);
	this->setRequestSequenceNumber(requestSeqNum);
	this->setResponseSequenceNumber(1);
	this->serialize(sendout);
	return sendout;
}
/**
 *  \details   This method creates Timeout message.
 */
string KAResponsePack::createTimeoutMessage(string uuid,int pid,int requestSeqNum,int responseSeqNum,
		string stdOut,string stdErr,string source,string taskuuid)	//Creating Timeout Message
{
	this->setType("EXECUTE_TIMEOUTED");
	this->setSource(source);
	this->setTaskUuid(taskuuid);
	this->setPid(pid);
	this->setUuid(uuid);
	this->setRequestSequenceNumber(requestSeqNum);
	this->setResponseSequenceNumber(responseSeqNum);
	this->setStandardOutput(stdOut);
	this->setStandardError(stdErr);
	this->serialize(sendout);
	return sendout;
}

