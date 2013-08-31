/*
 * Response.cpp
 *
 *  Created on: Aug 29, 2013
 *      Author: Emin inal
 */
#include "Response.h"
#include <jsoncpp/json.h>

using namespace std;


Response::Response() {
	// TODO Auto-generated constructor stub
	setType("");
	setUuid("");
	setRequestSeqnum(0);
	setResponseSeqnum(0);
	setStderr("");
	setStdout("");
}

Response::~Response() {
	// TODO Auto-generated destructor stub
}
void Response::Serialize( Json::Value& root , std::string& output ){

	Json::Value environment;
	Json::StyledWriter writer;

	root["response"]["type"] = type;
	root["response"]["request_seqnum"]=request_seqnum;
	root["response"]["response_seqnum"]=response_seqnum;
	root["response"]["stdout"]=stdout;
	root["response"]["stderr"]=stderr;
	root["response"]["uuid"]=uuid;

	output = writer.write(root);
	root.clear();
}
bool Response::Deserialize( Json::Value& root, std::string& input ){
	Json::FastWriter writer;
	Json::Reader reader;

	bool parsedSuccess = reader.parse(input,root,false);

	if(!parsedSuccess)
	{
		cout<<"Failed to parse JSON"<<endl<<reader.getFormatedErrorMessages()<<endl;
		return false; //error in parsing Json
	}

	this->setType(root["response"]["type"].asString());
	this->setRequestSeqnum(root["response"]["request_seqnum"].asInt());
	this->setResponseSeqnum(root["response"]["response_seqnum"].asInt());
	this->setStdout(root["response"]["stdout"].asString());
	this->setStderr(root["response"]["stderr"].asString());
	this->setUuid(root["response"]["uuid"].asString());

	string output = writer.write(root);
	return true;
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



