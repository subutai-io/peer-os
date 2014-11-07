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
 *  \details   Default constructor of SubutaiContainer class.
 */
SubutaiContainer::SubutaiContainer(SubutaiLogger* logger, lxc_container* cont)
{
    this->container = cont;
    this->containerLogger = logger;
}

/**
 *  \details   Default destructor of SubutaiContainer class.
 */
SubutaiContainer::~SubutaiContainer()
{
    // TODO Auto-generated destructor stub
}

/**
 *  \details   Clear id, mac address and ip adresses.
 */
void SubutaiContainer::clear()
{
    id = "";
    macAddresses.clear();
    ipAddress.clear();
}

/**
 * Run program given as parameter 'program' with arguments 'params'
 * Return stdout if success or stderr if fails
 */
string SubutaiContainer::RunProgram(string program, vector<string> params) 
{
    ExecutionResult result = RunProgram(program, params, true, LXC_ATTACH_OPTIONS_DEFAULT);
    //cout << result.out << endl;
    if (result.exit_code == 0) {
        return result.out;
    } else {
        return result.err;
    }
}

/**
 * Run program given as parameter 'program' with arguments 'params' using lxc attach options 'opts'
 * Returns ExecutionResult object including exit_code and stdout if success or stderr if fails.
 *
 */
ExecutionResult SubutaiContainer::RunProgram(string program, vector<string> params, bool return_result, lxc_attach_options_t opts, bool captureOutput) 
{
    containerLogger->writeLog(1, containerLogger->setLogData("<SubutaiContainer>", "Running program: ", program));


    // get arguments list of the command which will be run on lxc

    char* _params[params.size() + 2];
    _params[0] = const_cast<char*>(program.c_str());
    vector<string>::iterator it;
    int i = 1;
    for (it = params.begin(); it != params.end(); it++) {
        _params[i] = const_cast<char*>((*it).c_str());
        i++;
    }
    _params[i] = NULL;

    // DEBUG

#if _DEBUG
    for (int __j = 0; __j < params.size() + 2; __j++) {
        cout << "<DEBUG> PARAMS DATA: " << _params[__j] << endl;
    }
#endif


    //   run command on LXC and read stdout into buffer.
    int fd[2];
    int _stdout = dup(1);
    ExecutionResult result;
    char buffer[1000];

    if (captureOutput) {
        pipe(fd);
        dup2(fd[1], 1);
        fflush(stdout);
        fflush(stderr);
        close(fd[1]);
    }
    result.exit_code = this->container->attach_run_wait(this->container, &opts, program.c_str(), _params);
    if (captureOutput) {
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


        //   get exit code, stdout and stderr.

        if (result.exit_code == 0) {
            result.out = command_output;
        } else {
            result.err = command_output;
        }
    }
    containerLogger->writeLog(1, containerLogger->setLogData("<SubutaiContainer>","Program executed: ", program));
    return result;
}

/**
 * return true if container is running, false otherwise
 */
bool SubutaiContainer::isContainerRunning()
{
    if (this->status == RUNNING) return true;
    return false;
}

/**
 * return true if container is stopped, false otherwise
 */
bool SubutaiContainer::isContainerStopped()
{
    if (this->status == STOPPED) return true;
    return false;
}

/**
 * return true if container is frozen, false otherwise
 */
bool SubutaiContainer::isContainerFrozen()
{
    if (this->status == FROZEN) return true;
    return false;
}


/**
 *  \details   get the users defined on LXC
 */
void SubutaiContainer::UpdateUsersList() 
{ 
    if(status != RUNNING) return ;
    this->_users.clear();
    vector<string> params;
    params.push_back("/etc/passwd");
    string passwd = RunProgram("/bin/cat", params);

    stringstream ss(passwd);
    string line;
    while (getline(ss, line, '\n')) {
        int uid;
        string uname;

        std::size_t found_first  = line.find(":");
        std::size_t found_second = line.find(":", found_first+1);
        std::size_t found_third  = line.find(":", found_second+1);

        uname = line.substr(0, found_first);
        uid   = atoi(line.substr(found_second+1, found_third).c_str());

        this->_users.insert(make_pair(uid, uname));
        //cout << " user: " <<  uid << " " << uname << endl;

    }
}
/**
 *  \details   ID of the Subutai Container is fetched from statically using this function.
 *  		   Example id:"ff28d7c7-54b4-4291-b246-faf3dd493544"
 */
bool SubutaiContainer::getContainerId()
{
    try
    {
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
            ofstream file(uuidFile.c_str());
            file << this->id;
            file.close();

            containerLogger->writeLog(1,containerLogger->setLogData("<SubutaiAgent>","Subutai Agent UUID: ",this->id));
            return false;
        }
        return true;
    } catch(const std::exception& error) {
        cout << error.what()<< endl;
    }
    return false;
}

/**
 *  \details   get mac ids of the Subutai Container is fetched from statically.
 */
bool SubutaiContainer::getContainerMacAddresses()
{
    macAddresses.clear();
    if (this->status != RUNNING) return false;
    try
    {
        string s ;
        s.append("/bin/ls /var/lib/lxc/");  	s.append(hostname);   	s.append("/rootfs/sys/class/net/");
        char *command = new char[s.length() + 1];    	strcpy(command, s.c_str());

        vector<string> network_list = _helper.runAndSplit(command, "r", "\n");
        for (vector<string>::iterator it = network_list.begin(); it != network_list.end(); it++) {

            string address_net;
            string addressFile = "/var/lib/lxc/" + this->hostname + "/rootfs/sys/class/net/"+ (*it) +"/address";
            ifstream file(addressFile.c_str());	//opening uuid.txt

            getline(file,address_net);
            file.close();

            if (address_net.empty()) {		//if mac is null or not reading successfully
                containerLogger->writeLog(3,containerLogger->setLogData("<SubutaiAgent>","MacAddress cannot be read !!"));
                return false;
            }

            macAddresses.insert(pair<string, string>((*it), address_net));

            containerLogger->writeLog(6,containerLogger->setLogData("<SubutaiAgent>", "Subutai Agent MacID for " + (*it) + ":", address_net));
            return true;
        }
    } catch(const std::exception& error) {
        cout << error.what()<< endl;
    }
    return false;
}


/**
 *  \details   set the hostname of Subutai Container.
 */
void SubutaiContainer::setContainerHostname(string hostname)
{
    this->hostname = hostname;
}

/**
 *  \details   get the status of Subutai Container.
 */
string SubutaiContainer::getContainerStatus()
{
    if (this->status == RUNNING) return "RUNNING";
    if (this->status == STOPPED) return "STOPPED";
    if (this->status == FROZEN)  return "FROZEN";
    return "ERROR";
}

/**
 *  \details   set the status of Subutai Container.
 */
void SubutaiContainer::setContainerStatus(containerStatus status)
{
    this->status = status;
}


/**
 *  \details   IpAddress of the SubutaiContainer machine is fetched from statically.
 */
bool SubutaiContainer::getContainerIpAddress()
{
    if (this->status != RUNNING) return false;
    ipAddress.clear();
    char** interfaces = this->container->get_interfaces(this->container);
    int i = 0;
    if(interfaces != NULL)
    {
        while (interfaces[i] != NULL) {
            char** ips = this->container->get_ips(this->container, interfaces[i], "inet", 0);
            int j = 0;
            while (ips[j] != NULL) {
                ipAddress.push_back(ips[j]);
                j++;
            }
            i++;
        }
    }
    free(interfaces);
    if (ipAddress.size() > 0) {
        return true;
    } else {
        return false;
    }
}

void SubutaiContainer::write()
{
    cout << hostname << " " << id << endl;
}

/**
 *  \details   getting SubutaiContainer uuid value.
 */
string SubutaiContainer::getContainerIdValue()
{
    return id;
}

/**
 *  \details   getting SubutaiContainer hostname value.
 */
string SubutaiContainer::getContainerHostnameValue()
{
    return hostname;
}

/**
 *  \details   getting SubutaiContainer lxc container value.
 */
lxc_container* SubutaiContainer::getLxcContainerValue()
{
    return container;
}

/**
 *  \details   getting SubutaiContainer macaddress value for a given interface.
 */
string SubutaiContainer::getContainerMacAddressValue(string network)
{
    return macAddresses.find(network)->second;
}

/**
 *  \details   getting SubutaiContainer Ip values.
 */
vector<string> SubutaiContainer::getContainerIpValue()
{
    return ipAddress;
}

/**
 *  \details   update all field of Subutai Container
 */
void SubutaiContainer::getContainerAllFields()
{
    clear();
    getContainerId();
    getContainerMacAddresses();
    getContainerIpAddress();

    UpdateUsersList();
}

ExecutionResult SubutaiContainer::RunCommand(SubutaiCommand* command) 
{
    lxc_attach_options_t opts = LXC_ATTACH_OPTIONS_DEFAULT;
    if (command->getWorkingDirectory() != "" && checkCWD(command->getWorkingDirectory())) {
        opts.initial_cwd = const_cast<char*>(command->getWorkingDirectory().c_str());
    }
    if (command->getRunAs() != "" && checkUser(command->getRunAs())) {
        opts.uid = getRunAsUserId(command->getRunAs());
    }
    vector<string> pr = ExplodeCommandArguments(command);
    bool hasProgram = false;
    string program;
    vector<string> args;
    for (vector<string>::iterator it = pr.begin(); it != pr.end(); it++) {
        if (!hasProgram) {
            program = (*it);
            hasProgram = true;
            continue;
        } 
        args.push_back((*it));
    }
    ExecutionResult res = RunProgram(program, args, true, opts, false);
    return res;
}


// We need to check if CWD is exist because in LXC API - if cwd does not
// exist CWD will become root directory
bool SubutaiContainer::checkCWD(string cwd) 
{
    vector<string> params;
    params.push_back(cwd);
    ExecutionResult result = RunProgram("ls", params, true, LXC_ATTACH_OPTIONS_DEFAULT);    
    if (result.exit_code == 0) { 
        return true;
    } else {
        return false;
    }
}

/*
 * /details     Runs throught the list of userid:username pairs
 *              and check user existence
 */

bool SubutaiContainer::checkUser(string username) 
{
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
int SubutaiContainer::getRunAsUserId(string username) 
{
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

/**
 * \details		Write info into a file on LXC
 */
void SubutaiContainer::PutToFile(string filename, string text) {
    vector<string> args;
    args.push_back("-c");
    args.push_back("'/bin/echo");
    args.push_back(text);
    args.push_back(">");
    args.push_back(filename);
    RunProgram("/bin/bash", args);
}

/**
 * \details		Get the full path for a given program
 */
string SubutaiContainer::findFullProgramPath(string program_name) 
{
    vector<string> args;
    args.push_back(program_name);
    string locations = RunProgram("whereis", args);
    return locations; // TODO: Parse whereis output
}

/**
 * \details 	run ps command on LXC.
 */
string SubutaiContainer::RunPsCommand() {
    vector<string> args;
    args.push_back("for i in `ps aux | grep '[s]h -c' | awk -F \" \" '{print $2}'`; do ps aux | grep `pgrep -P $i` | sed '/grep/d' ; done 2> /dev/null");
    return RunProgram("/bin/bash", args);
}

/**
 * \details 	check and divide command and arguments if necessary.
 */
vector<string> SubutaiContainer::ExplodeCommandArguments(SubutaiCommand* command) 
{
    vector<string> result;
    size_t p = 0;
    size_t n = 0;
    while ((n = command->getCommand().find_first_of(" ", p)) != string::npos) {
        if (n - p != 0) {
            result.push_back(command->getCommand().substr(p, n - p));
        }
        p = n + 1;
    } 
    if (p < command->getCommand().size()) {
        result.push_back(command->getCommand().substr(p));
    }
    for(unsigned int i = 0; i < command->getArguments().size(); i++)
        result.push_back(command->getArguments()[i]);

    return result;
}

/**
 * For testinf purpose
 *
 * Test if long commands with && can run or not:
 * It waits until all the commands run to return.
 */
void SubutaiContainer::tryLongCommand() {
    vector<string> args;
    args.push_back("-c");
    args.push_back("ls -la && ls && ls -la && ls && sleep 2 && ls && ls -la && ls && ls -la && ls && ls && sleep 2 && ls && ls -la && ls && ls -la && ls && ls && sleep 2 && ls && ls -la && ls && ls -la && ls && ls && sleep 2 && ls && ls -la && ls && ls -la && ls && ls && sleep 2 && ls && ls -la && ls && ls -la && ls && ls && sleep 2 && ls && ls -la && ls && ls -la && ls && ls && sleep 2 && ls && ls -la && ls && ls -la && ls && ls && sleep 2 && ls && ls -la && ls && ls -la && ls");

    cout << RunProgram("/bin/bash", args) << endl;
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
