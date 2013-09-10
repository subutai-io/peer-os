/*
 * Command.cpp
 *
 *  Created on: Aug 28, 2013
 *      Author: Emin inal
 */
#include <iostream>
#include <jsoncpp/json.h>
#include <string>
#include <vector>
#include <cstdlib>
#include <sstream>
#include <list>
#include "Command.h"

using namespace std;
using std::stringstream;
using std::string;

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
	setMacID("");
}

Command::~Command() {
	// TODO Auto-generated destructor stub
}

void Command::Serialize(string& output){

	Json::Value environment;
	Json::StyledWriter writer;
	Json::Value root;

	root["command"]["type"] = type;
	root["command"]["cwd"]=cwd;
	root["command"]["request_seqnum"]=request_seqnum;
	root["command"]["stdout"]=stdout;
	root["command"]["stderr"]=stderr;
	root["command"]["program"]=program;
	root["command"]["run_as"]=run_as;
	root["command"]["uuid"]=uuid;
	root["command"]["macID"]=macID;

	for(unsigned int index=0; index < arguments.size(); index++)
	{
		root["command"]["arguments"][index]=arguments[index];
	}

	for(std::list<pair<string,string> >::iterator it = env.begin(); it != env.end(); it++ ){
		environment[it->first.c_str()] = it->second.c_str();
	}

	root["command"]["environment"]=environment;

	output = writer.write(root);

	request_seqnum++;		//increasing request_seqnum for new command serialization
}
bool Command::Deserialize(string input){
	Json::FastWriter writer;
	Json::Reader reader;
	Json::Value root;
	pair <string,string> dummy;

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
	this->setMacID(root["command"]["macID"].asString());

	Json::Value::Members members = root["command"]["environment"].getMemberNames();

	this->env.clear();
	for(unsigned int index=0; index < members.size(); index++)
	{
		dummy.first = members[index].c_str();
		dummy.second = root["command"]["environment"][members[index].c_str()].asString();
		this->env.push_back(dummy);
	}

	arguments.clear();
	for(unsigned int index=0; index < root["command"]["arguments"].size(); index++)
	{
		arguments.push_back(root["command"]["arguments"][index].asString());
	}

	return true;
}
list<pair<string,string> >& Command::getEnv(){
	return this->env;
}
void Command::setEnv(list<pair<string,string> >& envr){
	env.clear();
	pair <string,string> dummy;

	for (std::list<pair<string,string> >::iterator it = envr.begin(); it != envr.end(); it++ )
	{
		dummy.first = it->first.c_str();
		dummy.second = it->second.c_str();
		env.push_back(dummy);
	}
}
string& Command::getMacID(){
	return macID;
}
void Command::setMacID(const string& mac){
	this->macID = mac ;
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
