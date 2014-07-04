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
#include "KACommand.h"
/**
 *  \details   Default constructor of KACommand class.
 */
KACommand::KACommand()
{			//Setting default values..
	// TODO Auto-generated constructor stub
	this->setType("");
	this->setProgram("");
	this->setWorkingDirectory("");
	this->setUuid("");
	this->setPid(-1);
	this->setRequestSequenceNumber(-1);
	this->setStandardError("");
	this->setStandardErrPath("");
	this->setStandardOutput("");
	this->setStandardOutPath("");
	this->setRunAs("");
	this->setTimeout(30);
	this->getArguments().clear();
	this->getEnvironment().clear();
	this->setTaskUuid("");
	this->setMacAddress("");
	this->setHostname("");
	this->getIps().clear();
	this->setSource("");
}

/**
 *  \details   Default destructor of KACommand class.
 */
KACommand::~KACommand()
{
	// TODO Auto-generated destructor stub
}

/**
 *  \details   This method clears given instance's all private and public variables.
 */
void KACommand::clear()
{		//clear the all variables..
	this->setType("");
	this->setProgram("");
	this->setWorkingDirectory("");
	this->setUuid("");
	this->setPid(-1);
	this->setRequestSequenceNumber(-1);
	this->setStandardError("");
	this->setStandardErrPath("");
	this->setStandardOutput("");
	this->setStandardOutPath("");
	this->setRunAs("");
	this->setTimeout(30);
	this->getArguments().clear();
	this->getEnvironment().clear();
	this->setTaskUuid("");
	this->setMacAddress("");
	this->setHostname("");
	this->getIps().clear();
	this->setSource("");
}

/**
 *  \details   deserialize function deserialize the given Json strings to KACommand instance.
 *     		   This is one of the most frequently used function is the class.
 *  		   It also check the existing variable in the JSON strings when deserializing the instance.
 *  		   it uses reference input and deserialize it to called KAcommand instance
 *  		   it returns true if the given input string is true formatted otherwise return false.
 */
bool KACommand::deserialize(string& input)
{														//Deserialize a Json String to Command instance
	Json::Reader reader;								//return true Deserialize operation is successfully done
	Json::Value root;
	pair <string,string> dummy;
	Json::FastWriter writer;

	try
	{
		clear(); //clear all arguments firstly..
		bool parsedNumberSuccess = checkCommandString(input);
		bool parsedSuccess = reader.parse(input,root,true);		//parsing Json String

		if(!parsedSuccess || !parsedNumberSuccess)	//if it is not successfull
		{
			cout<<"Failed to parse JSON"<<endl<<reader.getFormatedErrorMessages()<<endl;
			cout <<"Failed Message: " << input << endl;
			return false; //error in parsing Json
		}

		if(!root["command"]["type"].isNull())
		{
			this->setType(root["command"]["type"].asString());
		}
		if(!root["command"]["stdOut"].isNull())
		{
			this->setStandardOutput(root["command"]["stdOut"].asString());		//initialize standardOutput parameter if it is not null
		}
		if(!root["command"]["stdOutPath"].isNull())
		{
			this->setStandardOutPath(root["command"]["stdOutPath"].asString());		//initialize standardOutpath parameter if it is not null
		}
		if(!root["command"]["stdErr"].isNull())
		{
			this->setStandardError(root["command"]["stdErr"].asString());		//initialize standardError parameter if it is not null
		}
		if(!root["command"]["stdErrPath"].isNull())
		{
			this->setStandardErrPath(root["command"]["stdErrPath"].asString());		//initialize standardError parameter if it is not null
		}
		if(!root["command"]["uuid"].isNull())
		{
			this->setUuid(root["command"]["uuid"].asString());				//initialize UUID parameter if it is not null
		}
		if(!root["command"]["pid"].isNull())
		{
			this->setPid(root["command"]["pid"].asInt());					//initialize pid parameter if it is not null
		}
		if(!root["command"]["workingDirectory"].isNull())
		{
			this->setWorkingDirectory(root["command"]["workingDirectory"].asString());		//initialize workingDirectory parameter if it is not null
		}
		if(!root["command"]["requestSequenceNumber"].isNull())
		{
			this->setRequestSequenceNumber(root["command"]["requestSequenceNumber"].asInt()); //initialize requestSequenceNumber parameter if it is not null
		}
		if(!root["command"]["program"].isNull())
		{
			this->setProgram(root["command"]["program"].asString());		//initialize program parameter if it is not null
		}
		if(!root["command"]["runAs"].isNull())
		{
			setRunAs(root["command"]["runAs"].asString());		//initialize runAs parameter if it is not null
		}
		if(!root["command"]["timeout"].isNull())
		{
			this->setTimeout(root["command"]["timeout"].asInt());		//initialize runAs parameter if it is not null
		}
		Json::Value::Members members = root["command"]["environment"].getMemberNames();		//get environment members

		for(unsigned int index=0; index < members.size(); index++)	//set Env path pairs
		{
			dummy.first = members[index].c_str();
			dummy.second = root["command"]["environment"][members[index].c_str()].asString();
			this->environment.push_back(dummy);
		}

		string arg;
		for(unsigned int index=0; index < root["command"]["args"].size(); index++)	//set arguments
		{
			arg =  root["command"]["args"][index].asString();
			this->getArguments().push_back(arg);
		}
		if(!root["command"]["taskUuid"].isNull())
		{
			setTaskUuid(root["command"]["taskUuid"].asString());		//initialize taskUuid parameter if it is not null
		}
		if(!root["command"]["hostname"].isNull())
		{
			setHostname(root["command"]["hostname"].asString());		//initialize hostname parameter if it is not null
		}
		if(!root["command"]["macAddress"].isNull())
		{
			setMacAddress(root["command"]["macAddress"].asString());		//initialize macAddress parameter if it is not null
		}
		arg.clear();
		for(unsigned int index=0; index < root["command"]["ips"].size(); index++)	//set ips
		{
			arg =  root["command"]["ips"][index].asString();
			this->getIps().push_back(arg);
		}
		if(!root["command"]["source"].isNull())
		{
			setSource(root["command"]["source"].asString());		//initialize hostname parameter if it is not null
		}
		string arg1;
		arg1.clear();
		watchArgs.clear();
		for(unsigned int index=0; index < root["command"]["confPoints"].size(); index++)	//set arguments
		{
			arg1 =  root["command"]["confPoints"][index].asString();
			this->getWatchArguments().push_back(arg1);
		}

		input = writer.write(root);
		return true;
	}
	catch(exception e)
	{
		cout << e.what() << endl;
	}
}

/**
 *  \details   getting "envioronment" private variable of KACommand instance
 */
list<pair<string,string> >& KACommand::getEnvironment()
{					//getting EnvPath
	return this->environment;
}

/**
 *  \details   setting "envioronment" private variable of KACommand instance
 *  		   environment parameter is set and used in execution.
 */
void KACommand::setEnvironment(list<pair<string,string> >& envr)
{			//setting EnvPath
	this->environment.clear();
	pair <string,string> dummy;

	for (std::list<pair<string,string> >::iterator it = envr.begin(); it != envr.end(); it++ )
	{
		dummy.first = it->first.c_str();
		dummy.second = it->second.c_str();
		this->environment.push_back(dummy);
	}
}

/**
 *  \details   getting "pid" private variable of KACommand instance
 */
int KACommand::getPid()
{
	return this->pid;
}

/**
 *  \details   setting "pid" private variable of KACommand instance.
 *  		   It carries the process id of the execution.
 */
void KACommand::setPid(int pid)
{
	this->pid=pid;
}

/**
 *  \details   setting "uuid" private variable of KACommand instance
 */
void KACommand::setUuid(const string& uu_id)
{					//setting UUid
	this->uuid = uu_id;
}

/**
 *  \details   getting "uuid" private variable of KACommand instance.
 *  		   It is command specific uuid parameter.
 *  		   It should be match with KiskisAgent static uuid.
 */
string& KACommand::getUuid()
{								//getting UUid
	return this->uuid;
}

/**
 *  \details   setting "args" private vector variable of KACommand instance.
 *  		   This is the list of arguments vector that is used in execution.
 */
void KACommand::setArguments(vector<string> myvector)
{		//setting Argument vector

	for(unsigned int index=0 ; index< myvector.size(); index++)
	{
		this->args.push_back(myvector[index]);
	}
}

/**
 *  \details   getting "args" private vector variable of KACommand instance.
 *  		   This is the list of arguments vector that is used in execution.
 */
vector<string>& KACommand::getArguments()
{

	return this->args;
}

/**
 *  \details   getting "workingDirectory" private variable of KACommand instance.
 */
string& KACommand::getWorkingDirectory()
{						//getting Current Working directory
	return this->workingDirectory;
}

/**
 *  \details   setting "workingDirectory" private variable of KACommand instance.
 */
void KACommand::setWorkingDirectory(const string& workingdirectory)
{			//setting Current Working directory
	this->workingDirectory = workingdirectory;
}

/**
 *  \details   getting "program" private variable of KACommand instance.
 */
string& KACommand::getProgram()
{					//getting Program path
	return this->program;
}

/**
 *  \details   setting "program" private variable of KACommand instance.
 *  		   This variable is an absolute program path.
 *  		   For instance: "/bin/ls" or "/usr/bin/tail"
 */
void KACommand::setProgram(const string& myprogram)
{	//setting Program path
	this->program = myprogram;
}

/**
 *  \details   getting "requestSequenceNumber" private variable of KACommand instance.
 */
int KACommand::getRequestSequenceNumber()
{					//getting RequestSeqnum
	return this->requestSequenceNumber;
}

/**
 *  \details   setting "requestSequenceNumber" private variable of KACommand instance.
 *  		   This variable holds the sequenceNumber of the KACommand instance.
 */
void KACommand::setRequestSequenceNumber(int requestSequenceNumber)
{	//setting RequestSeqnum
	this->requestSequenceNumber = requestSequenceNumber;
}

/**
 *  \details   getting "runAs" private variable of KACommand instance.
 */
string& KACommand::getRunAs()
{						//getting runAs
	return this->runAs;
}

/**
 *  \details   setting "runAs" private variable of KACommand instance.
 *  		   This is the user of execution.
 *  		   It should be: "root", "Alex" , "Emin" etc..
 */
void KACommand::setRunAs(const string& runAs)
{		//setting runAs
	this->runAs = runAs;
}

/**
 *  \details   getting "stdErr" private variable of KACommand instance.
 */
string& KACommand::getStandardError()
{						//getting standard err
	return this->stdErr;
}

/**
 *  \details   getting "stdErrPath" private variable of KACommand instance.
 */
string& KACommand::getStandardErrPath()
{					//getting standard errpath
	return this->stdErrPath;
}
/**
 *  \details   setting "stdErr" private variable of KACommand instance.
 *  			It has the mode of Error.
 *  			it Should be: "CAPTURE", "CAPTURE_AND_RETURN" ,"RETURN" , "NO"
 */
void KACommand::setStandardError(const string& mystderr)
{		//setting standard err
	this->stdErr = mystderr;
}

/**
 *  \details   setting "stdErrPath" private variable of KACommand instance.
 *  		   This variable holds the path and file name for capturing error responses
 */
void KACommand::setStandardErrPath(const string& mystderrpath)
{		//setting standard errpath
	this->stdErrPath=mystderrpath;
}
/**
 *  \details   getting "stdOut" private variable of KACommand instance.
 */
string& KACommand::getStandardOutput()
{						//getting standard out
	return this->stdOut;
}

/**
 *  \details   getting "stdOuthPath" private variable of KACommand instance.
 */
string& KACommand::getStandardOutputPath()
{					//getting standard outpath
	return this->stdOuthPath;
}
/**
 *  \details   setting "stdOut" private variable of KACommand instance.
 *  			It has the mode of Output.
 *  			it Should be: "CAPTURE", "CAPTURE_AND_RETURN" ,"RETURN" , "NO"
 */
void KACommand::setStandardOutput(const string& mystdout)
{ 	//setting standard out
	this->stdOut = mystdout;
}

/**
 *  \details   setting "stdOuthPath" private variable of KACommand instance.
 *  		   This variable holds the path and file name for capturing error responses
 */
void KACommand::setStandardOutPath(const string& mystdoutpath)
{		//setting standard outpath
	this->stdOuthPath=mystdoutpath;
}

/**
 *  \details   getting "type" private variable of KACommand instance.
 */
string& KACommand::getType()
{						//getting command type
	return this->type;
}

/**
 *  \details   setting "type" private variable of KACommand instance.
 *  		   This holds the type of command.
 *  		   it should be: "EXECUTE_REQUEST" or "REGISTRATION_REQUEST_DONE" or "HEARTBEAT_REQUEST" or "TERMINATE_REQUEST"
 */
void KACommand::setType(const string& mytype)
{ 		//setting command type
	this->type = mytype;
}

/**
 *  \details   getting "timeout" private variable of KACommand instance.
 */
int KACommand::getTimeout()
{
	return this->timeout;
}

/**
 *  \details   setting "timeout" private variable of KACommand instance.
 *  		   This holds the timeout value of the command.
 *  		   Command is terminated due to this variable value default:30 seconds
 */
void KACommand::setTimeout(int timeout)
{
	this->timeout = timeout;
}

/**
 *  \details   getting "hostname" private variable of KACommand instance.
 */
string& KACommand::getHostname()
{
	return this->hostname;
}

/**
 *  \details   setting "hostname" private variable of KACommand instance.
 *  		   This holds the hostname of the agent machine
 */
void KACommand::setHostname(const string& hostname)
{
	this->hostname = hostname;
}

/**
 *  \details   getting "macAddress" private variable of KACommand instance.
 */
string& KACommand::getMacAddress()
{
	return this->macAddress;
}

/**
 *  \details   setting "macAddress" private variable of KACommand instance.
 *  		   This holds the macAddress(eth0) of the agent machine
 */
void KACommand::setMacAddress(const string& macAddress)
{
	this->macAddress = macAddress;
}

/**
 *  \details   getting "taskUuid" private variable of KACommand instance.
 */
string& KACommand::getTaskUuid()
{
	return this->taskUuid;
}

/**
 *  \details   setting "taskUuid" private variable of KACommand instance.
 *  		   This holds the task uuid of the command
 */
void KACommand::setTaskUuid(const string& taskuuid)
{
	this->taskUuid = taskuuid;
}

/**
 *  \details   setting "ips" private vector variable of KACommand instance.
 *  		   This is the list of ips vector that holds the ip addresses of the machine
 */
void KACommand::setIps(vector<string> myvector)
{		//setting ips vector

	for(unsigned int index=0 ; index< myvector.size(); index++)
	{
		this->ips.push_back(myvector[index]);
	}
}

/**
 *  \details   getting "ips" private vector variable of KACommand instance.
 */
vector<string>& KACommand::getIps()
{					//getting ips vector

	return this->ips;
}

/**
 *  \details   getting "source" private variable of KACommand instance.
 */
string& KACommand::getSource()
{
	return this->source;
}

/**
 *  \details   setting "source" private variable of KACommand instance.
 *  		   This holds the task source information of the command
 */
void KACommand::setSource(const string& source)
{
	this->source = source;
}

/**
 *  \details   getting "watchArgs" private vector variable of KACommand instance.
 *  		   This is the list of arguments vector that is used in execution.
 */
vector<string>& KACommand::getWatchArguments()
{
	return this->watchArgs;
}

/**
 *  \details   checking the number of curly braces in the command string
 *
 */
bool KACommand::checkCommandString(const string& input)
{
	unsigned int leftBrace=0;
	unsigned int rightBrace=0;
	for(unsigned int i=0; i < input.length();i++)
	{
		if(input[i] == '{')
			leftBrace++;
		if(input[i] == '}')
			rightBrace++;
	}
	cout << "Right Braces: " << rightBrace << endl;
	cout << "Left Braces: " << leftBrace << endl;

	if(rightBrace==leftBrace)
		return true;
	else
		return false;
}
