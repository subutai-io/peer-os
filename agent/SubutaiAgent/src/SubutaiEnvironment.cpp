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
#include "SubutaiEnvironment.h"
/**
 *  \details   Default constructor of SubutaiEnvironment class.
 */
SubutaiEnvironment::SubutaiEnvironment(SubutaiLogger* logger)
{
	this->environmentLogger = logger;
}

/**
 *  \details   Default destructor of SubutaiEnvironment class.
 */
SubutaiEnvironment::~SubutaiEnvironment()
{
	// TODO Auto-generated destructor stub
}


/**
 *  \details   This method designed for Typically conversion from integer to string.
 */
string SubutaiEnvironment::toString(int intcont)
{		//integer to string conversion
	ostringstream dummy;
	dummy << intcont;
	return dummy.str();
}

/**
 *  \details   KiskisAgent's settings.xml is read by this function.
 *  		   url: Broker address is fetched. (for instance: url = "localhost:8883))
 *  		   connectionOptions: ReconnectDelay and Reconnect feature settings.
 *  		   loglevel: Debugging Loglevel. (0-8)
 */
int SubutaiEnvironment::getAgentSettings()
{
	pugi::xml_document doc;

	if(doc.load_file("/etc/subutai-agent/agent.xml").status)		//if the settings file does not exist
	{
		environmentLogger->writeLog(7,environmentLogger->setLogData("<SubutaiEnvironment::getAgentSettings>","Agent.xml cannot be read!"));
		environmentLogger->writeLog(7,environmentLogger->setLogData("<SubutaiEnvironment::getAgentSettings>","Agent is closing now."));
		environmentLogger->closeLogFile();
		return 100;
		exit(1);
	}
	connectionUrl = doc.child("Settings").child_value("BrokerIP") ;		//reading url
	logLevel = doc.child("Settings").child_value("log_level") ;		//reading loglevel
	clientPassword = doc.child("Settings").child_value("clientpasswd") ;		//reading cleintpassword
	connectionPort = doc.child("Settings").child_value("Port");
	connectionOptions = doc.child("Settings").child_value("reconnect_timeout");

	environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","ConnectionUrl: ",connectionUrl));
	environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","ConnectionPort: ",connectionPort));
	environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","ConnectionOptions:",connectionOptions));
	environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","LogLevel:",logLevel));
	environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","Agent.xml is read successfully.."));
	int loglevel;
	stringstream(logLevel) >> loglevel;
	environmentLogger->setLogLevel(loglevel);

	return 0;
}

/**
 *  \details   UUID of the Subutai Agent is fetched from statically using this function.
 *  		   Example uuid:"ff28d7c7-54b4-4291-b246-faf3dd493544"
 */
bool SubutaiEnvironment::getAgentUuid()
{
	try
	{
		ifstream file("/etc/subutai-agent/uuid.txt");	//opening uuid.txt
		getline(file,this->uuid);
		file.close();
		if(this->uuid.empty())		//if uuid is null or not reading successfully
		{
			boost::uuids::random_generator gen;
			boost::uuids::uuid u = gen();

			const std::string tmp = boost::lexical_cast<std::string>(u);
			this->uuid = tmp;
			ofstream file("/etc/subutai-agent/uuid.txt");
			file << this->uuid;
			file.close();
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
bool SubutaiEnvironment::getAgentMacAddress()
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
bool SubutaiEnvironment::getAgentHostname()
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
bool SubutaiEnvironment::getAgentParentHostname()
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
 *  \details   EnvironmentId of the Subutai Agent machine is fetched from config file.
 */
bool SubutaiEnvironment::getAgentEnvironmentId()
{
	try
	{
		if (ifstream("/etc/subutai/lxc-config")) //file exist
		{
			boost::property_tree::ptree pt;
			boost::property_tree::ini_parser::read_ini("/etc/subutai/lxc-config", pt);
			environmentId =  pt.get<std::string>("Subutai-Agent.subutai_env_id");
			environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","environmentId: ",environmentId));
		}

		if(!environmentId.empty())
		{
			return true;
		}
		else
		{
			environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","environmentId does not exist !!"));
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
 *  \details   Checking the machine is lxc or not
 */
bool SubutaiEnvironment::isAgentLxc()
{
	try
	{
		string firstline;
		ifstream file("/proc/1/cpuset");	//opening root cgroup file
		getline(file,firstline);
		file.close();
		int ret = firstline.find("lxc");
		if(ret==-1)		//if cgroup is null or not reading successfully
		{
			islxc = 0;
			environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","This machine is not a Lxc Container.."));
			environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","Subutai Agent IsLxc:",toString(islxc)));
			getAgentHostname(); //its physical there is no parenthost.
			parentHostname="";
			environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","Subutai Agent Hostname:",hostname));
			return false;
		}
		else
		{
			islxc = 1;
			environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","This machine is a Lxc Container.."));
			environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","Subutai Agent IsLxc:",toString(islxc)));
			if(getAgentParentHostname())	//trying to get parentHostname
			{
				getAgentHostname();
				environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","Subutai Agent Hostname:",hostname));
			}
			else
			{
				environmentLogger->writeLog(3,environmentLogger->setLogData("<SubutaiAgent>","ParentHostname cannot be read !!"));
				getAgentHostname();
				parentHostname="";
				environmentLogger->writeLog(6,environmentLogger->setLogData("<SubutaiAgent>","Subutai Agent Hostname:",hostname));
			}
			return true;
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
bool SubutaiEnvironment::getAgentIpAddress()
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
string SubutaiEnvironment::getAgentUuidValue()
{
	return uuid;
}

/**
 *  \details   getting Agent hostname value.
 */
string SubutaiEnvironment::getAgentHostnameValue()
{
	return hostname;
}

/**
 *  \details   getting Agent macaddress value.
 */
string SubutaiEnvironment::getAgentMacAddressValue()
{
	return macAddress;
}

/**
 *  \details   getting Agent parentHostname value.
 */
string SubutaiEnvironment::getAgentParentHostnameValue()
{
	return parentHostname;
}

/**
 *  \details   getting Agent connectionUrl value.
 */
string SubutaiEnvironment::getAgentConnectionUrlValue()
{
	return connectionUrl;
}

/**
 *  \details   getting Agent connectionPort value.
 */
string SubutaiEnvironment::getAgentConnectionPortValue()
{
	return connectionPort;
}

/**
 *  \details   getting Agent connectionOptions value.
 */
string SubutaiEnvironment::getAgentConnectionOptionsValue()
{
	return connectionOptions;
}

/**
 *  \details   getting Agent Ip values.
 */
vector<string> SubutaiEnvironment::getAgentIpValue()
{
	return ipAddress;
}

/**
 *  \details   getting Agent environmentId value.
 */
string SubutaiEnvironment::getAgentEnvironmentIdValue()
{
	return environmentId;
}


