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
 *  @brief     SubutaiContainer.cpp
 *  @class     SubutaiContainer.cpp
 *  @details   SubutaiContainer Class is designed for getting and setting container variables and special informations.
 *  		   This class's instance can get get useful container specific informations
 *  		   such as IPs, UUID, hostname, macID, parentHostname, etc..
 *  @author    Mikhail Savochkin
 *  @author    Ozlem Ceren Sahin
 *  @version   1.1.0
 *  @date      Oct 31, 2014
 */
#include "SubutaiContainer.h"
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <iostream>

using namespace std;
/**
 *  \details   Default constructor of SubutaiEnvironment class.
 */
SubutaiContainer::SubutaiContainer(SubutaiLogger* logger, lxc_container* cont)
{
    this->container = cont;
    this->containerLogger = logger;
}

/**
 *  \details   Default destructor of SubutaiEnvironment class.
 */
SubutaiContainer::~SubutaiContainer()
{
    // TODO Auto-generated destructor stub
}

/**
 *  \details   This method designed for Typically conversion from integer to string.
 */
string SubutaiContainer::toString(int intcont)
{		//integer to string conversion
    ostringstream dummy;
    dummy << intcont;
    return dummy.str();
}

string SubutaiContainer::RunProgram(string program, vector<string> params) {
    ExecutionResult result = RunProgram(program, params, true, LXC_ATTACH_OPTIONS_DEFAULT);
    if (result.exit_code == 0) {
        return result.out;
    } else {
        return result.err;
    }
}

ExecutionResult SubutaiContainer::RunProgram(string program, vector<string> params, bool return_result, lxc_attach_options_t opts) {
    char* _params[params.size() + 2];
    _params[0] = const_cast<char*>(program.c_str());
    vector<string>::iterator it;
    int i = 1;
    for (it = params.begin(); it != params.end(); it++, i++) {
        _params[i] = const_cast<char*>(it->c_str());
    }
    _params[i] = NULL;
    int fd[2];
    int _stdout = dup(1);
    pipe(fd);
    dup2(fd[1], 1);
    char buffer[1000];
    ExecutionResult result;
    result.exit_code = this->container->attach_run_wait(this->container, &opts, program.c_str(), _params);
    fflush(stdout);
    close(fd[1]);
    dup2(_stdout, 1);
    close(_stdout);
    string command_output;
    while (1) {
        ssize_t size = read(fd[0], buffer, 1000);
        if (size < 1000) {
            buffer[size] = '\0';
            command_output += buffer;
            break;
        } else {
            command_output += buffer;
        }
    }
    if (result.exit_code == 0) {
        result.out = command_output;
    } else {
        result.err = command_output;
    }
    return result;
}

bool SubutaiContainer::isContainerRunning()
{
    if (this->status == RUNNING) return true;
    return false;
}

bool SubutaiContainer::isContainerStopped()
{
    if (this->status == STOPPED) return true;
    return false;
}

bool SubutaiContainer::isContainerFrozen()
{
    if (this->status == FROZEN) return true;
    return false;
}

void SubutaiContainer::UpdateUsersList() { 
    this->_users.clear();
    vector<string> params;
    params.push_back("/etc/passwd");
    string passwd = RunProgram("/bin/cat", params);
    size_t n = 0;
    size_t p = 0;
    stringstream ss(passwd);
    string line;
    while (getline(ss, line, '\n')) {
        int c = 0;
        int uid;
        string uname;
        while ((n = line.find_first_of(":", p)) != string::npos) {
            c++;
            if (n - p != 0) {
                if (c == 1) {
                    // This is a username
                    uname = line.substr(p, n - p);
                } else if (c == 3) {
                    // This is a uid
                    stringstream conv(line.substr(p, n - p));
                    if (!(conv >> uid)) {
                        uid = -1; // We failed to convert string to int
                    }
                }
            }
            this->_users.insert(make_pair(uid, uname));
        }
    }
}
/**
 *  \details   UUID of the Subutai Agent is fetched from statically using this function.
 *  		   Example uuid:"ff28d7c7-54b4-4291-b246-faf3dd493544"
 */
bool SubutaiContainer::getContainerId()
{
    try
    {/*
        vector<string> args;
        args.push_back("/etc/subutai-agent/uuid.txt");
        this-> id = RunProgram("/bin/cat", args);
        */
    	string uuidFile = "/var/lib/lxc/" + this->hostname + "/rootfs/etc/subutai-agent/uuid.txt";
    	ifstream file(uuidFile.c_str());	//opening uuid.txt
    	getline(file,this->id);
    	file.close();

        if (this->id.empty())		//if uuid is null or not reading successfully
        {
            boost::uuids::random_generator gen;
            boost::uuids::uuid u = gen();
            const std::string tmp = boost::lexical_cast<std::string>(u);
            this->id = tmp;
            /*
            args.clear();
            args.push_back(this->id);
            args.push_back(">");
            args.push_back("/etc/subutai-agent/uuid.txt");
            this-> id = RunProgram("/bin/echo", args);
            */
            ofstream file(uuidFile.c_str());
            file << this->id;
            file.close();

            containerLogger->writeLog(1,containerLogger->setLogData("<SubutaiAgent>","Subutai Agent UUID: ",this->id));
            return false;
        }
        return true;
    }
    catch(const std::exception& error)
    {
        cout << error.what()<< endl;
    }
    return false;
}

/**
 *  \details   MACID(eth0) of the KiskisAgent is fetched from statically.
 */
bool SubutaiContainer::getContainerMacAddress()
{
    if(this->status != RUNNING) return false;
    try
    {
        vector<string> args;
        args.push_back("/sys/class/net/eth0/address");
        this->macAddress = RunProgram("/bin/cat", args);
        if(this->macAddress.empty())		//if mac is null or not reading successfully
        {
            containerLogger->writeLog(3,containerLogger->setLogData("<SubutaiAgent>","MacAddress cannot be read !!"));
            return false;
        }
        containerLogger->writeLog(6,containerLogger->setLogData("<SubutaiAgent>","Subutai Agent MacID:",this->macAddress));
        return true;
    }
    catch(const std::exception& error)
    {
        cout << error.what()<< endl;
    }
    return false;
}

/**
 *  \details   Hostname of the KiskisAgent machine is fetched from statically.
 */
bool SubutaiContainer::getContainerHostname()
{
    if(this-> status != RUNNING || !this->hostname.empty()) return false;
    try
    {
        vector<string> args;
        args.push_back("/etc/hostname");
        this->hostname = RunProgram("/bin/cat", args);

        if(this->hostname.empty())		//if hostname is null or not reading successfully
        {
            containerLogger->writeLog(7, containerLogger->setLogData("<SubutaiAgent>","Failed to get container hostname (getContainerHostname)"));
            return false;
        }
        else
        {
            if(this->hostname[this->hostname.size()-1] == '\n') this->hostname[this->hostname.size()-1] = '\0';
        }

        containerLogger->writeLog(6,containerLogger->setLogData("<SubutaiAgent>","Retrieved container hostname:", this->hostname));
        return true;
    }
    catch(const std::exception& error)
    {
        cout << error.what()<< endl;
    }
    return false;
}

/**
 *  \details   Hostname of the KiskisAgent machine is fetched from statically.
 */
void SubutaiContainer::setContainerHostname(string hostname)
{
    this-> hostname = hostname;
}

/**
 *  \details   Hostname of the KiskisAgent machine is fetched from statically.
 */
string SubutaiContainer::getContainerStatus()
{
    if(this->status == RUNNING) return "RUNNING";
    if(this->status == STOPPED) return "STOPPED";
    if(this->status == FROZEN)  return "FROZEN";
}

void SubutaiContainer::setContainerStatus(containerStatus status)
{
    this->status = status;
}
/**
 *  \details   Parent Hostname of the Subutai Agent machine is fetched from c paramonfig file.
 */
bool SubutaiContainer::getContainerParentHostname()
{
    if(this->status != RUNNING) return false;
    try
    {
        vector<string> args;
        args.push_back("/etc/hostname");
        string config = RunProgram("/bin/cat", args);
        if (config.empty()) //file exist
        {
        	ofstream file("/tmp/config.txt");
        	file << config;
        	file.close();

            boost::property_tree::ptree pt;
            boost::property_tree::ini_parser::read_ini("/tmp/config.txt", pt);
            parentHostname =  pt.get<std::string>("Subutai-Agent.subutai_parent_hostname");
            containerLogger->writeLog(6, containerLogger->setLogData("<SubutaiAgent>","parentHostname: ",parentHostname));
        }

        if (!parentHostname.empty())
        {
            return true;
        }
        else
        {
            containerLogger->writeLog(6, containerLogger->setLogData("<SubutaiAgent>", "parentHostname does not exist!"));
            return false;
        }
    }
    catch(const std::exception& error)
    {
        cout << error.what()<< endl;
    }
    return false;
}


/**
 *  \details   IpAddress of the KiskisAgent machine is fetched from statically.
 */
bool SubutaiContainer::getContainerIpAddress()
{
    if (this->status != RUNNING) return false;
    ipAddress.clear();
    char** interfaces = this->container->get_interfaces(this->container);
    int i = 0;
    while (interfaces[i]) {
        char** ips = this->container->get_ips(this->container, interfaces[i], "inet", 0);
        int j = 0;
        while (ips[j]) {
            ipAddress.push_back(ips[j]);
            j++;
        }
        i++;
    }
    delete [] interfaces;
    if (ipAddress.size() > 0) {
        return true;
    } else {
        return false;
    }
}

/**
 *  \details   getting Agent uuid value.
 */
string SubutaiContainer::getContainerIdValue()
{
    return id;
}

/**
 *  \details   getting Agent hostname value.
 */
string SubutaiContainer::getContainerHostnameValue()
{
    return hostname;
}

/**
 *  \details   getting lxc container value.
 */
lxc_container* SubutaiContainer::getLxcContainerValue()
{
    return container;
}

/**
 *  \details   getting Agent macaddress value.
 */
string SubutaiContainer::getContainerMacAddressValue()
{
    return macAddress;
}

/**
 *  \details   getting Agent parentHostname value.
 */
string SubutaiContainer::getContainerParentHostnameValue()
{
    return parentHostname;
}

/**
 *  \details   getting Agent Ip values.
 */
vector<string> SubutaiContainer::getContainerIpValue()
{
    return ipAddress;
}

void SubutaiContainer::getContainerAllFields()
{
    getContainerHostname();
    getContainerId();
    getContainerMacAddress();
    getContainerParentHostname();
    getContainerIpAddress();
}

ExecutionResult SubutaiContainer::RunCommand(SubutaiCommand* command) {
    lxc_attach_options_t opts = LXC_ATTACH_OPTIONS_DEFAULT;
    if (command->getWorkingDirectory() != "" && checkCWD(command->getWorkingDirectory())) {
        opts.initial_cwd = const_cast<char*>(command->getWorkingDirectory().c_str());
    }
    if (command->getRunAs() != "" && checkUser(command->getRunAs())) {
        opts.uid = getRunAsUserId(command->getRunAs());
    }
    vector<string> args;
    ExecutionResult res = RunProgram(command->getProgram(), args, true, opts);
    return res;
}

void SubutaiContainer::write(){
    cout << " start" <<  id << "  " << macAddress << "  " << hostname << "  " << parentHostname<< " stop" <<  endl;

}

/*
void SubutaiContainer::registerContainer(SubutaiConnection* connection)
{
    SubutaiResponsePack response;
    getContainerAllFields();
    string sendout = response.createRegistrationMessage(this->id, this->macAddress, this->hostname, this->parentHostname, "", this->ipAddress);
    containerLogger->writeLog(7, containerLogger->setLogData("<SubutaiAgent>","Registration Message:", sendout));
    connection->sendMessage(sendout);
}*/

// We need to check if CWD is exist because in LXC API - if cwd does not
// exist CWD will become root directory
bool SubutaiContainer::checkCWD(string cwd) {
    vector<string> params;
    params.push_back(cwd);
    ExecutionResult result = RunProgram("/bin/cd", params, true, LXC_ATTACH_OPTIONS_DEFAULT);    
    if (result.exit_code == 0) 
        return true;
    else
        return false;
}

/*
 * /details     Runs throught the list of userid:username pairs
 *              and check user existence
 */

bool SubutaiContainer::checkUser(string username) {
    if (_users.empty()) {
        UpdateUsersList();
    }
    for (user_it it = _users.begin(); it != _users.end(); it++) {
        if ((*it).second.compare(username) == 0) {
            return true;
        }
    } 
    return false;
}

/*
 * /details     Runs through the list of userid:username pairs
 *              and returns user id if username was found
 */
int SubutaiContainer::getRunAsUserId(string username) {
    if (_users.empty()) {
        UpdateUsersList();
    }
    for (user_it it = _users.begin(); it != _users.end(); it++) {
        if ((*it).second.compare(username) == 0) {
            return (*it).first;
        }
    } 
    return -1;
}

string SubutaiContainer::findFullProgramPath(string program_name) {
    vector<string> args;
    args.push_back(program_name);
    string locations = RunProgram("whereis", args);
    return locations; // TODO: Parse whereis output
}
