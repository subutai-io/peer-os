/*
 * Command.cpp
 *
 *  Created on: Aug 28, 2013
 *      Author: Emin inal
 */

#include "Command.h"
#include <jsoncpp/json.h>
#include <vector>
using namespace std;

Command::Command() {
	// TODO Auto-generated constructor stub
	setType("");
	setCwd("");
	setRequestSeqnum(0);
	setStderr("");
	setStdout("");
	setRunAs("");
	vector<string> dummy;
	setArguments(dummy);
	setFooPath("");
	setBarValue("");
}

Command::~Command() {
	// TODO Auto-generated destructor stub
}

void Command::Serialize( Json::Value& root , std::string& output ){

	Json::Value environment;
	Json::StyledWriter writer;

	root["command"]["type"] = type;
	root["command"]["cwd"]=cwd;
	root["command"]["request_seqnum"]=request_seqnum;
	root["command"]["stdout"]=stdout;
	root["command"]["stderr"]=stderr;
	root["command"]["program"]=program;
	root["command"]["run_as"]=run_as;
	root["command"]["uuid"]=uuid;

	environment["FOO_PATH"]=FOO_PATH;
	environment["BAR_VALUE"]=BAR_VALUE;

	root["command"]["environment"]=environment;

	for(unsigned int index=0; index < arguments.size(); index++)
	{
		root["command"]["arguments"][index]=arguments[index];
	}
	output = writer.write(root);

	request_seqnum++;		//increasing request_seqnum for new command serialization
	arguments.clear();
	root.clear();
}
bool Command::Deserialize( Json::Value& root, std::string& input ){
	Json::FastWriter writer;
	Json::Reader reader;

	bool parsedSuccess = reader.parse(input,root,false);

	if(!parsedSuccess)
	{
		cout<<"Failed to parse JSON"<<endl<<reader.getFormatedErrorMessages()<<endl;
		return false; //error in parsing Json
	}

	this->setType(root["command"]["type"].asString());
	this->setCwd(root["command"]["cwd"].asString());
	this->setRequestSeqnum(root["command"]["request_seqnum"].asInt());
	this->setStdout(root["command"]["stdout"].asString());
	this->setStderr(root["command"]["stderr"].asString());
	this->setProgram(root["command"]["program"].asString());
	this->setRunAs(root["command"]["run_as"].asString());
	this->setUuid(root["command"]["uuid"].asString());
	this->setFooPath(root["command"]["environment"]["FOO_PATH"].asString());
	this->setBarValue(root["command"]["environment"]["BAR_VALUE"].asString());

	arguments.clear();
	for(unsigned int index=0; index < root["command"]["arguments"].size(); index++)
	{
		arguments.push_back(root["command"]["arguments"][index].asString());
	}

	return true;
}
string& Command::getFooPath() {
	return FOO_PATH;
}
void Command::setFooPath(const string& FooPath){
	this-> FOO_PATH=FooPath;
}
string& Command::getBarValue() {
	return BAR_VALUE;
}
void Command::setBarValue(const string& BarValue){
	this->BAR_VALUE=BarValue;
}

void Command::setUuid(const string& uu_id){
	this->uuid = uu_id;
}
string& Command::getUuid() {
	return uuid;
}
void Command::setArguments(vector<string> myvector){

	for(unsigned int index=0 ; index< myvector.size(); index++)
	{
		arguments.push_back(myvector[index]);
	}
}

vector<string>& Command::getArguments(){

	return arguments;
}

string& Command::getCwd()  {
	return cwd;
}

void Command::setCwd(const string& cw_d) {
	this->cwd = cw_d;
}

string& Command::getProgram()  {
	return program;
}

void Command::setProgram(const string& program) {
	this->program = program;
}

int Command::getRequestSeqnum()  {
	return request_seqnum;
}

void Command::setRequestSeqnum(int requestSeqnum) {
	this->request_seqnum = requestSeqnum;
}

string& Command::getRunAs()  {
	return run_as;
}

void Command::setRunAs(const string& runAs) {
	this->run_as = runAs;
}

string& Command::getStderr()  {
	return stderr;
}

void Command::setStderr(const string& stderr) {
	this->stderr = stderr;
}

string& Command::getStdout()  {
	return stdout;
}

void Command::setStdout(const string& stdout) {
	this->stdout = stdout;
}

string& Command::getType()  {
	return type;
}

void Command::setType(const string& type) {
	this->type = type;
}
