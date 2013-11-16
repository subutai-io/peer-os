#include "KACommand.h"
/**
 *  \details   Default constructor of KACommand class.
 */
KACommand::KACommand()
{			//Setting default values..
	// TODO Auto-generated constructor stub
	this->setType("");
	this->setWorkingDirectory("");
	this->setUuid("");
	this->setPid("");
	this->setRequestSequenceNumber(-1);
	this->setStandardError("");
	this->setStandardErrPath("");
	this->setStandardOutput("");
	this->setStandardOutPath("");
	this->setRunAs("");
	this->setTimeout(60);
	this->getArguments().clear();
	this->getEnvironment().clear();
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
	this->setWorkingDirectory("");
	this->setUuid("");
	this->setPid("");
	this->setRequestSequenceNumber(-1);
	this->setStandardError("");
	this->setStandardErrPath("");
	this->setStandardOutput("");
	this->setStandardOutPath("");
	this->setRunAs("");
	this->setTimeout(60);
	this->getArguments().clear();
	this->getEnvironment().clear();
}
/**
 *  \details   serialize function creates a JSON strings from called instance.
 *  		   This is one of the most frequently used function is the class.
 *  		   It also check the existing variable(NULL or not) when serializing the instance.
 *  		   It returns given reference output strings.
 */
void KACommand::serialize(string& output)
{			//Serialize a Command instance to a Json String
	Json::Value env;
	Json::StyledWriter writer;
	Json::Value root;

	//mandatory arguments
	if((!this->getType().empty()))
		root["command"]["type"] = this->getType();
	if(!(this->getStandardOutput().empty()))
		root["command"]["stdOut"] = this->getStandardOutput();
	if(!(this->getStandardError().empty()))
		root["command"]["stdErr"] = this->getStandardError();
	if(!(this->getUuid().empty()))
		root["command"]["uuid"] = this->getUuid();
	if(!(this->getPid().empty()))
		root["command"]["pid"] = this->getPid();								//check the pid is assigned or not
	if(!(this->getWorkingDirectory().empty()))									//check the workingDirectory is assigned or not
		root["command"]["workingDirectory"] = this->getWorkingDirectory();
	if(this->getRequestSequenceNumber() >= 0)									//check the requestSequenceNumber is assigned or not
		root["command"]["requestSequenceNumber"] = this->getRequestSequenceNumber();
	if(!(this->getProgram().empty()))											//check the program is assigned or not
		root["command"]["program"] = this->getProgram();
	if(!(this->getRunAs().empty()))												//check the runAs is assigned or not
		root["command"]["runAs"] = this->getRunAs();
	if(!(this->getStandardErrPath().empty()))											//check the StandardErrPath is assigned or not
		root["command"]["stdErrPath"] = this->getStandardErrPath();
	if(!(this->getStandardOutputPath().empty()))											//check the StandardOutPath is assigned or not
		root["command"]["stdOutPath"] = this->getStandardOutputPath();
	if(this->getTimeout()!=60)											//check the TimeoutValue is assigned or not
		root["command"]["timeout"] = this->getTimeout();
	for(unsigned int index=0; index < this->getArguments().size(); index++)	//automatically check the size of the argument list
		root["command"]["args"][index]=this->getArguments()[index];
	if(this->getEnvironment().size() > 0)
	{
		//automatically check the size of the envirenment list
		for(std::list<pair<string,string> >::iterator it = this->getEnvironment().begin(); it != this->getEnvironment().end(); it++ )
		{
			env[it->first.c_str()] = it->second.c_str();	//adding env parameters to env Jsonstring
		}
		root["command"]["environment"]=env;	//envireonment is added to command Json
	}
	output = writer.write(root); 				//Json command string is created
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
	Json::FastWriter writer;							//return false if parsing error
	Json::Reader reader;								//return true Deserialize operation is successfully done
	Json::Value root;
	pair <string,string> dummy;

	clear(); //clear all arguments firstly..
	bool parsedSuccess = reader.parse(input,root,false);		//parsing Json String

	if(!parsedSuccess)	//if it is not successfull
	{
		cout<<"Failed to parse JSON"<<endl<<reader.getFormatedErrorMessages()<<endl;
		return false; //error in parsing Json
	}
	//Start mandatory parameters deserialization
	if(!root["command"]["type"].isNull())
	{				//initialize type parameter if it is not null
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
		this->setPid(root["command"]["pid"].asString());				//initialize pid parameter if it is not null
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
	return true;
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
string& KACommand::getPid()
{
	return this->pid;
}
/**
 *  \details   setting "pid" private variable of KACommand instance.
 *  		   It carries the process id of the execution.
 */
void KACommand::setPid(const string& pid)
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
{					//getting Argument vector

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
 *  		   Command is terminated due to this variable value
 */
void KACommand::setTimeout(int timeout)
{
	this->timeout = timeout;
}
