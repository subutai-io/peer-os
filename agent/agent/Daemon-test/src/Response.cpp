/*
 * Response.cpp
 *
 *  Created on: Aug 29, 2013
 *      Author: Emin inal
 */
#include "Response.h"
using namespace std;

Response::Response() {
	// TODO Auto-generated constructor stub
	setType("");
	setUuid("");
	setRequestSeqnum(0);
	setResponseSeqnum(0);
	setStderr("");
	setStdout("");
	setExitcode("");
	setMacID("");
}

Response::~Response() {
	// TODO Auto-generated destructor stub
}
void Response::Serialize(string& output){
	Json::Value environment;
	Json::StyledWriter writer;
	Json::Value root;

	root["response"]["type"] = type;
	root["response"]["request_seqnum"]=request_seqnum;
	root["response"]["response_seqnum"]=response_seqnum;
	root["response"]["stdout"]=stdout;
	root["response"]["stderr"]=stderr;
	root["response"]["macID"]= macID;
	root["response"]["uuid"]=uuid;

	output = writer.write(root);
	response_seqnum++;		//increasing response_seqnum for new response serialization
}
void Response::SerializeDone(string& output){
	Json::Value environment;
	Json::StyledWriter writer;
	Json::Value root;

	root["response"]["type"] = type;
	root["response"]["request_seqnum"]=request_seqnum;
	root["response"]["response_seqnum"] =response_seqnum;
	root["response"]["exit_code"] = exit_code;
	root["response"]["macID"]= macID;
	root["response"]["uuid"]=uuid;

	output = writer.write(root);
	response_seqnum++;				//increasing response_seqnum for new response serialization
}
bool Response::Deserialize(string input){
	Json::FastWriter writer;
	Json::Reader reader;
	Json::Value root;

	bool parsedSuccess = reader.parse(input,root,false);

	if(!parsedSuccess)
	{
		cout<<"Failed to parse JSON"<<endl<<reader.getFormatedErrorMessages()<<endl;
		return false; //error in parsing Json
	}

	if(root["response"]["exit_code"].asString() == "")
	{
		this->setType(root["response"]["type"].asString());
		this->setRequestSeqnum(root["response"]["request_seqnum"].asInt());
		this->setResponseSeqnum(root["response"]["response_seqnum"].asInt());
		this->setMacID(root["response"]["macID"].asString());
		this->setStdout(root["response"]["stdout"].asString());
		this->setStderr(root["response"]["stderr"].asString());
		this->setUuid(root["response"]["uuid"].asString());
	}
	else{
		this->setType(root["response"]["type"].asString());
		this->setRequestSeqnum(root["response"]["request_seqnum"].asInt());
		this->setResponseSeqnum(root["response"]["response_seqnum"].asInt());
		this->setMacID(root["response"]["macID"].asString());
		this->setExitcode(root["response"]["exit_code"].asString());
		this->setUuid(root["response"]["uuid"].asString());
	}
	return true;
}
string& Response::getMacID(){

	try{
		string mac;
		ifstream file("/root/mac.txt");
		getline(file,mac);
		macID = mac;
		file.close();
		return macID;
	}
	catch(const std::exception& error){
		cout << error.what()<< endl;
	}

	return macID;
}
void Response::setMacID(const string& mac){
	this->macID = mac ;
}
string& Response::getExitcode(){
	return exit_code;
}
void Response::setExitcode(const string& exitcode){
	exit_code = exitcode;
}

string& Response::getType() {
	return type;
}
void Response::setType(const string& type){
	this->type = type;
}
string& Response::getUuid(){
	return uuid;
}
void Response::setUuid(const string& uu_id){
	this->uuid = uu_id;
}
int Response::getRequestSeqnum(){
	return request_seqnum;
}
void Response::setRequestSeqnum(int requestSeqnum){
	this->request_seqnum = requestSeqnum;
}
int Response::getResponseSeqnum() {
	return response_seqnum;
}
void Response::setResponseSeqnum(int responseSeqnum){
	this->response_seqnum = responseSeqnum;
}
string& Response::getStderr(){
	return stderr;
}
void Response::setStderr(const string& std_err){
	this->stderr = std_err;
}
string& Response::getStdout(){
	return stdout;
}
void Response::setStdout(const string& std_out){
	this->stdout = std_out;
}
