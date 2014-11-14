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
 *  @brief     SubutaiCommand.h
 *  @class     SubutaiCommand.h
 *  @details   SubutaiCommand Class is designed for marshaling and unmarshalling command instance.
 *  	       This class's instance can serialize and deserialize JSON/string datas.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.1.0
 *  @date      Sep 13, 2014
 */
#ifndef SUBUTAICOMMAND_H_
#define SUBUTAICOMMAND_H_
#include <syslog.h>
#include <iostream>
#include <jsoncpp/json.h>
#include <string>
#include <vector>
#include <cstdlib>
#include <sstream>
#include <list>
#if USE_PROTOBUF
#include "Message.pb.h"
#endif
using namespace std;
using std::stringstream;
using std::string;

class SubutaiCommand
{
public:
	SubutaiCommand( void );
	virtual ~SubutaiCommand( void );
	list<pair<string,string> >& getEnvironment();
	vector<string>& getArguments();
	vector<string>& getWatchArguments();
	string& getUuid();
	string& getWorkingDirectory();
	string& getCommand();
	string& getRunAs();
	string& getStandardError();
	string& getStandardOutput();
	string& getType();
	int getPid();
    int getIsDaemon();
	string& getHostname();
	string& getMacAddress();
	string& getCommandId();
	vector<string>& getIps();
	int getTimeout();
	int getRequestSequenceNumber();
	void setHostname(const string&);
	void setMacAddress(const string&);
	void setCommandId(const string&);
	void setIps(vector<string>);
	void setTimeout(int);
	void setPid(int);
	void setEnvironment(list<pair<string,string> >&);
	void setArguments(vector<string>);
	void setUuid(const string&);
	void setWorkingDirectory(const string&);
	void setCommand(const string&);
	void setRequestSequenceNumber(int);
	void setRunAs(const string&);
	void setStandardError(const string&);
	void setStandardOutput(const string&);
	void setType(const string&);
        void setIsDaemon(int);
	bool deserialize(string&);					//Deserializing Json String to Command Instance
	bool checkCommandString(const string&);
	void clear();
private:
	string        	type;
	string        	program;
	string		    uuid;
	int				pid;
	int			 	requestSequenceNumber;
	int				timeout;
	string       	stdOut;
	string        	stdErr;
	string       	workingDirectory;
	string        	runAs;
	vector<string>	args;
	list<pair<string,string> > environment;
	string			taskUuid;
	string			macAddress;
	string			hostname;
	vector<string>  ips;
	vector<string>	watchArgs;
        int             _isDaemon;
};
#endif /* SUBUTAICOMMAND_H_ */
