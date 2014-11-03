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
/**
 *  @brief     SubutaiEnvironment.h
 *  @class     SubutaiEnvironment.h
 *  @details   SubutaiEnvironment Class is designed for getting and setting environment variables and special informations.
 *  		   This class's instance can get get useful Agent's specific Environment informations
 *  		   such us IPs, UUID, hostname, macID, parentHostname, etc..
 *  @author    Mikhail Savochkin
 *  @author    Ozlem Ceren Sahin
 *  @version   1.1.0
 *  @date      Oct 31, 2014
 */
#ifndef SUBUTAICONTAINER_H_
#define SUBUTAICONTAINER_H_
#include <syslog.h>
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <cstdlib>
#include <sstream>
#include <list>
#include <lxc/lxccontainer.h>
#include "pugixml.hpp"
#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_generators.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/thread/thread.hpp>
#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/ini_parser.hpp>
#include "SubutaiLogger.h"
#include "SubutaiConnection.h"
#include "SubutaiResponsePack.h"
using namespace std;
using std::stringstream;
using std::string;

// Execution Result contains exit code of a functions, stdout and/or stderr
struct ExecutionResult {
    string out;
    string err;
    int exit_code;
};

enum containerStatus { RUNNING, STOPPED, FROZEN };

class SubutaiContainer
{
public:
	SubutaiContainer( SubutaiLogger*, lxc_container* cont );
	virtual ~SubutaiContainer( void );
	string toString( int );
	bool getContainerId();
	bool getContainerMacAddress();
	bool getContainerHostname();
	bool getContainerParentHostname();
	bool getContainerIpAddress();
	string getContainerIdValue();
	string getContainerHostnameValue();
	string getContainerMacAddressValue();
	string getContainerParentHostnameValue();
	string getContainerConnectionUrlValue();
	string getContainerConnectionPortValue();
	string getContainerConnectionOptionsValue();
	lxc_container* getLxcContainerValue();
	vector<string> getContainerIpValue();
	containerStatus getContainerStatus();
	bool isContainerRunning();
	bool isContainerStopped();
	bool isContainerFrozen();

        void UpdateUsersList();
	void setContainerHostname(string);
	void setContainerStatus(containerStatus);
	void getContainerAllFields();
	string RunProgram(string , vector<string>);
	ExecutionResult RunProgram(string , vector<string>, bool return_result);
	void registerContainer(SubutaiConnection* );
	void write();
    bool checkCWD(string cwd);

private:
	containerStatus status;
	lxc_container* container;
	string id;
	string macAddress;
	string hostname;
	string parentHostname;
        map<int, string> _users;        // List of users available in system
	vector<string> ipAddress;
	SubutaiLogger*	containerLogger;
};
#endif /* SUBUTAICONTAINER_H_ */



