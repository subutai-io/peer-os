/**   @copyright 2013 Safehaus.org
 *
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
 */
/**
 *  @brief     KACommand.h
 *  @class     KACommand.h
 *  @details   KACommand Class is designed for marshaling and unmarshalling command instance.
 *  		   This class's instance can serialize and deserialize JSON/string datas.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.0.0
 *  @date      Aug 28, 2013
 */
#ifndef KACOMMAND_H_
#define KACOMMAND_H_
#include <syslog.h>
#include <iostream>
#include <jsoncpp/json.h>
#include <string>
#include <vector>
#include <cstdlib>
#include <sstream>
#include <list>
using namespace std;
using std::stringstream;
using std::string;

class KACommand
{
public:
	KACommand( void );
	virtual ~KACommand( void );
	list<pair<string,string> >& getEnvironment();
	vector<string>& getArguments();
	string& getUuid();
	string& getWorkingDirectory();
	string& getProgram();
	string& getRunAs();
	string& getStandardError();
	string& getStandardOutput();
	string& getType();
	string& getStandardOutputPath();
	string& getStandardErrPath();
	int getPid();
	string& getHostname();
	string& getMacAddress();
	string& getTaskUuid();
	vector<string>& getIps();
	int getTimeout();
	int getRequestSequenceNumber();
	string& getSource();
	void setSource(const string&);
	void setHostname(const string&);
	void setMacAddress(const string&);
	void setTaskUuid(const string&);
	void setIps(vector<string>);
	void setTimeout(int);
	void setPid(int);
	void setEnvironment(list<pair<string,string> >&);
	void setArguments(vector<string>);
	void setUuid(const string&);
	void setWorkingDirectory(const string&);
	void setProgram(const string&);
	void setRequestSequenceNumber(int);
	void setRunAs(const string&);
	void setStandardError(const string&);
	void setStandardOutput(const string&);
	void setStandardErrPath(const string&);
	void setStandardOutPath(const string&);
	void setType(const string&);
	bool deserialize(string&);					//Deserializing Json String to Command Instance
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
	string 			stdOuthPath;
	string 			stdErrPath;
	vector<string>	args;
	list<pair<string,string> > environment;
	string			taskUuid;
	string			macAddress;
	string			hostname;
	vector<string>  ips;
	string			source;
};
#endif /* KACOMMAND_H_ */
