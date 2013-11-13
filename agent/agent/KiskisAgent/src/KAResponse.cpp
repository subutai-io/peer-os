/*
 *============================================================================
 Name        : KAResponse.cpp
 Author      : Emin INAL
 Date		 : Aug 29, 2013
 Version     : 1.0
 Copyright   : Your copyright notice
 Description : KAResponse class is Designed for Sending Response Messages to Broker
==============================================================================
 */
#include "KAResponse.h"
KAResponse::KAResponse()
{
	// TODO Auto-generated constructor stub
	setType("");
	setUuid("");
	setPid("");
	setRequestSequenceNumber(-1);
	setResponseSequenceNumber(-1);
	setStandardError("");
	setStandardOutput("");
	setExitCode(-1);
}
KAResponse::~KAResponse()
{
	// TODO Auto-generated destructor stub
}
void KAResponse::clear()
{		//clear the all variables..
	setType("");
	setUuid("");
	setPid("");
	setRequestSequenceNumber(-1);
	setResponseSequenceNumber(-1);
	setStandardError("");
	setStandardOutput("");
}
void KAResponse::serialize(string& output)
{
	Json::Value environment;
	Json::StyledWriter writer;
	Json::Value root;

	//mandatory arguments

	if(!getStandardOutput().empty())
		root["response"]["stdOut"] = getStandardOutput();
	if(!getStandardError().empty())
		root["response"]["stdErr"] = getStandardError();
	if(!getType().empty())
		root["response"]["type"] = getType();
	if(!getUuid().empty())
		root["response"]["uuid"] = getUuid();
	if(!getPid().empty())
		root["response"]["pid"] = getPid();										//check the pid is assigned or not
	if(getRequestSequenceNumber() >= 0)											//check the requestSequenceNumber is assigned or not
		root["response"]["requestSequenceNumber"]=getRequestSequenceNumber();
	if(getResponseSequenceNumber() >= 0)										//check the responseSequenceNumber is assigned or not
		root["response"]["responseSequenceNumber"]=getResponseSequenceNumber();
	output = writer.write(root);		//Json Response string is created
}
void KAResponse::serializeDone(string& output)
{			//Serialize a Done Response  to a Json String
	Json::Value environment;
	Json::StyledWriter writer;
	Json::Value root;

	if(!getType().empty())
		root["response"]["type"] = getType();
	if(!getUuid().empty())
		root["response"]["uuid"] = getUuid();
	if(getRequestSequenceNumber() >= 0)											//check the requestSequenceNumber is assigned or not
		root["response"]["requestSequenceNumber"]=getRequestSequenceNumber();
	if(getResponseSequenceNumber() >= 0)										//check the responseSequenceNumber is assigned or not
		root["response"]["responseSequenceNumber"]=getResponseSequenceNumber();
	if(!getPid().empty())
		root["response"]["pid"] = getPid();										//check the pid is assigned or not
	if(getExitCode() >= 0)
		root["response"]["exitCode"] = getExitCode();
	output = writer.write(root);		//Json Response Done string is created
}
bool KAResponse::deserialize(string& input)
{													//Deserialize a Json String to Response instance
	Json::FastWriter writer;						//return true: if Deserialization is successfull
	Json::Reader reader;							//return false: if Deserialization unsuccessfull
	Json::Value root;
	clear(); //clear all arguments firstly..

	bool parsedSuccess = reader.parse(input,root,false);			//Parsing Json String to Response instance

	if(!parsedSuccess)											//if parsing is not successfull
	{
		cout<<"Failed to parse JSON"<<endl<<reader.getFormatedErrorMessages()<<endl;
		return false;
	}

	if(root["response"]["exitCode"].isNull())	//if incoming message does not contain Exit code it is just a chunk response
	{
		if(!root["response"]["type"].isNull())
		{				//initialize type parameter if it is not null
			setType(root["response"]["type"].asString());
		}
		if(!root["response"]["uuid"].isNull())
		{
			setUuid(root["response"]["uuid"].asString());				//initialize UUID parameter if it is not null
		}
		if(!root["response"]["stdOut"].isNull())
		{
			setStandardOutput(root["response"]["stdOut"].asString());		//initialize standardOutput parameter if it is not null
		}
		if(!root["response"]["stdErr"].isNull())
		{
			setStandardError(root["response"]["stdErr"].asString());		//initialize standardError parameter if it is not null
		}
		if(!root["response"]["pid"].isNull())
		{
			setPid(root["response"]["pid"].asString());				//initialize pid parameter if it is not null
		}
		if(!root["response"]["requestSequenceNumber"].isNull())
		{
			setRequestSequenceNumber(root["response"]["requestSequenceNumber"].asInt()); //initialize requestSequenceNumber parameter if it is not null
		}
		if(!root["response"]["responseSequenceNumber"].isNull())
		{
			setResponseSequenceNumber(root["response"]["responseSequenceNumber"].asInt()); //initialize requestSequenceNumber parameter if it is not null
		}
	}
	else
	{				//if incoming message contain Exit code it is just an exit response
		if(!root["response"]["type"].isNull())
		{				//initialize type parameter if it is not null
			setType(root["response"]["type"].asString());
		}
		if(!root["response"]["uuid"].isNull())
		{
			setUuid(root["response"]["uuid"].asString());				//initialize UUID parameter if it is not null
		}
		if(!root["response"]["pid"].isNull())
		{
			setPid(root["response"]["pid"].asString());				//initialize pid parameter if it is not null
		}
		if(!root["response"]["exitCode"].isNull())
		{
			setExitCode(root["response"]["exitCode"].asInt());				//initialize pid parameter if it is not null
		}
		if(!root["response"]["requestSequenceNumber"].isNull())
		{
			setRequestSequenceNumber(root["response"]["requestSequenceNumber"].asInt()); //initialize requestSequenceNumber parameter if it is not null
		}
		if(!root["response"]["responseSequenceNumber"].isNull())
		{
			setResponseSequenceNumber(root["response"]["responseSequenceNumber"].asInt()); //initialize responseSequenceNumber parameter if it is not null
		}
	}
	return true;
}
string& KAResponse::getPid()
{						//getting pid
	return this->pid;
}
void KAResponse::setPid(const string& pid)
{			//setting pid
	this->pid=pid;
}
int KAResponse::getExitCode()
{					//getting ExitCode
	return this->exitCode;
}
void KAResponse::setExitCode(int exitcode)
{			//setting ExitCode
	this->exitCode = exitcode;
}
string& KAResponse::getType()
{								//getting Type
	return this->type;
}
void KAResponse::setType(const string& type)
{				//setting Type
	this->type = type;
}
string& KAResponse::getUuid()
{								//getting uuid
	return this->uuid;
}
void KAResponse::setUuid(const string& uuid)
{				//setting uuid
	this->uuid = uuid;
}
int KAResponse::getRequestSequenceNumber()
{								//getting RequestSeqnumber
	return this->requestSequenceNumber;
}
void KAResponse::setRequestSequenceNumber(int requestSequenceNumber)
{	//setting RequestSeqnumber
	this->requestSequenceNumber = requestSequenceNumber;
}
int KAResponse::getResponseSequenceNumber()
{									//getting ResponseSeqnumber
	return this->responseSequenceNumber;
}
void KAResponse::setResponseSequenceNumber(int responseSequenceNumber)
{			//setting ResponseSeqnumber
	this->responseSequenceNumber = responseSequenceNumber;
}
string& KAResponse::getStandardError()
{						//getting standard err
	return this->stdErr;
}
void KAResponse::setStandardError(const string& mystderr)
{		//setting standard err
	this->stdErr = mystderr;
}
string& KAResponse::getStandardOutput()
{						//getting standard out
	return this->stdOut;
}
void KAResponse::setStandardOutput(const string& mystdout)
{ 	//setting standard out
	this->stdOut = mystdout;
}
