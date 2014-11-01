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
#include "SubutaiConnection.h"
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
    char* _params[params.size() + 2];
    _params[0] = const_cast<char*>(program.c_str());
    vector<string>::iterator it;
    int i = 1;
    for (it = params.begin(); it != params.end(); it++, i++) {
        _params[i] = const_cast<char*>(it->c_str());
    }
    _params[i] = NULL;
    lxc_attach_options_t opts = LXC_ATTACH_OPTIONS_DEFAULT;
    int fd[2];
    int _stdout = dup(1);
    pipe(fd);
    dup2(fd[1], 1);
    char buffer[1000];
    // TODO: if exit code not equals one - we got stderr
    int exit_code = this->container->attach_run_wait(this->container, &opts, program.c_str(), _params);
    fflush(stdout);
    close(fd[1]);
    dup2(_stdout, 1);
    close(_stdout);
    // TODO: Decide where to keep this command output
    string command_output;
    while (1) {
        ssize_t size = read(fd[0], buffer, 1000);
        command_output += buffer;
        if (size < 1000) {
            buffer[size] = '\0';
            command_output += buffer;
            break;
        } else {
            command_output += buffer;
        }
    }

    return command_output;
}


bool SubutaiContainer::isContainerRunning()
{
	if(this->status == RUNNING) return true;
	return false;
}

bool SubutaiContainer::isContainerStopped()
{
	if(this->status == STOPPED) return true;
	return false;
}

bool SubutaiContainer::isContainerFrozen()
{
	if(this->status == FROZEN) return true;
	return false;
}
/**
 *  \details   UUID of the Subutai Agent is fetched from statically using this function.
 *  		   Example uuid:"ff28d7c7-54b4-4291-b246-faf3dd493544"
 */
bool SubutaiContainer::getContainerId()
{
	if(this-> status != RUNNING) return false;
    try
    {
        vector<string> args;
        args.push_back("/etc/subutai-agent/uuid.txt");
        this-> id = RunProgram("/bin/cat", args);
        if (this->id.empty())		//if uuid is null or not reading successfully
        {
            boost::uuids::random_generator gen;
            boost::uuids::uuid u = gen();

            const std::string tmp = boost::lexical_cast<std::string>(u);
            this->id = tmp;

            args.clear();
            args.push_back(this->id);
            args.push_back(">");
            args.push_back("/etc/subutai-agent/uuid.txt");
            this-> id = RunProgram("/bin/echo", args);
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
	if(this-> status != RUNNING) return false;
    try
    {
        vector<string> args;
        args.push_back("/sys/class/net/eth0/address");
        this-> macAddress = RunProgram("/bin/cat", args);
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
	if(this-> status != RUNNING) return false;
    try
    {
    	vector<string> args;
    	args.push_back("/etc/hostname");
    	this-> hostname = RunProgram("/bin/cat", args);
        if(this->hostname.empty())		//if hostname is null or not reading successfully
        {
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
 *  \details   Hostname of the KiskisAgent machine is fetched from statically.
 */
void SubutaiContainer::setContainerHostname(string hostname)
{
    this-> hostname = hostname;
}

/**
 *  \details   Hostname of the KiskisAgent machine is fetched from statically.
 */
containerStatus SubutaiContainer::getContainerStatus()
{
    return this->status;
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
	if(this-> status != RUNNING) return false;
    try
    {
    	vector<string> args;
    	args.push_back("/etc/hostname");
    	string config = RunProgram("/bin/cat", args);
        if (config.empty()) //file exist
        {
        	ofstream file("/tmp/subutai/config.txt");
        	file << config;
        	file.close();
            boost::property_tree::ptree pt;
            boost::property_tree::ini_parser::read_ini("/tmp/subutai/config.txt", pt);
            parentHostname =  pt.get<std::string>("Subutai-Agent.subutai_parent_hostname");
            containerLogger->writeLog(6,containerLogger->setLogData("<SubutaiAgent>","parentHostname: ",parentHostname));
        }

        if(!parentHostname.empty())
        {
            return true;
        }
        else
        {
        	containerLogger->writeLog(6,containerLogger->setLogData("<SubutaiAgent>","parentHostname does not exist!"));
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
	if(this-> status != RUNNING) return false;
    try
    {

        ipAddress.clear();

    	vector<string> args ;
    	string config = RunProgram("ifconfig", args);

    	ofstream file("/tmp/subutai/ipaddress.txt");
    	file << config;
    	file.close();

        FILE * fp = fopen("/tmp/subutai/ipaddress.txt", "r");
        if (fp)
        {
            char *p=NULL, *e; size_t n;
            while ((getline(&p, &n, fp) > 0) && p)
            {
                if ((p = strstr(p, "inet addr:")))
                {
                    p+=10;
                    if ((e = strchr(p, ' ')))
                    {
                        *e='\0';
                        ipAddress.push_back(p);
                    }
                }
            }
        }
        pclose(fp);

        for(unsigned int i=0; i < ipAddress.size() ; i++)
        {
        	containerLogger->writeLog(6,containerLogger->setLogData("<SubutaiAgent>","Subutai Agent IpAddress:",ipAddress[i]));
        }
        return true;
    }
    catch(const std::exception& error)
    {
        cout << error.what()<< endl;
    }
    containerLogger->writeLog(3,containerLogger->setLogData("<SubutaiAgent>","IpAddress cannot be read !!"));
    return false;
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
	getContainerId();
	getContainerMacAddress();
	getContainerHostname();
	getContainerParentHostname();
	getContainerIpAddress();
}

void SubutaiContainer::write(){
   cout << id << "  " << macAddress << "  " << hostname << "  " << parentHostname<< "  " <<  endl;

}


void SubutaiContainer::registerContainer(SubutaiConnection* connection)
{
	SubutaiResponsePack response;

	string sendout = response.createRegistrationMessage(this-> id,this->macAddress,this->hostname,this->parentHostname,NULL,this->ipAddress);
	containerLogger->writeLog(7,containerLogger->setLogData("<SubutaiAgent>","Registration Message:",sendout));

	connection->sendMessage(sendout);
}
