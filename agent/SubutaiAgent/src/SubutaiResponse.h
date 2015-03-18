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
 *    @copyright 2013 Safehaus.org
 */
/**
 *  @brief     SubutaiResponse.h
 *  @class     SubutaiResponse.h
 *  @details   SubutaiResponse class is designed for marshaling and unmarshalling response messages.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.1.0
 *  @date      Sep 13, 2014
 */
#ifndef SUBUTAIRESPONSE_H_
#define SUBUTAIRESPONSE_H_

#include <syslog.h>
#include <iostream>
#include <jsoncpp/json.h>
#include <string>
#include <fstream>
#include "SubutaiContainer.h"
#include "Message.pb.h"
#include "SubutaiHelper.h"
using namespace std;
using std::stringstream;
using std::string;

class SubutaiResponse {
public:
	SubutaiResponse(void);
	virtual ~SubutaiResponse(void);
	string& getType();
	string& getUuid();
	int getRequestSequenceNumber();
	int getResponseSequenceNumber();
	int getExitCode();
	string& getStandardError();
	string& getStandardOutput();
	int getPid();
	string& getHostname();
	string& getArch();
	string& getParentHostname();
	vector<Interface> getInterfaces();
	string& getCommandId();
	string& getconfigPoint();
	string& getDateTime();
	string& getChangeType();
	vector<string>& getConfPoints();
	vector<SubutaiContainer*>& getContainerSet();
	void setContainerSet(vector<SubutaiContainer*>);
	void setHostname(const string&);
	void setParentHostname(const string&);
	void setCommandId(const string&);
	void setInterfaces(vector<Interface>);
	void addInterface(string, string, string);
	void setPid(int);
	void setType(const string&);
	void setUuid(const string&);
	void setArch(const string&);
	void setRequestSequenceNumber(int);
	void setResponseSequenceNumber(int);
	void setStandardError(const string&);
	void setStandardOutput(const string&);
	void setExitCode(int);
	void setconfigPoint(const string&);
	void setDateTime(const string&);
	void setChangeType(const string&);
	void setConfPoints(vector<string>);
	void serialize(string&);//Serializing a Chunk Response message to a Json String
	void serializeDone(string&);//Serializing a Last Done Response message to a Json string
	void clear();
private:
	string type;
	string uuid;
	int requestSequenceNumber;
	int responseSequenceNumber;
	int exitCode;
	int pid;
	string stdOut;
	string stdErr;
	string commandId;
	string hostname;
	string parentHostname;
	vector<Interface> interfaces;
	string configPoint;
	string dateTime;
	string changeType;
	string topic;
	string arch;
	vector<string> confPoints;
	vector<SubutaiContainer*> containers;
};

#endif /* SUBUTAIRESPONSE_H_ */
