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
#include "SubutaiContainer.h"
/**
 *  \details   Default constructor of SubutaiEnvironment class.
 */
SubutaiContainer::SubutaiContainer(SubutaiLogger* logger, lxc_container* cont)
{
	this->container = cont;
    this->environmentLogger = logger;
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
    _params[i + 1] = NULL;
    lxc_attach_options_t opts = LXC_ATTACH_OPTIONS_DEFAULT;
    int fd[2];
    pipe(fd);
    int _stdout = dup(1);
    dup2(fd[1], 1);
    char buffer[1000];
    this->container->attach_run_wait(this->container, &opts, program.c_str(), _params);
    fflush(stdout);
    // TODO: Decide where to keep this command output
    string command_output;
    while (1) {
        ssize_t size = read(fd[0], buffer, 1000);
        command_output += buffer;
        if (size < 1000) {
            buffer[size] = '\0';
            command_output += buffer;
        }
    }
    dup2(_stdout, 1);

    return command_output;
}


/**
 *  \details   UUID of the Subutai Agent is fetched from statically using this function.
 *  		   Example uuid:"ff28d7c7-54b4-4291-b246-faf3dd493544"
 */
bool SubutaiContainer::getContainerUuid()
{
    try
    {
        vector<string> args = NULL;
        this-> uuid = RunProgram("cat /etc/subutai-agent/uuid.txt", args);
        if(this->uuid.empty())		//if uuid is null or not reading successfully
        {
            boost::uuids::random_generator gen;
            boost::uuids::uuid u = gen();

            const std::string tmp = boost::lexical_cast<std::string>(u);
            this->uuid = tmp;

            this-> uuid = RunProgram("echo " + this->uuid + " /etc/subutai-agent/uuid.txt", args);
            environmentLogger->writeLog(1,environmentLogger->setLogData("<SubutaiAgent>","Subutai Agent UUID: ",this->uuid));
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
    try
    {
        ifstream file("/sys/class/net/eth0/address");	//opening macaddress
        getline(file,this->macAddress);
        file.close();
        if(this->macAddress.empty())		//if mac is null or not reading successfully
        {
            environmentLogger->writeLog(3,environmentLogger->setLogData("<SubutaiAgent>","MacAddress cannot be read !!"));
            return false;
        }
        environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","Subutai Agent MacID:",this->macAddress));
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
    try
    {
        ifstream file("/etc/hostname");	//opening hostname
        getline(file,this->hostname);
        file.close();
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
 *  \details   Parent Hostname of the Subutai Agent machine is fetched from c paramonfig file.
 */
bool SubutaiContainer::getContainerParentHostname()
{
    try
    {
        if (ifstream("/etc/subutai/lxc-config")) //file exist
        {
            boost::property_tree::ptree pt;
            boost::property_tree::ini_parser::read_ini("/etc/subutai/lxc-config", pt);
            parentHostname =  pt.get<std::string>("Subutai-Agent.subutai_parent_hostname");
            environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","parentHostname: ",parentHostname));
        }

        if(!parentHostname.empty())
        {
            return true;
        }
        else
        {
            environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","parentHostname does not exist!"));
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
    try
    {
        ipAddress.clear();
        FILE * fp = popen("ifconfig", "r");
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
                        //printf("%s\n", p);
                    }
                }
            }
        }
        pclose(fp);

        for(unsigned int i=0; i < ipAddress.size() ; i++)
        {
            environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","Subutai Agent IpAddress:",ipAddress[i]));
        }
        return true;
    }
    catch(const std::exception& error)
    {
        cout << error.what()<< endl;
    }
    environmentLogger->writeLog(3,environmentLogger->setLogData("<SubutaiAgent>","IpAddress cannot be read !!"));
    return false;
}

/**
 *  \details   getting Agent uuid value.
 */
string SubutaiContainer::getContainerUuidValue()
{
    return uuid;
}

/**
 *  \details   getting Agent hostname value.
 */
string SubutaiContainer::getContainerHostnameValue()
{
    return hostname;
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



