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
#include "SubutaiResponse.h"

/**
 *  \details   Default constructor of the SubutaiResponse class.
 */
SubutaiResponse::SubutaiResponse()
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
 *  \details   Default destructor of the SubutaiResponse class.
 */
SubutaiResponse::~SubutaiResponse()
{
    // TODO Auto-generated destructor stub
}

/**
 *  \details   This method clears the all pricate variables in the given SubutaiResponse instance.
 */
void SubutaiResponse::clear()
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
 *
 *
 *
 */
void SubutaiResponse::serialize(string& output)
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
        root["response"]["id"] = this->getUuid();
    }
    if(this->getPid() >= 0)
    {
        root["response"]["pid"] = this->getPid();										//check the pid is assigned or not
    }
    if(this->getRequestSequenceNumber() >= 0)											//check the requestSequenceNumber is assigned or not
    {
        root["response"]["requestNumber"] = this->getRequestSequenceNumber();
    }
    if(this->getResponseSequenceNumber() >= 0)										//check the responseSequenceNumber is assigned or not
    {
        root["response"]["responseNumber"] = this->getResponseSequenceNumber();
    }
    for(unsigned int index=0; index < this->getIps().size(); index++)
    {	//automatically check the size of the ips list
        root["response"]["ips"][index]=this->getIps()[index];
    }
    if(!(this->getTaskUuid().empty()))											//check the taskuuid is assigned or not
    {
        root["response"]["commandId"] = this->getTaskUuid();
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
    if(!(this->getEnvironmentId().empty()))
    {
        root["response"]["environmentId"] = this->getEnvironmentId();
    }
    for(unsigned int index = 0; index < this->containers.size(); index++) {
        root["response"]["containers"][index]["hostname"]	= this->containers[index].getContainerHostnameValue();
        root["response"]["containers"][index]["id"]		= this->containers[index].getContainerIdValue();
        root["response"]["containers"][index]["status"]		= this->containers[index].getContainerStatus();
        vector<string> ipValues	=	this->containers[index].getContainerIpValue();
        for(unsigned int i=0; i < ipValues.size(); i++) {
            root["response"]["containers"][index]["ips"][i]=ipValues[i];
        }
    }
    for(unsigned int index = 0; index < this->getConfPoints().size(); index++) {
        if(this->getType() == "INOTIFY_LIST_RESPONSE") {
            root["response"]["configPoints"][index]=this->getConfPoints()[index];
        } else if(this->getType() == "INOTIFY_ACTION_RESPONSE") {
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
void SubutaiResponse::serializeDone(string& output)
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
 *   \details Add a new container set for response.
 */
void SubutaiResponse::setContainerSet(vector<SubutaiContainer> contSet)
{
    this->containers.clear();
    this->containers = contSet;
}


/**
 *  \details   getting "pid" private variable of SubutaiResponse instance
 */
int SubutaiResponse::getPid()
{						//getting pid
    return this->pid;
}

/**
 *  \details   setting "pid" private variable of SubutaiResponse instance
 */
void SubutaiResponse::setPid(int pid)
{			//setting pid
    this->pid=pid;
}

/**
 *  \details   getting "exitCode" private variable of SubutaiResponse instance
 */
int SubutaiResponse::getExitCode()
{					//getting ExitCode
    return this->exitCode;
}

/**
 *  \details   setting "exitCode" private variable of SubutaiResponse instance
 */
void SubutaiResponse::setExitCode(int exitcode)
{			//setting ExitCode
    this->exitCode = exitcode;
}

/**
 *  \details   getting "type" private variable of SubutaiResponse instance
 */
string& SubutaiResponse::getType()
{								//getting Type
    return this->type;
}

/**
 *  \details   setting "type" private variable of SubutaiResponse instance
 */
void SubutaiResponse::setType(const string& type)
{				//setting Type
    this->type = type;
}

/**
 *  \details   getting "uuid" private variable of SubutaiResponse instance
 */
string& SubutaiResponse::getUuid()
{								//getting uuid
    return this->uuid;
}

/**
 *  \details   setting "uuid" private variable of SubutaiResponse instance
 */
void SubutaiResponse::setUuid(const string& uuid)
{				//setting uuid
    this->uuid = uuid;
}

/**
 *  \details   getting "requestSequenceNumber" private variable of SubutaiResponse instance
 */
int SubutaiResponse::getRequestSequenceNumber()
{								//getting RequestSeqnumber
    return this->requestSequenceNumber;
}

/**
 *  \details   setting "requestSequenceNumber" private variable of SubutaiResponse instance
 */
void SubutaiResponse::setRequestSequenceNumber(int requestSequenceNumber)
{	//setting RequestSeqnumber
    this->requestSequenceNumber = requestSequenceNumber;
}

/**
 *  \details   getting "responseSequenceNumber" private variable of SubutaiResponse instance
 */
int SubutaiResponse::getResponseSequenceNumber()
{									//getting ResponseSeqnumber
    return this->responseSequenceNumber;
}

/**
 *  \details   setting "responseSequenceNumber" private variable of SubutaiResponse instance
 */
void SubutaiResponse::setResponseSequenceNumber(int responseSequenceNumber)
{			//setting ResponseSeqnumber
    this->responseSequenceNumber = responseSequenceNumber;
}

/**
 *  \details   getting "stdErr" private variable of SubutaiResponse instance
 */
string& SubutaiResponse::getStandardError()
{						//getting standard err
    return this->stdErr;
}
/**
 *  \details   setting "stdErr" private variable of SubutaiResponse instance
 */
void SubutaiResponse::setStandardError(const string& mystderr)
{		//setting standard err
    this->stdErr = mystderr;
}

/**
 *  \details   getting "stdOut" private variable of SubutaiResponse instance
 */
string& SubutaiResponse::getStandardOutput()
{						//getting standard out
    return this->stdOut;
}

/**
 *  \details   setting "stdOut" private variable of SubutaiResponse instance
 */
void SubutaiResponse::setStandardOutput(const string& mystdout)
{ 	//setting standard out
    this->stdOut = mystdout;
}

/**
 *  \details   getting "parenthostname" private variable of SubutaiResponse instance.
 */
string& SubutaiResponse::getParentHostname()
{
    return this->parentHostname;
}

/**
 *  \details   setting "parenthostname" private variable of SubutaiResponse instance.
 *  		   This holds the parenthostname of the agent machine
 */
void SubutaiResponse::setParentHostname(const string& parenthostname)
{
    this->parentHostname = parenthostname;
}

/**
 *  \details   getting "hostname" private variable of SubutaiResponse instance.
 */
string& SubutaiResponse::getHostname()
{
    return this->hostname;
}

/**
 *  \details   getting "environmentId" private variable of SubutaiResponse instance.
 */
string& SubutaiResponse::getEnvironmentId()
{
    return this->environmentId;
}

/**
 *  \details   setting "hostname" private variable of SubutaiResponse instance.
 *  		   This holds the hostname of the agent machine
 */
void SubutaiResponse::setHostname(const string& hostname)
{
    this->hostname = hostname;
}

/**
 *  \details   getting "macAddress" private variable of SubutaiResponse instance.
 */
string& SubutaiResponse::getMacAddress()
{
    return this->macAddress;
}

/**
 *  \details   setting "macAddress" private variable of SubutaiResponse instance.
 *  		   This holds the macAddress(eth0) of the agent machine
 */
void SubutaiResponse::setMacAddress(const string& macAddress)
{
    this->macAddress = macAddress;
}

/**
 *  \details   setting "environmentId" private variable of SubutaiResponse instance.
 */
void SubutaiResponse::setEnvironmentId(const string& envID)
{
    this->environmentId = envID;
}

/**
 *  \details   getting "taskUuid" private variable of SubutaiResponse instance.
 */
string& SubutaiResponse::getTaskUuid()
{
    return this->taskUuid;
}

/**
 *  \details   setting "taskUuid" private variable of SubutaiResponse instance.
 *  		   This holds the task uuid of the command
 */
void SubutaiResponse::setTaskUuid(const string& taskuuid)
{
    this->taskUuid = taskuuid;
}

/**
 *  \details   setting "ips" private vector variable of SubutaiResponse instance.
 *  		   This is the list of ips vector that holds the ip addresses of the machine
 */
void SubutaiResponse::setIps(vector<string> myvector)
{
    this->ips.clear();
    for(unsigned int index=0 ; index< myvector.size(); index++)
    {
        this->ips.push_back(myvector[index]);
    }
}

/**
 *  \details   getting "ips" private vector variable of SubutaiResponse instance.
 */
vector<string>& SubutaiResponse::getIps()
{					//getting ips vector

    return this->ips;
}

/**
 *  \details   getting "source" private variable of SubutaiResponse instance.
 */
string& SubutaiResponse::getSource()
{
    return this->source;
}

/**
 *  \details   setting "source" private variable of SubutaiResponse instance.
 *  		   This holds the task source information of the response
 */
void SubutaiResponse::setSource(const string& source)
{
    this->source = source;
}

/**
 *  \details   setting "configPoint" private variable of SubutaiResponse instance.
 *  		   This holds the task source information of the response
 */
void SubutaiResponse::setconfigPoint(const string& configPoint)
{
    this->configPoint = configPoint;
}

/**
 *  \details   getting "configPoint" private variable of SubutaiResponse instance.
 */
string& SubutaiResponse::getconfigPoint()
{
    return this->configPoint;
}

/**
 *  \details   setting "dateTime" private variable of SubutaiResponse instance.
 *  		   This holds the task source information of the response
 */
void SubutaiResponse::setDateTime(const string& dateTime)
{
    this->dateTime = dateTime;
}

/**
 *  \details   getting "dateTime" private variable of SubutaiResponse instance.
 */
string& SubutaiResponse::getDateTime()
{
    return this->dateTime;
}

/**
 *  \details   setting "changeType" private variable of SubutaiResponse instance.
 *  		   This holds the task source information of the response
 */
void SubutaiResponse::setChangeType(const string& changeType)
{
    this->changeType = changeType;
}

/**
 *  \details   getting "changeType" private variable of SubutaiResponse instance.
 */
string& SubutaiResponse::getChangeType()
{
    return this->changeType;
}

/**
 *  \details   setting "confPoints" private vector variable of SubutaiResponse instance.
 *  		   This is the list of config Points vector that holds the watching points of the agent
 */
void SubutaiResponse::setConfPoints(vector<string> myvector)
{
    this->confPoints.clear();
    for(unsigned int index=0 ; index< myvector.size(); index++)
    {
        this->confPoints.push_back(myvector[index]);
    }
}

/**
 *  \details   getting "confPoints" private vector variable of SubutaiResponse instance.
 */
vector<string>& SubutaiResponse::getConfPoints()
{					//getting ips vector

    return this->confPoints;
}
