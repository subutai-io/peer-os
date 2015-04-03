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
#include "SubutaiCommand.h"
/**
 *  \details   Default constructor of SubutaiCommand class.
 */
SubutaiCommand::SubutaiCommand() :
	_isDaemon(false), requestSequenceNumber(-1), pid(-1), timeout(5)
{
	//Setting default values..
	this->setType("");
	this->setCommand("");
	this->setWorkingDirectory("");
	this->setUuid("");
	this->setPid(-1);
	this->setRequestSequenceNumber(-1);
	this->setStandardError("");
	this->setStandardOutput("");
	this->setRunAs("");
	this->setTimeout(30);
	this->getArguments().clear();
	this->getEnvironment().clear();
	this->setCommandId("");
	this->setMacAddress("");
	this->setHostname("");
	this->getIps().clear();
}

/**
 *  \details   Default destructor of SubutaiCommand class.
 */
SubutaiCommand::~SubutaiCommand() {
	// TODO Auto-generated destructor stub
}

/**
 *  \details   This method clears given instance's all private and public variables.
 */
void SubutaiCommand::clear() {		//clear the all variables..
	this->setType("");
	this->setCommand("");
	this->setWorkingDirectory("");
	this->setUuid("");
	this->setPid(-1);
	this->setRequestSequenceNumber(-1);
	this->setStandardError("");
	this->setStandardOutput("");
	this->setRunAs("");
	this->setTimeout(30);
	this->getArguments().clear();
	this->getEnvironment().clear();
	this->setCommandId("");
	this->setMacAddress("");
	this->setHostname("");
	this->getIps().clear();
}

/**
 *  \details   deserialize function deserialize the given Json strings to SubutaiCommand instance.
 *     		   This is one of the most frequently used function is the class.
 *  		   It also check the existing variable in the JSON strings when deserializing the instance.
 *  		   it uses reference input and deserialize it to called SubutaiCommand instance
 *  		   it returns true if the given input string is true formatted otherwise return false.
 */
bool SubutaiCommand::deserialize(string& input) {
#if USE_PROTOBUF
	// Protobuffers implementation
	Subutai::Request request;
	if (request.ParseFromString(input)) {
		if (request.has_type()) {
			switch (request.type()) {
				case Subutai::Request::EXECUTE_REQUEST:
				this->setType("EXECUTE_REQUEST");
				break;
				case Subutai::Request::TERMINATE_REQUEST:
				this->setType("TERMINATE_REQUEST");
				break;
				case Subutai::Request::PS_REQUEST:
				this->setType("PS_REQUEST");
				break;
				case Subutai::Request::SET_INOTIFY_REQUEST:
				this->setType("SET_INOTIFY_REQUEST");
				break;
				case Subutai::Request::UNSET_INOTIFY_REQUEST:
				this->setType("UNSET_INOTIFY_REQUEST");
				break;
				case Subutai::Request::LIST_INOTIFY_REQUEST:
				this->setType("LIST_INOTIFY_REQUEST");
				break;
				default:
				break;
			}
		}
		if (request.has_id()) {
			this->setUuid(request.id());
		}
		if (request.has_commandid()) {
			this->setCommandId(request.commandid());
		}
		if (request.has_workingdirectory()) {
			this->setWorkingDirectory(request.workingdirectory());
		}
		if (request.has_command()) {
			this->setCommand(request.command());
		}
		vector<string> args;
		for (int i = 0; i < request.args_size(); i++) {
			args.push_back(request.args(i));
		}
		for (int i = 0; i < request.environment_size(); i++) {
			const Subutai::Request::env& rEnvironment = request.environment(i);
			pair<string, string> env_vars;
			env_vars.first = rEnvironment.key();
			env_vars.second = rEnvironment.value();
			this->environment.push_back(env_vars);
		}
		if (request.has_stdout()) {
			switch (request.stdout()) {
				case Subutai::Request::NO:
				this->setStandardOutput("NO");
				break;
				default:
				this->setStandardOutput("RETURN");
				break;
			}
		}
		if (request.has_stderr()) {
			switch (request.stderr()) {
				case Subutai::Request::NO:
				this->setStandardError("NO");
				break;
				default:
				this->setStandardError("RETURN");
				break;
			}
		}
		if (request.has_runas()) {
			this->setRunAs(request.runas());
		}
		if (request.has_timeout()) {
			this->setTimeout(request.timeout());
		}
		if (request.has_isdaemon()) {
			this->setIsDaemon(request.isdaemon());
		}
	} else {
		return false;
	}
#else
	//Deserialize a Json String to Command instance
	Json::Reader reader;//return true Deserialize operation is successfully done
	Json::Value root;
	pair<string, string> dummy;
	Json::FastWriter writer;

	try {
		clear(); //clear all arguments firstly..
		bool parsedNumberSuccess = checkCommandString(input);
		bool parsedSuccess = reader.parse(input, root, false);

		if (!parsedSuccess || !parsedNumberSuccess) {
			cout << "Failed to parse JSON" << endl
					<< reader.getFormatedErrorMessages() << endl;
			cout << "Failed Message: " << input << endl;
			return false; //error in parsing Json
		}

		if (!root["request"]["type"].isNull()) {
			this->setType(root["request"]["type"].asString());
		}
		if (!root["request"]["stdOut"].isNull()) {
			this->setStandardOutput(root["request"]["stdOut"].asString());//initialize standardOutput parameter if it is not null
		}
		if (!root["request"]["stdErr"].isNull()) {
			this->setStandardError(root["request"]["stdErr"].asString());//initialize standardError parameter if it is not null
		}
		if (!root["request"]["id"].isNull()) {
			this->setUuid(root["request"]["id"].asString());//initialize UUID parameter if it is not null
		}
		if (!root["request"]["pid"].isNull()) {
			this->setPid(root["request"]["pid"].asInt());//initialize pid parameter if it is not null
		}
		if (!root["request"]["workingDirectory"].isNull()) {
			this->setWorkingDirectory(
					root["request"]["workingDirectory"].asString());//initialize workingDirectory parameter if it is not null
		}
		if (!root["request"]["isDaemon"].isNull()) {
			this->setIsDaemon(root["request"]["isDaemon"].asInt());
		}
		if (!root["request"]["command"].isNull()) {
			this->setCommand(root["request"]["command"].asString());//initialize program parameter if it is not null
		}
		if (!root["request"]["runAs"].isNull()) {
			setRunAs(root["request"]["runAs"].asString());//initialize runAs parameter if it is not null
		}
		if (!root["request"]["timeout"].isNull()) {
			this->setTimeout(root["request"]["timeout"].asInt());//initialize runAs parameter if it is not null
		}
		Json::Value::Members members =
				root["request"]["environment"].getMemberNames();//get environment members

		for (unsigned int index = 0; index < members.size(); index++)//set Env path pairs
				{
			dummy.first = members[index].c_str();
			dummy.second =
					root["request"]["environment"][members[index].c_str()].asString();
			this->environment.push_back(dummy);
		}

		string arg;
		for (unsigned int index = 0; index < root["request"]["args"].size();
				index++)	//set arguments
				{
			arg = root["request"]["args"][index].asString();
			this->getArguments().push_back(arg);
		}
		if (!root["request"]["commandId"].isNull()) {
			setCommandId(root["request"]["commandId"].asString());//initialize taskUuid parameter if it is not null
		}
		if (!root["request"]["hostname"].isNull()) {
			setHostname(root["request"]["hostname"].asString());//initialize hostname parameter if it is not null
		}
		if (!root["request"]["macAddress"].isNull()) {
			setMacAddress(root["request"]["macAddress"].asString());//initialize macAddress parameter if it is not null
		}
		arg.clear();
		for (unsigned int index = 0; index < root["request"]["ips"].size();
				index++)	//set ips
				{
			arg = root["request"]["ips"][index].asString();
			this->getIps().push_back(arg);
		}
		string arg1;
		arg1.clear();
		watchArgs.clear();
		for (unsigned int index = 0;
				index < root["request"]["configPoints"].size(); index++)//set arguments
				{
			arg1 = root["request"]["configPoints"][index].asString();
			this->getWatchArguments().push_back(arg1);
		}

		input = writer.write(root);
		return true;
	} catch (exception e) {
		cout << e.what() << endl;
	}
#endif

	return true;
}

/**
 *  \details   getting "environment" private variable of SubutaiCommand instance
 */
list<pair<string, string> >& SubutaiCommand::getEnvironment() {	//getting EnvPath
	return this->environment;
}

/**
 *  \details   setting "environment" private variable of SubutaiCommand instance
 *  		   environment parameter is set and used in execution.
 */
void SubutaiCommand::setEnvironment(list<pair<string, string> >& envr) {//setting EnvPath
	this->environment.clear();
	pair<string, string> dummy;

	for (std::list<pair<string, string> >::iterator it = envr.begin();
			it != envr.end(); it++) {
		dummy.first = it->first.c_str();
		dummy.second = it->second.c_str();
		this->environment.push_back(dummy);
	}
}

/**
 *  \details   getting "pid" private variable of SubutaiCommand instance
 */
int SubutaiCommand::getPid() {
	return this->pid;
}

int SubutaiCommand::getIsDaemon() {
	return this->_isDaemon;
}

/**
 *  \details   setting "pid" private variable of SubutaiCommand instance.
 *  		   It carries the process id of the execution.
 */
void SubutaiCommand::setPid(int pid) {
	this->pid = pid;
}

/**
 *  \details   setting "uuid" private variable of SubutaiCommand instance
 */
void SubutaiCommand::setUuid(const string& uu_id) {				//setting UUid
	this->uuid = uu_id;
}

/**
 *  \details   getting "uuid" private variable of SubutaiCommand instance.
 *  		   It is command specific uuid parameter.
 *  		   It should be match with KiskisAgent static uuid.
 */
string& SubutaiCommand::getUuid() {								//getting UUid
	return this->uuid;
}

/**
 *  \details   setting "args" private vector variable of SubutaiCommand instance.
 *  		   This is the list of arguments vector that is used in execution.
 */
void SubutaiCommand::setArguments(vector<string> myvector) {//setting Argument vector

	for (unsigned int index = 0; index < myvector.size(); index++) {
		this->args.push_back(myvector[index]);
	}
}

/**
 *  \details   getting "args" private vector variable of SubutaiCommand instance.
 *  		   This is the list of arguments vector that is used in execution.
 */
vector<string>& SubutaiCommand::getArguments() {

	return this->args;
}

/**
 *  \details   getting "workingDirectory" private variable of SubutaiCommand instance.
 */
string& SubutaiCommand::getWorkingDirectory() {	//getting Current Working directory
	return this->workingDirectory;
}

/**
 *  \details   setting "workingDirectory" private variable of SubutaiCommand instance.
 */
void SubutaiCommand::setWorkingDirectory(const string& workingdirectory) {//setting Current Working directory
	this->workingDirectory = workingdirectory;
}

/**
 *  \details   getting "program" private variable of SubutaiCommand instance.
 */
string& SubutaiCommand::getCommand() {					//getting Command path
	return this->program;
}

/**
 *  \details   setting "program" private variable of SubutaiCommand instance.
 *  		   This variable is an absolute program path.
 *  		   For instance: "/bin/ls" or "/usr/bin/tail"
 */
void SubutaiCommand::setCommand(const string& mycommand) {//setting Command path
	this->program = mycommand;
}

/**
 *  \details   getting "requestSequenceNumber" private variable of SubutaiCommand instance.
 */
int SubutaiCommand::getRequestSequenceNumber() {		//getting RequestSeqnum
	return this->requestSequenceNumber;
}

/**
 *  \details   setting "requestSequenceNumber" private variable of SubutaiCommand instance.
 *  		   This variable holds the sequenceNumber of the SubutaiCommand instance.
 */
void SubutaiCommand::setRequestSequenceNumber(int requestSequenceNumber) {//setting RequestSeqnum
	this->requestSequenceNumber = requestSequenceNumber;
}

/**
 *  \details   getting "runAs" private variable of SubutaiCommand instance.
 */
string& SubutaiCommand::getRunAs() {						//getting runAs
	return this->runAs;
}

/**
 *  \details   setting "runAs" private variable of SubutaiCommand instance.
 *  		   This is the user of execution.
 *  		   It should be: "root", "Alex" , "Emin" etc..
 */
void SubutaiCommand::setRunAs(const string& runAs) {		//setting runAs
	this->runAs = runAs;
}

/**
 *  \details   getting "stdErr" private variable of SubutaiCommand instance.
 */
string& SubutaiCommand::getStandardError() {			//getting standard err
	return this->stdErr;
}

/**
 *  \details   setting "stdErr" private variable of SubutaiCommand instance.
 *  			It has the mode of Error.
 *  			it Should be: "CAPTURE", "CAPTURE_AND_RETURN" ,"RETURN" , "NO"
 */
void SubutaiCommand::setStandardError(const string& mystderr) {	//setting standard err
	this->stdErr = mystderr;
}

/**
 *  \details   getting "stdOut" private variable of SubutaiCommand instance.
 */
string& SubutaiCommand::getStandardOutput() {			//getting standard out
	return this->stdOut;
}

/**
 *  \details   setting "stdOut" private variable of SubutaiCommand instance.
 *  			It has the mode of Output.
 *  			it Should be: "CAPTURE", "CAPTURE_AND_RETURN" ,"RETURN" , "NO"
 */
void SubutaiCommand::setStandardOutput(const string& mystdout) { //setting standard out
	this->stdOut = mystdout;
}

/**
 *  \details   getting "type" private variable of SubutaiCommand instance.
 */
string& SubutaiCommand::getType() {						//getting command type
	return this->type;
}

/**
 *  \details   setting "type" private variable of SubutaiCommand instance.
 *  		   This holds the type of command.
 *  		   it should be: "EXECUTE_REQUEST" or "REGISTRATION_REQUEST_DONE" or "HEARTBEAT_REQUEST" or "TERMINATE_REQUEST"
 */
void SubutaiCommand::setType(const string& mytype) { 	//setting command type
	this->type = mytype;
}

void SubutaiCommand::setIsDaemon(int isDaemon) {
	this->_isDaemon = isDaemon;
}

/**
 *  \details   getting "timeout" private variable of SubutaiCommand instance.
 */
int SubutaiCommand::getTimeout() {
	return this->timeout;
}

/**
 *  \details   setting "timeout" private variable of SubutaiCommand instance.
 *  		   This holds the timeout value of the command.
 *  		   Command is terminated due to this variable value default:30 seconds
 */
void SubutaiCommand::setTimeout(int timeout) {
	this->timeout = timeout;
}

/**
 *  \details   getting "hostname" private variable of SubutaiCommand instance.
 */
string& SubutaiCommand::getHostname() {
	return this->hostname;
}

/**
 *  \details   setting "hostname" private variable of SubutaiCommand instance.
 *  		   This holds the hostname of the agent machine
 */
void SubutaiCommand::setHostname(const string& hostname) {
	this->hostname = hostname;
}

/**
 *  \details   getting "macAddress" private variable of SubutaiCommand instance.
 */
string& SubutaiCommand::getMacAddress() {
	return this->macAddress;
}

/**
 *  \details   setting "macAddress" private variable of SubutaiCommand instance.
 *  		   This holds the macAddress(eth0) of the agent machine
 */
void SubutaiCommand::setMacAddress(const string& macAddress) {
	this->macAddress = macAddress;
}

/**
 *  \details   getting "taskUuid" private variable of SubutaiCommand instance.
 */
string& SubutaiCommand::getCommandId() {
	return this->taskUuid;
}

/**
 *  \details   setting "taskUuid" private variable of SubutaiCommand instance.
 *  		   This holds the task uuid of the command
 */
void SubutaiCommand::setCommandId(const string& taskuuid) {
	this->taskUuid = taskuuid;
}

/**
 *  \details   setting "ips" private vector variable of SubutaiCommand instance.
 *  		   This is the list of ips vector that holds the ip addresses of the machine
 */
void SubutaiCommand::setIps(vector<string> myvector) {		//setting ips vector

	for (unsigned int index = 0; index < myvector.size(); index++) {
		this->ips.push_back(myvector[index]);
	}
}

/**
 *  \details   getting "ips" private vector variable of SubutaiCommand instance.
 */
vector<string>& SubutaiCommand::getIps() {					//getting ips vector

	return this->ips;
}

/**
 *  \details   getting "watchArgs" private vector variable of SubutaiCommand instance.
 *  		   This is the list of arguments vector that is used in execution.
 */
vector<string>& SubutaiCommand::getWatchArguments() {
	return this->watchArgs;
}

/**
 *  \details   checking the number of curly braces in the command string
 *
 */
bool SubutaiCommand::checkCommandString(const string& input) {
	unsigned int leftBrace = 0;
	unsigned int rightBrace = 0;
	for (unsigned int i = 0; i < input.length(); i++) {
		if (input[i] == '{')
			leftBrace++;
		if (input[i] == '}')
			rightBrace++;
	}
	if (rightBrace == leftBrace)
		return true;
	else
		return false;
}
