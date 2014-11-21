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
    setCommandId("");
    setHostname("");
    setParentHostname("");
    getInterfaces().clear();
    getContainerSet().clear();
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
    setCommandId("");
    setHostname("");
    setParentHostname("");
    getInterfaces().clear();
    setUuid("");
    setconfigPoint("");
    setDateTime("");
    setChangeType("");
    setExitCode(-1);
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
#if USE_PROTOBUF
    Subutai::Response response;
    if (!this->getType().empty()) {
        if (this->getType() == "HEARTBEAT") {
            response.set_type(Subutai::Response::HEARTBEAT);
        } else if (this->getType() == "EXECUTE_RESPONSE") {
            response.set_type(Subutai::Response::EXECUTE_RESPONSE);
        } else if (this->getType() == "EXECUTE_TIMEOUT") {
            response.set_type(Subutai::Response::EXECUTE_RESPONSE);
        } else if (this->getType() == "IN_QUEUE") {
            response.set_type(Subutai::Response::IN_QUEUE);
        } else if (this->getType() == "TERMINATE_RESPONSE") {
            response.set_type(Subutai::Response::TERMINATE_RESPONSE);
        } else if (this->getType() == "PS_RESPONSE") { 
            response.set_type(Subutai::Response::PS_RESPONSE);
        } else if (this->getType() == "SET_INOTIFY_RESPONSE") {
            response.set_type(Subutai::Response::SET_INOTIFY_RESPONSE);
        } else if (this->getType() == "UNSET_INOTIFY_RESPONSE") {
            response.set_type(Subutai::Response::UNSET_INOTIFY_RESPONSE);
        } else if (this->getType() == "LIST_INOTIFY_RESPONSE") {
            response.set_type(Subutai::Response::LIST_INOTIFY_RESPONSE);
        } else if (this->getType() == "INOTIFY_EVENT") {
            response.set_type(Subutai::Response::INOTIFY_EVENT);
        }
    }
    if (!this->getHostname().empty()) {
        response.set_hostname(this->getHostname());
    }
    if (!this->getUuid().empty()) {
        response.set_id(this->getUuid());
    }
    if (this->containers.size() > 0) {
        for (vector<SubutaiContainer>::iterator it = this->containers.begin(); it != this->containers.end(); it++) {
        /*  root["response"]["containers"][index]["hostname"]	= this->containers[index].getContainerHostnameValue();
        root["response"]["containers"][index]["id"]		= this->containers[index].getContainerIdValue();
        root["response"]["containers"][index]["status"]		= this->containers[index].getContainerStatus();
        vector<string> ipValues	=	this->containers[index].getContainerIpValue();
        
        for(unsigned int i=0; i < ipValues.size(); i++) {
            root["response"]["containers"][index]["ips"][i]=ipValues[i];
        }*/
            Subutai::Response::Container* cont = response.add_containers();
            cont->set_hostname((*it).getContainerHostnameValue());
            cont->set_id((*it).getContainerIdValue());
            if ((*it).getContainerStatus() == "RUNNING") {
                cont->set_status(Subutai::Response::RUNNING);
            } else if ((*it).getContainerStatus() == "FREEZED") {
                cont->set_status(Subutai::Response::FREEZED);
            } else if ((*it).getContainerStatus() == "STOPPED") {
                cont->set_status(Subutai::Response::STOPPED);
            }
            /*vector<string> ipValues = (*it).getContainerIpValue();
            for (unsigned int i = 0; i < ipValues.size(); i++) {
            }*/
        }
    }
    /*  if (this->getIpValue().size() > 0) {
        for (vector<string>::iterator it = this->ips.begin(); it != this->ips.end(); it++) {
            string* ip = response.add_ips();
        }   
        // TODO: Add ips and macs after Ceren implement em
    }*/
    if (!this->getCommandId().empty()) {
        response.set_commandid(this->getCommandId());
    }
    if (this->getPid() >= 0) {
        response.set_pid(this->getPid());
    }
    if (this->getResponseSequenceNumber() >= 0) {
        response.set_responsenumber(this->getResponseSequenceNumber());
    }
    if (!this->getStandardOutput().empty()) {
        response.set_stdout(this->getStandardOutput());
    }
    if (!this->getStandardError().empty()) {
        response.set_stderr(this->getStandardError());
    }
    if (this->getExitCode() >= 0) {
        response.set_exitcode(this->getExitCode());
    }
    if (this->getConfPoints().size() > 0) {
        
    }

#else
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
    if(this->getExitCode() >= 0)
    {
    	if(this->getType()!= "INOTIFY_EVENT")
    		root["response"]["exitCode"] = this->getExitCode();										//check the pid is assigned or not
    }
    if(this->getResponseSequenceNumber() >= 0)										//check the responseSequenceNumber is assigned or not
    {
        root["response"]["responseNumber"] = this->getResponseSequenceNumber();
    }
    for(unsigned int index=0; index < this->getInterfaces().size(); index++)
	{
		root["response"]["interfaces"][index]["interfaceName"]=this->getInterfaces()[index].name;
		root["response"]["interfaces"][index]["ip"]=this->getInterfaces()[index].ip;
		root["response"]["interfaces"][index]["mac"]=this->getInterfaces()[index].mac;
	}

    if(!(this->getCommandId().empty()))											//check the taskuuid is assigned or not
    {
        root["response"]["commandId"] = this->getCommandId();
    }
    if(!(this->getHostname().empty()))											//check the hostname is assigned or not
    {
        root["response"]["hostname"] = this->getHostname();
    }
    if(!(this->getParentHostname().empty()))											//check the parenthostname is assigned or not
    {
        root["response"]["parentHostName"] = this->getParentHostname();
    }
    if(!(this->getconfigPoint().empty()))
    {
    	if(this->getType() == "INOTIFY_EVENT") root["response"]["configPoint"] = this->getconfigPoint();
    	else root["response"]["configPoints"] = this->getconfigPoint();
    }
    if(!(this->getDateTime().empty()))
    {
        root["response"]["dateTime"] = this->getDateTime();
    }
    if(!(this->getChangeType().empty()))
    {
        root["response"]["eventType"] = this->getChangeType();
    }

    if(this->getType()!= "INOTIFY_EVENT")
    {
		for(unsigned int index = 0; index < this->containers.size(); index++) {
			root["response"]["containers"][index]["hostname"]	= this->containers[index].getContainerHostnameValue();
			root["response"]["containers"][index]["id"]		= this->containers[index].getContainerIdValue();
			root["response"]["containers"][index]["status"]		= this->containers[index].getContainerStatus();
			vector<Interface> interfaceValues	=	this->containers[index].getContainerInterfaceValues();
			for(unsigned int i=0; i < interfaceValues.size(); i++) {
				root["response"]["containers"][index]["interfaces"][i]["interfaceName"]=interfaceValues[i].name;
				root["response"]["containers"][index]["interfaces"][i]["ip"]=interfaceValues[i].ip;
				root["response"]["containers"][index]["interfaces"][i]["mac"]=interfaceValues[i].mac;
			}
		}
    }
    for(unsigned int index = 0; index < this->getConfPoints().size(); index++) {
        if (this->getType() == "LIST_INOTIFY_RESPONSE") {
            root["response"]["configPoints"][index]=this->getConfPoints()[index];
        } else if (this->getType() == "INOTIFY_EVENT") {
            root["response"]["configPoint"][index]=this->getConfPoints()[index];
        }
    }
    output = writer.write(root);
#endif
}

/**
 *  \details   serializeDone method serialize the Done response JSon string from called instance.
 *     		   This is one of the most frequently used function is the class.
 *     		   It also check the existing variable(NULL or not) when serializing the instance.
 *  		   It returns given reference output strings.
 */
void SubutaiResponse::serializeDone(string& output)
{			//Serialize a Done Response  to a Json String
#if USE_PROTOBUF
    Subutai::Response response;
    if (!this->getType().empty()) {
        if (this->getType() == "HEARTBEAT") {
            response.set_type(Subutai::Response::HEARTBEAT);
        } else if (this->getType() == "EXECUTE_RESPONSE") {
            response.set_type(Subutai::Response::EXECUTE_RESPONSE);
        } else if (this->getType() == "EXECUTE_TIMEOUT") {
            response.set_type(Subutai::Response::EXECUTE_RESPONSE);
        } else if (this->getType() == "IN_QUEUE") {
            response.set_type(Subutai::Response::IN_QUEUE);
        } else if (this->getType() == "TERMINATE_RESPONSE") {
            response.set_type(Subutai::Response::TERMINATE_RESPONSE);
        } else if (this->getType() == "PS_RESPONSE") { 
            response.set_type(Subutai::Response::PS_RESPONSE);
        } else if (this->getType() == "SET_INOTIFY_RESPONSE") {
            response.set_type(Subutai::Response::SET_INOTIFY_RESPONSE);
        } else if (this->getType() == "UNSET_INOTIFY_RESPONSE") {
            response.set_type(Subutai::Response::UNSET_INOTIFY_RESPONSE);
        } else if (this->getType() == "LIST_INOTIFY_RESPONSE") {
            response.set_type(Subutai::Response::LIST_INOTIFY_RESPONSE);
        } else if (this->getType() == "INOTIFY_EVENT") {
            response.set_type(Subutai::Response::INOTIFY_EVENT);
        }
    }
    if (!this->getUuid().empty()) {
        response.set_id(this->getUuid());
    }
    if (!this->getCommandId().empty()) {
        response.set_commandid(this->getCommandId());
    }
    if (this->getPid() >= 0) {
        response.set_pid(this->getPid());
    }
    if (this->getResponseSequenceNumber() >= 0) {
        response.set_responsenumber(this->getResponseSequenceNumber());
    }
    if (this->getExitCode() >= 0) {
        response.set_exitcode(this->getExitCode());
    }
    if (this->getConfPoints().size() > 0) {
        
    }

#else
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
        root["response"]["id"] = this->getUuid();
    }
    if(this->getRequestSequenceNumber() >= 0)											//check the requestSequenceNumber is assigned or not
    {
        root["response"]["requestNumber"] = this->getRequestSequenceNumber();
    }
    if(this->getResponseSequenceNumber() >= 0)										//check the responseSequenceNumber is assigned or not
    {
        root["response"]["responseNumber"] = this->getResponseSequenceNumber();
    }
    if(this->getPid() >= 0)
    {
        root["response"]["pid"] = this->getPid();										//check the pid is assigned or not
    }
    if(this->getExitCode() >= 0)
    {
        root["response"]["exitCode"] = this->getExitCode();
    }
    if(!(this->getCommandId().empty()))											//check the taskuuid is assigned or not
    {
        root["response"]["commandId"] = this->getCommandId();
    }
    output = writer.write(root);	//Json Response Done string is created
#endif
}

/**
 *  \details   Add new interface to interfaces
 *
 */

void SubutaiResponse::addInterface(string name, string ip , string mac){
	Interface i;
	i.name = name;
	i.ip = ip;
	i.mac = mac;
	this->interfaces.push_back(i);
}


/**
 *  \details   getting "interfaces" private vector variable of SubutaiResponse instance.
 *  		   This is the list of interfaces vector that holds the ip address, name and mac address of each interface of the machine
 */
vector<Interface> SubutaiResponse::getInterfaces()
{
    return this->interfaces;
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
 *  \details   setting "hostname" private variable of SubutaiResponse instance.
 *  		   This holds the hostname of the agent machine
 */
void SubutaiResponse::setHostname(const string& hostname)
{
    this->hostname = hostname;
}


/**
 *  \details   getting "taskUuid" private variable of SubutaiResponse instance.
 */
string& SubutaiResponse::getCommandId()
{
    return this->commandId;
}

/**
 *  \details   setting "taskUuid" private variable of SubutaiResponse instance.
 *  		   This holds the task uuid of the command
 */
void SubutaiResponse::setCommandId(const string& commandid)
{
    this->commandId = commandid;
}

/**
 *  \details   setting "ips" private vector variable of SubutaiResponse instance.
 *  		   This is the list of ips vector that holds the ip addresses of the machine
 */
void SubutaiResponse::setInterfaces(vector<Interface> myvector)
{
    this->interfaces.clear();
    for(unsigned int index=0 ; index< myvector.size(); index++)
    {
        this->interfaces.push_back(myvector[index]);
    }
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

/**
 *  \details   getting "containers" private vector variable of SubutaiResponse instance.
 */
vector<SubutaiContainer>& SubutaiResponse::getContainerSet()
{
	return this->containers;
}
