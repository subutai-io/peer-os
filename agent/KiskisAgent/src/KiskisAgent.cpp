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
 *  @brief     KiskisAgent.cpp
 *  @class     KiskisAgent.cpp
 *  @details   This is KiskisAgent Software main process.
 *  		   It's main responsibility is that send and receive messages from ActiveMQ broker.
 *  		   It also creates a new process using KAThread Class when the new Execute Request comes.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.0.5
 *  @date      May 8 , 2014
 */
/** \mainpage  Welcome to Project KiskisAgent
 *	\section   KisKisAgent
 * 			   The Kiskis Agent is a simple daemon designed to connect securely to an AMQP server to
 * 			   reliably receive and send messages on queues and topics.
 * 	 	 	   It's purpose is to perform a very simple reduced set of instructions to
 * 	 	 	   manage any system administration task.
 * 	 	 	   The agent may run on physical servers, virtual machines or inside Linux Containers.
 */
#include "KACommand.h"
#include "KAResponse.h"
#include "KAUserID.h"
#include "KAResponsePack.h"
#include "KAThread.h"
#include "KALogger.h"
#include "KAConnection.h"
#include "pugixml.hpp"
#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_generators.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/thread/thread.hpp>

/**
 *  \details   This method designed for Typically conversion from integer to string.
 */
string toString(int intcont)
{		//integer to string conversion
	ostringstream dummy;
	dummy << intcont;
	return dummy.str();
}

/**
 *  \details   KiskisAgent's settings.xml is read by this function.
 *  		   url: Broker address is fetched. (for instance: url = "failover://(ssl://localhost:61167))
 *  		   connectionOptions: ReconnectDelay and Reconnect feature settings.
 *  		   loglevel: Debugging Loglevel. (0-8)
 */
int getSettings(string & url, string & connectionOptions, string & loglevel, string & clientpasswd)
{
	pugi::xml_document doc;

	if(doc.load_file("/etc/ksks-agent/config/settings.xml").status)		//if the settings file does not exist
	{
		return 100;
		exit(1);
	}
	url = doc.child("Settings").child_value("BrokerIP") ;		//reading url
	loglevel = doc.child("Settings").child_value("log_level") ;		//reading loglevel
	clientpasswd = doc.child("Settings").child_value("clientpasswd") ;		//reading cleintpassword
	url = "failover:ssl://" + url +":"+  doc.child("Settings").child_value("Port");		//combine url and port
	connectionOptions = "{reconnect:" + (string)(doc.child("Settings").child_value("reconnect")) + ", "
			"reconnect_timeout:" + doc.child("Settings").child_value("reconnect_timeout") +
			", reconnect_interval_max:" + doc.child("Settings").child_value("reconnect_interval_max") + "}";
	//combine connectionOptions string
	return 0;
}

/**
 *  \details   UUID of the KiskisAgent is fetched from statically using this function.
 *  		   Example uuid:"ff28d7c7-54b4-4291-b246-faf3dd493544"
 */
bool getUuid(string& Uuid)
{
	try
	{
		ifstream file("/etc/ksks-agent/config/uuid.txt");	//opening uuid.txt
		getline(file,Uuid);
		file.close();
		if(Uuid.empty())		//if uuid is null or not reading successfully
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
 *  \details   MACID(eth0) of the KiskisAgent is fetched from statically.
 */
bool getMacAddress(string& macaddress)
{
	try
	{
		ifstream file("/sys/class/net/eth0/address");	//opening macaddress
		getline(file,macaddress);
		file.close();
		if(macaddress.empty())		//if mac is null or not reading successfully
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
bool getHostname(string& hostname)
{
	try
	{
		ifstream file("/etc/hostname");	//opening hostname
		getline(file,hostname);
		file.close();
		if(hostname.empty())		//if hostname is null or not reading successfully
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
 *  \details   ParentHostname of the KiskisAgent machine is fetched from environment parameters.
 */
bool getParentHostname(string& parentHostname)
{
	try
	{
		string dummyline = "";
		ifstream file;
		file.open("/etc/profile", std::ifstream::in);	//opening profile file

		bool found_line=false;

		if (file.is_open())
		{
			while ( getline(file,dummyline) )
			{
				if (dummyline.find("PHY_HOST")!= std::string::npos)
				{
					found_line=true;
					break;
				}
			}
			file.close();
		}

		if(found_line==true)
		{
			unsigned pos = dummyline.find("PHY_HOST");
			parentHostname = dummyline.substr(pos+9,dummyline.size());
		}
		else
		{
			cout << "environment parameter is not found in /etc/profile file!!" << endl;
		}

		if(!parentHostname.empty())
		{
			return true;
		}
		else
			return false;
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
bool checkLXC()
{
	try
	{
		string firstline;
		ifstream file("/proc/1/cgroup");	//opening root cgroup file
		getline(file,firstline);
		file.close();
		int ret = firstline.find("lxc");
		if(ret==-1)		//if cgroup is null or not reading successfully
		{
			return false;
		}
		else
		{
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
bool getIpAddresses(vector<string>& myips)
{
	try
	{
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
						myips.push_back(p);
						//printf("%s\n", p);
					}
				}
			}
		}
		pclose(fp);
		return true;
	}
	catch(const std::exception& error)
	{
		cout << error.what()<< endl;
	}
	return false;
}

/**
 *  \details   threadSend function sends string messages in the Shared Memory buffer to ActiveMQ Broker.
 *  		   This is a thread with working concurrently with main thread.
 *  		   It is main purpose that checking the Shared Memory Buffer in Blocking mode and sending them to Broker
 */
void threadSend(message_queue *mq,KAConnection *connection,KALogger* logMain)
{
	try
	{
		string str;
		unsigned int priority;
		size_t recvd_size;
		while(true)
		{
			str.resize(2500);
			mq->receive(&str[0],str.size(),recvd_size,priority);
			logMain->writeLog(7,logMain->setLogData("<KiskisAgent>::<threadsend>",
					"New message comes to messagequeue to be sent:",str));
			connection->sendMessage(str);
			str.clear();
		}
		message_queue::remove("message_queue");
	}
	catch(interprocess_exception &ex)
	{
		message_queue::remove("message_queue");
		std::cout << ex.what() << std::endl;
		logMain->writeLog(3,logMain->setLogData("<KiskisAgent>::<threadsend>","New exception Handled:",ex.what()));
	}
}

/**
 *  \details   This method checks the Default HeartBeat execution timeout value.
 *  		   if execution timeout is occured it returns true. Otherwise it returns false.
 */
bool checkExecutionTimeout(unsigned int* startsec,bool* overflag,unsigned int* exectimeout,unsigned int* count)
{
	if (*exectimeout != 0)
	{
		boost::posix_time::ptime current = boost::posix_time::second_clock::local_time();
		unsigned int currentsec  =  current.time_of_day().seconds();

		if((currentsec > *startsec) && *overflag==false)
		{
			if(currentsec != 59)
			{
				*count = *count + (currentsec - *startsec);
				*startsec = currentsec;
			}
			else
			{
				*count = *count + (currentsec - *startsec);
				*overflag = true;
				*startsec = 1;
			}
		}
		if(currentsec == 59)
		{
			*overflag = true;
			*startsec = 1;
		}
		else
		{
			*overflag = false;
		}
		if(*count >= *exectimeout) //timeout
		{
			return true;	//timeout occured now
		}
		else
		{
			return false; //no timeout occured
		}
	}
	return false;	//no timeout occured
}

/**
 *  \details   This function is the main thread of KiskisAgent.
 *  		   It sends and receives messages from ActiveMQ broker.
 *  		   It is also responsible from creation new process.
 */
int main(int argc,char *argv[],char *envp[])
{
	string url,connectionOptions,loglevel;
	string clientpasswd;
	string Uuid,macaddress,hostname,parentHostname;
	int isLxc = -1;
	vector<string> ipadress;
	string serveraddress="SERVICE_QUEUE";
	string clientaddress;
	KAThread thread;
	int level;

	if(!thread.getUserID().checkRootUser())
	{
		//user is not root KiskisAgent Will be closed
		cout << "Main Process User is not root.. KiskisAgent is going to be closed.."<<endl;
		close(STDIN_FILENO);
		close(STDOUT_FILENO);
		close(STDERR_FILENO);
		return 300;
	}
	KALogger logMain;
	if(!logMain.openLogFileWithName("KiskisAgentMain.log"))
	{
		cout << "/var/log/ksks-agent/ folder does not exist.. KiskisAgent is going to be closed.."<<endl;
		FILE* dumplog = fopen("/etc/ksks-agent_dump.log","a+");
		string log = "<DEBUG> /var/log/ksks-agent/ folder does not exist.. KiskisAgent is going to be closed.. \n";
		fputs(log.c_str(),dumplog);
		fflush(dumplog);
		close(STDIN_FILENO);
		close(STDOUT_FILENO);
		close(STDERR_FILENO);
		return 200;
	}
	logMain.setLogLevel(7);
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","KiskisAgent is starting.."));
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","Settings.xml is reading.."));
	if(!getSettings(url,connectionOptions,loglevel,clientpasswd))
	{
		logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","URL:",url));
		logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","ConnectionOptions:",connectionOptions));
		logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","LogLevel:",loglevel));
		logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","Settings.xml is read successfully.."));
		stringstream(loglevel) >> level;
		logMain.setLogLevel(level);
	}
	else
	{
		logMain.writeLog(3,logMain.setLogData("<KiskisAgent>","Settings.xml cannot be read KiskisAgent is closing.."));
		logMain.closeLogFile();
		return 100;
	}
	if(!getUuid(Uuid))
	{						//get UUID of the agent if it exist. if it does not it will be regenerated..
		boost::uuids::random_generator gen;
		boost::uuids::uuid u = gen();

		const std::string tmp = boost::lexical_cast<std::string>(u);
		Uuid = tmp;
		ofstream file("/etc/ksks-agent/config/uuid.txt");
		file << Uuid;
		file.close();
		logMain.writeLog(1,logMain.setLogData("<KiskisAgent>","KiskisAgent UUID:",Uuid));
	}
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","KiskisAgent UUID:",Uuid));
	if(!getMacAddress(macaddress))	//getting MacAddress
	{
		logMain.writeLog(3,logMain.setLogData("<KiskisAgent>","MacAddress cannot be read !!"));
	}
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","KiskisAgent MacID:",macaddress));

	if(checkLXC()) //its lxc get the parenthostname from env
	{
		isLxc = 1;
		logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","This machine is a Lxc Container.."));
		logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","KiskisAgent IsLxc:",toString(isLxc)));
		if(getParentHostname(parentHostname))	//trying to get parentHostname
		{
			getHostname(hostname);
			logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","KiskisAgent Hostname:",hostname));
			logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","KiskisAgent ParentHostname:",parentHostname));
		}
		else
		{
			logMain.writeLog(3,logMain.setLogData("<KiskisAgent>","ParentHostname cannot be read !!"));
			getHostname(hostname);
			parentHostname="";
			logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","KiskisAgent Hostname:",hostname));
		}
	}
	else	//its physical parent hostname is null
	{
		isLxc = 0;
		logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","This machine is not a Lxc Container.."));
		logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","KiskisAgent IsLxc:",toString(isLxc)));
		getHostname(hostname); //its physical there is no parenthost.
		parentHostname="";
		logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","KiskisAgent Hostname:",hostname));
		logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","KiskisAgent ParentHostname:",parentHostname));
	}

	if(!getIpAddresses(ipadress))	//getting IPs
	{
		logMain.writeLog(3,logMain.setLogData("<KiskisAgent>","IpAddresses cannot be read !!"));
	}
	for(unsigned int i=0; i < ipadress.size() ; i++)
	{
		logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","KiskisAgent IpAddresses:",ipadress[i]));
	}

	activemq::library::ActiveMQCPP::initializeLibrary();
	decaf::lang::System::setProperty("decaf.net.ssl.keyStore","/etc/ksks-agent/config/client_ks.pem");
	decaf::lang::System::setProperty("decaf.net.ssl.keyStorePassword",clientpasswd.c_str());
	decaf::lang::System::setProperty("decaf.net.ssl.trustStore", "/etc/ksks-agent/config/client_ts.pem" );

	clientaddress = Uuid;
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","Connection url:",url));
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","Server Address:",serveraddress));
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","Client Address:",clientaddress));
	KAConnection connection(url,serveraddress,clientaddress);
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","Trying to open Connection with ActiveMQ Broker: ",url));
	KACommand command;
	KAResponsePack response;
	string input;
	string sendout;

	response.setIps(ipadress);
	response.setHostname(hostname);
	response.setParentHostname(parentHostname);
	response.setMacAddress(macaddress);
	response.setUuid(Uuid); 	//setting Uuid for response messages.
	if(!connection.openSession())
	{
		logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","Connection could not be established withActiveMQ Broker: ",url));
		logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","KiskisAgent is closing.."));
		logMain.closeLogFile();
		return 400;
	}
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","Connection Successfully opened with ActiveMQ Broker: ",url));
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","Registration Message is sending to ActiveMQ Broker.."));

	/*sending registration message*/
	sendout = response.createRegistrationMessage(response.getUuid(),response.getMacAddress(),response.getHostname(),
			response.getParentHostname());
	logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Registration Message:",sendout));
	connection.sendMessage(sendout);

	logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Shared Memory MessageQueue is initializing.."));
	message_queue messageQueue
	(open_or_create              //only create
			,"message_queue"           //name
			,100                       //max message number
			,2500             //max message size
	);
	logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Sending Thread is starting.."));
	boost::thread thread1(threadSend,&messageQueue,&connection,&logMain);
	/* Change the file mode mask */
	umask(0);

	boost::posix_time::ptime start = boost::posix_time::second_clock::local_time();
	unsigned int exectimeout = 175; //180 seconds for HeartBeat Default Timeout
	unsigned int startsec  =  start.time_of_day().seconds();
	bool overflag = false;
	unsigned int count = 1;
	list<int> pidList;
	int ncores = -1;
	ncores = sysconf(_SC_NPROCESSORS_CONF);
	int currentProcess=0;
	string str,str2; //
	logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Number of cpu core:", toString(ncores)));

	while(true)
	{
		try
		{
			if(checkExecutionTimeout(&startsec,&overflag,&exectimeout,&count)) //checking HeartBeat Default Timeout
			{
				//timeout occured!!
				response.clear();
				response.setIps(ipadress);
				response.setHostname(hostname);
				response.setMacAddress(macaddress);
				string resp = response.createHeartBeatMessage(Uuid,command.getRequestSequenceNumber(),
						macaddress,hostname,parentHostname,command.getSource(),command.getTaskUuid());
				connection.sendMessage(resp);

				logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","HeartBeat Response:", resp));
				start = boost::posix_time::second_clock::local_time();	//Reset HeartBeat Default Timeout values
				startsec  =  start.time_of_day().seconds();
				overflag = false;
				exectimeout = 175;
				count = 1;
			}
			usleep(20000);//20 ms delay for the main loop
			command.clear();
			for(list<int>::iterator iter = pidList.begin(); iter != pidList.end();iter++)
			{
				if(pidList.begin()!=pidList.end())
				{
					int status;
					pid_t result = waitpid(*iter,&status,WNOHANG);
					if(result == 0)
					{
						logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Process continue! PID:",toString(*iter),"total running:",toString(currentProcess)));
					}
					else
					{
						logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Process finished! PID:",toString(*iter),"total running:",toString(currentProcess)));
						iter = pidList.erase(iter);
						currentProcess--;
					}
				}
			}
			if(connection.fetchMessage(input)) 	//check and wait if new message comes?
			{
				if(command.deserialize(input))
				{
					logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","New Message is received"));
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","New Message:",input));
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Command source:",command.getSource()));
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Command type:",command.getType()));
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Command uuid:",command.getUuid()));
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Command TaskUuid:",command.getTaskUuid()));
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Command RequestSequenceNumber:",
							toString(command.getRequestSequenceNumber())));
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Command workingDirectory:",command.getWorkingDirectory()));
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Command StdOut:",command.getStandardOutput()));
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Command stdOutPath:",command.getStandardOutputPath()));
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Command StdErr:",command.getStandardError()));
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Command stdErrPath:",command.getStandardErrPath()));
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Command Program:",command.getProgram()));
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Command runAs:",command.getRunAs()));
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Command timeout:",toString(command.getTimeout())));

					if(command.getType()=="REGISTRATION_REQUEST_DONE") //type is registration done
					{
						logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Registration is done.."));
						//agent is registered to server now
					}
					else if(command.getType()=="EXECUTE_REQUEST")	//execution request will be executed in other process.
					{
						fstream file;	//opening uuid.txt
						file.open("/etc/ksks-agent/config/commandQueue.txt",fstream::in | fstream::out | fstream::app);
						file << input << endl ;
						logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Received Message to internal currentProcess!"));
						file.close();
					}
					else if(command.getType()=="PS_REQUEST")
					{
						logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","PS execution operation is starting.."));
						KAThread* mypointer = new KAThread;
						mypointer->getLogger().setLogLevel(level);
						command.setProgram("for i in `ps aux | grep '[s]h -c' | awk -F \" \" '{print $2}'`; do ps aux | grep `pgrep -P $i` | sed '/grep/d' ; done 2> /dev/null");
						command.setWorkingDirectory("/");
						mypointer->threadFunction(&messageQueue,&command,argv);
						delete mypointer;
					}
					else if(command.getType()=="HEARTBEAT_REQUEST")
					{
						logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Heartbeat message has been taken.."));
						response.clear();
						response.setIps(ipadress);
						response.setHostname(hostname);
						response.setParentHostname(parentHostname);
						response.setMacAddress(macaddress);
						string resp = response.createHeartBeatMessage(Uuid,command.getRequestSequenceNumber(),
								macaddress,hostname,parentHostname,command.getSource(),command.getTaskUuid());
						connection.sendMessage(resp);
						logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","HeartBeat Response:", resp));
					}
					else if(command.getType()=="TERMINATE_REQUEST")
					{
						logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Termination request ID:",toString(command.getPid())));
						logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Killing given PID.."));
						if(command.getPid() > 0)
						{
							int retstatus = kill(command.getPid(),SIGKILL);
							if(retstatus == 0) //termination is successfully done
							{
								string resp = response.createTerminateMessage(Uuid,command.getRequestSequenceNumber(),command.getSource(),command.getTaskUuid());
								connection.sendMessage(resp);
								logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Terminate success Response:", resp));
							}
							else if (retstatus == -1) //termination is failed
							{
								string resp = response.createFailTerminateMessage(Uuid,command.getRequestSequenceNumber(),command.getSource(),command.getTaskUuid());
								connection.sendMessage(resp);
								logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Terminate Fail Response! Received PID:",toString(command.getPid())));
							}
						}
						else
						{
							logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","Irrelevant Terminate Request"));
						}
					}
				}
				else
				{
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Failed at parsing Json String: ",input));
					connection.sendMessage(response.createResponseMessage(Uuid,9999999,command.getRequestSequenceNumber(),819,
							"Failed to Parse Json!!!","",command.getSource(),command.getTaskUuid()));
				}
			}
			else
			{
				if (currentProcess < ncores)
				{
					ifstream file2("/etc/ksks-agent/config/commandQueue.txt");
					if(file2.peek()!=ifstream::traits_type::eof())
					{
						ofstream file3("/etc/ksks-agent/config/commandQueue2.txt");
						input = "";
						int count=0;
						do
						{
							getline(file2,str2);
							if(str2.find("{") != string::npos)
								count++;
							input=input + str2 + "\n";
							str+=str2;
							if(str2.find("}") != string::npos)
								count--;
						}while(count>0);
						//					file3.open",fstream::in | fstream::out | fstream::app);
						while(getline(file2,str2))
						{
							file3 << str2 << endl;
						}
						file3.close();
						rename("/etc/ksks-agent/config/commandQueue2.txt","/etc/ksks-agent/config/commandQueue.txt");
						logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","Message Fetched from internal queue!"));
						if(input != "\n" && command.deserialize(input))
						{
							logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Execute operation is starting.."));
							KAThread* mypointer = new KAThread;
							mypointer->getLogger().setLogLevel(level);
							pidList.push_back(mypointer->threadFunction(&messageQueue,&command,argv));
							currentProcess++;
							delete mypointer;
						}
					}
				}
			}
		}
		catch(const std::exception& error)
		{
			logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Exception is raised: ",error.what()));
			cout<<error.what()<<endl;
		}
	}
	close(STDIN_FILENO);
	close(STDOUT_FILENO);
	close(STDERR_FILENO);
	logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","KiskisAgent is closing Successfully.."));
	logMain.closeLogFile();
	kill(getpid(),SIGKILL);
	activemq::library::ActiveMQCPP::shutdownLibrary();
	return 0;
}
