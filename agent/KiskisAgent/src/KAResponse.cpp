/**
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    @copyright 2014 Safehaus.org
 */
#include "KAResponse.h"

/**
 *  \details   Default constructor of the KAResponse class.
 */
KAResponse::KAResponse()
{
	// TODO Auto-generated constructor stub
	setType("");
	setUuid("");
	setPid(-1);
	setRequestSequenceNumber(-1);
	setResponseSequenceNumber(-1);
	setStandardError("");
	setStandardOutput("");
	setExitCode(-1);
	setSource("");
	setTaskUuid("");
	setMacAddress("");
	setHostname("");
	setParentHostname("");
	getIps().clear();
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
	setPid(-1);
	setRequestSequenceNumber(-1);
	setResponseSequenceNumber(-1);
	setStandardError("");
	setStandardOutput("");
	setSource("");
	setTaskUuid("");
	setMacAddress("");
	setHostname("");
	setParentHostname("");
	getIps().clear();
	setUuid("");
	setconfigPoint("");
	setDateTime("");
	setChangeType("");
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
	Json::Value root;
	Json::FastWriter writer;
	Json::Features myfeatures;
	myfeatures.all();

	//mandatory arguments

	if(!(this->getStandardOutput().empty()))
	{
		root["response"]["stdOut"] = this->getStandardOutput();
	}
	if(!(this->getStandardError().empty()))
	{
		root["response"]["stdErr"] = this->getStandardError();
	}
	if(!(this->getType().empty()))
	{
		root["response"]["type"] = this->getType();
	}
	if(!(this->getUuid().empty()))
	{
		root["response"]["uuid"] = this->getUuid();
	}
	if(this->getPid() >= 0)
	{
		root["response"]["pid"] = this->getPid();										//check the pid is assigned or not
	}
	if(this->getRequestSequenceNumber() >= 0)											//check the requestSequenceNumber is assigned or not
	{
		root["response"]["requestSequenceNumber"] = this->getRequestSequenceNumber();
	}
	if(this->getResponseSequenceNumber() >= 0)										//check the responseSequenceNumber is assigned or not
	{
		root["response"]["responseSequenceNumber"] = this->getResponseSequenceNumber();
	}
	for(unsigned int index=0; index < this->getIps().size(); index++)
	{	//automatically check the size of the ips list
		root["response"]["ips"][index]=this->getIps()[index];
	}
	if(!(this->getTaskUuid().empty()))											//check the taskuuid is assigned or not
	{
		root["response"]["taskUuid"] = this->getTaskUuid();
	}
	if(!(this->getHostname().empty()))											//check the hostname is assigned or not
	{
		root["response"]["hostname"] = this->getHostname();
	}
	if(!(this->getParentHostname().empty()))											//check the parenthostname is assigned or not
	{
		root["response"]["parentHostName"] = this->getParentHostname();
	}
	if(!(this->getMacAddress().empty()))											//check the macAddress is assigned or not
	{
		root["response"]["macAddress"] = this->getMacAddress();
	}
	if(!(this->getSource().empty()))
	{
		root["response"]["source"] = this->getSource();
	}
	if(!(this->getconfigPoint().empty()))
	{
		root["response"]["configPoint"] = this->getconfigPoint();
	}
	if(!(this->getDateTime().empty()))
	{
		root["response"]["dateTime"] = this->getDateTime();
	}
	if(!(this->getChangeType().empty()))
	{
		root["response"]["changeType"] = this->getChangeType();
	}
	for(unsigned int index=0; index < this->getConfPoints().size(); index++)
	{
		if(this->getType() == "INOTIFY_LIST_RESPONSE")
		{
			root["response"]["configPoints"][index]=this->getConfPoints()[index];
		}
		else if(this->getType() == "INOTIFY_ACTION_RESPONSE")
		{
			root["response"]["configPoint"][index]=this->getConfPoints()[index];
		}
	}

	output = writer.write(root);
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
	Json::Value root;
	Json::FastWriter writer;
	Json::Features myfeatures;
	myfeatures.all();

	if(!(this->getType().empty()))
	{
		root["response"]["type"] = this->getType();
	}
	if(!(this->getUuid().empty()))
	{
		root["response"]["uuid"] = this->getUuid();
	}
	if(this->getRequestSequenceNumber() >= 0)											//check the requestSequenceNumber is assigned or not
	{
		root["response"]["requestSequenceNumber"] = this->getRequestSequenceNumber();
	}
	if(this->getResponseSequenceNumber() >= 0)										//check the responseSequenceNumber is assigned or not
	{
		root["response"]["responseSequenceNumber"] = this->getResponseSequenceNumber();
	}
	if(this->getPid() >= 0)
	{
		root["response"]["pid"] = this->getPid();										//check the pid is assigned or not
	}
	if(this->getExitCode() >= 0)
	{
		root["response"]["exitCode"] = this->getExitCode();
	}
	if(!(this->getTaskUuid().empty()))											//check the taskuuid is assigned or not
	{
		root["response"]["taskUuid"] = this->getTaskUuid();
	}
	if(!(this->getSource().empty()))											//check the macAddress is assigned or not
	{
		root["response"]["source"] = this->getSource();
	}
	output = writer.write(root);	//Json Response Done string is created
}

/**
 *  \details   getting "pid" private variable of KAResponse instance
 */
int KAResponse::getPid()
{						//getting pid
	return this->pid;
}

/**
 *  \details   setting "pid" private variable of KAResponse instance
 */
void KAResponse::setPid(int pid)
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

/**
 *  \details   getting "parenthostname" private variable of KAResponse instance.
 */
string& KAResponse::getParentHostname()
{
	return this->parentHostname;
}

/**
 *  \details   setting "parenthostname" private variable of KAResponse instance.
 *  		   This holds the parenthostname of the agent machine
 */
void KAResponse::setParentHostname(const string& parenthostname)
{
	this->parentHostname = parenthostname;
}

/**
 *  \details   getting "hostname" private variable of KAResponse instance.
 */
string& KAResponse::getHostname()
{
	return this->hostname;
}

/**
 *  \details   setting "hostname" private variable of KAResponse instance.
 *  		   This holds the hostname of the agent machine
 */
void KAResponse::setHostname(const string& hostname)
{
	this->hostname = hostname;
}

/**
 *  \details   getting "macAddress" private variable of KAResponse instance.
 */
string& KAResponse::getMacAddress()
{
	return this->macAddress;
}

/**
 *  \details   setting "macAddress" private variable of KAResponse instance.
 *  		   This holds the macAddress(eth0) of the agent machine
 */
void KAResponse::setMacAddress(const string& macAddress)
{
	this->macAddress = macAddress;
}

/**
 *  \details   getting "taskUuid" private variable of KAResponse instance.
 */
string& KAResponse::getTaskUuid()
{
	return this->taskUuid;
}

/**
 *  \details   setting "taskUuid" private variable of KAResponse instance.
 *  		   This holds the task uuid of the command
 */
void KAResponse::setTaskUuid(const string& taskuuid)
{
	this->taskUuid = taskuuid;
}

/**
 *  \details   setting "ips" private vector variable of KAResponse instance.
 *  		   This is the list of ips vector that holds the ip addresses of the machine
 */
void KAResponse::setIps(vector<string> myvector)
{
	this->ips.clear();
	for(unsigned int index=0 ; index< myvector.size(); index++)
	{
		this->ips.push_back(myvector[index]);
	}
}

/**
 *  \details   getting "ips" private vector variable of KAResponse instance.
 */
vector<string>& KAResponse::getIps()
{					//getting ips vector

	return this->ips;
}

/**
 *  \details   getting "source" private variable of KAResponse instance.
 */
string& KAResponse::getSource()
{
	return this->source;
}

/**
 *  \details   setting "source" private variable of KAResponse instance.
 *  		   This holds the task source information of the response
 */
void KAResponse::setSource(const string& source)
{
	this->source = source;
}

/**
 *  \details   setting "configPoint" private variable of KAResponse instance.
 *  		   This holds the task source information of the response
 */
void KAResponse::setconfigPoint(const string& configPoint)
{
	this->configPoint = configPoint;
}

/**
 *  \details   getting "configPoint" private variable of KAResponse instance.
 */
string& KAResponse::getconfigPoint()
{
	return this->configPoint;
}

/**
 *  \details   setting "dateTime" private variable of KAResponse instance.
 *  		   This holds the task source information of the response
 */
void KAResponse::setDateTime(const string& dateTime)
{
	this->dateTime = dateTime;
}

/**
 *  \details   getting "dateTime" private variable of KAResponse instance.
 */
string& KAResponse::getDateTime()
{
	return this->dateTime;
}

/**
 *  \details   setting "changeType" private variable of KAResponse instance.
 *  		   This holds the task source information of the response
 */
void KAResponse::setChangeType(const string& changeType)
{
	this->changeType = changeType;
}

/**
 *  \details   getting "changeType" private variable of KAResponse instance.
 */
string& KAResponse::getChangeType()
{
	return this->changeType;
}

/**
 *  \details   setting "confPoints" private vector variable of KAResponse instance.
 *  		   This is the list of config Points vector that holds the watching points of the agent
 */
void KAResponse::setConfPoints(vector<string> myvector)
{
	this->confPoints.clear();
	for(unsigned int index=0 ; index< myvector.size(); index++)
	{
		this->confPoints.push_back(myvector[index]);
	}
}

/**
 *  \details   getting "confPoints" private vector variable of KAResponse instance.
 */
vector<string>& KAResponse::getConfPoints()
{					//getting ips vector

	return this->confPoints;
}
