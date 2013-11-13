#include "KAResponse.h"
/**
 *  \details   Default constructor of the KAResponse class.
 */
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
/**
 *  \details   Default destructor of the KAResponse class.
 */
KAResponse::~KAResponse()
{
	// TODO Auto-generated destructor stub
}
/**
 *  \details   This method clears the all pricate variables in the given KAResponse instance.
 */
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
/**
 *  \details   serialize function creates a JSON strings from called instance.
 *  		   This is one of the most frequently used function is the class.
 *  		   It also check the existing variable(NULL or not) when serializing the instance.
 *  		   It returns given reference output strings.
 */
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
/**
 *  \details   serializeDone method serialize the Done response JSon string from called instance.
 *     		   This is one of the most frequently used function is the class.
 *     		   It also check the existing variable(NULL or not) when serializing the instance.
 *  		   It returns given reference output strings.
 */
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
/**
 *  \details   deserialize function deserialize the given Json strings to KAResponse instance.
 *     		   This is one of the most frequently used function is the class.
 *  		   It also check the existing variable in the JSON strings when deserializing the instance.
 *  		   it uses reference input and deserialize it to called KAResponse instance
 *  		   it returns true if the given input string is true formatted otherwise return false.
 */
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
/**
 *  \details   getting "pid" private variable of KAResponse instance
 */
string& KAResponse::getPid()
{						//getting pid
	return this->pid;
}
/**
 *  \details   setting "pid" private variable of KAResponse instance
 */
void KAResponse::setPid(const string& pid)
{			//setting pid
	this->pid=pid;
}
/**
 *  \details   getting "exitCode" private variable of KAResponse instance
 */
int KAResponse::getExitCode()
{					//getting ExitCode
	return this->exitCode;
}
/**
 *  \details   setting "exitCode" private variable of KAResponse instance
 */
void KAResponse::setExitCode(int exitcode)
{			//setting ExitCode
	this->exitCode = exitcode;
}
/**
 *  \details   getting "type" private variable of KAResponse instance
 */
string& KAResponse::getType()
{								//getting Type
	return this->type;
}
/**
 *  \details   setting "type" private variable of KAResponse instance
 */
void KAResponse::setType(const string& type)
{				//setting Type
	this->type = type;
}
/**
 *  \details   getting "uuid" private variable of KAResponse instance
 */
string& KAResponse::getUuid()
{								//getting uuid
	return this->uuid;
}
/**
 *  \details   setting "uuid" private variable of KAResponse instance
 */
void KAResponse::setUuid(const string& uuid)
{				//setting uuid
	this->uuid = uuid;
}
/**
 *  \details   getting "requestSequenceNumber" private variable of KAResponse instance
 */
int KAResponse::getRequestSequenceNumber()
{								//getting RequestSeqnumber
	return this->requestSequenceNumber;
}
/**
 *  \details   setting "requestSequenceNumber" private variable of KAResponse instance
 */
void KAResponse::setRequestSequenceNumber(int requestSequenceNumber)
{	//setting RequestSeqnumber
	this->requestSequenceNumber = requestSequenceNumber;
}
/**
 *  \details   getting "responseSequenceNumber" private variable of KAResponse instance
 */
int KAResponse::getResponseSequenceNumber()
{									//getting ResponseSeqnumber
	return this->responseSequenceNumber;
}
/**
 *  \details   setting "responseSequenceNumber" private variable of KAResponse instance
 */
void KAResponse::setResponseSequenceNumber(int responseSequenceNumber)
{			//setting ResponseSeqnumber
	this->responseSequenceNumber = responseSequenceNumber;
}
/**
 *  \details   getting "stdErr" private variable of KAResponse instance
 */
string& KAResponse::getStandardError()
{						//getting standard err
	return this->stdErr;
}
/**
 *  \details   setting "stdErr" private variable of KAResponse instance
 */
void KAResponse::setStandardError(const string& mystderr)
{		//setting standard err
	this->stdErr = mystderr;
}
/**
 *  \details   getting "stdOut" private variable of KAResponse instance
 */
string& KAResponse::getStandardOutput()
{						//getting standard out
	return this->stdOut;
}
/**
 *  \details   setting "stdOut" private variable of KAResponse instance
 */
void KAResponse::setStandardOutput(const string& mystdout)
{ 	//setting standard out
	this->stdOut = mystdout;
}
