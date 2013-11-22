/**
 *  @brief     KiskisAgent.cpp
 *  @class     KiskisAgent.cpp
 *  @details   This is KiskisAgent Software main process.
 *  		   It's main responsibility is that send and receive messages from ActiveMQ broker.
 *  		   It also creates a new process using KAThread Class when the new Execute Request comes.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.0
 *  @date      Sep 27, 2013
 *  @copyright GNU Public License.
 */

/** \mainpage  Welcome to Project KiskisAgent
 *	\section   KisKisAgent
 * 			   The Kiskis Agent is a simple daemon designed to connect securely to an AMQP server to reliably receive and send messages on queues and topics.
 * 	 	 	   It's purpose is to perform a very simple reduced set of instructions to manage any system administration task.
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
int getSettings(string & url, string & connectionOptions, string & loglevel)
{
	pugi::xml_document doc;

	if(doc.load_file("/etc/ksks-agent/config/settings.xml").status)		//if the settings file does not exist
	{
		return 100;
		exit(1);
	}
	url = doc.child("Settings").child_value("BrokerIP") ;		//reading url
	loglevel = doc.child("Settings").child_value("log_level") ;		//reading loglevel

	url = "failover://ssl://" + url +":"+  doc.child("Settings").child_value("Port");		//combine url and port

	connectionOptions = "{reconnect:" + (string)(doc.child("Settings").child_value("reconnect")) + ", reconnect_timeout:" + doc.child("Settings").child_value("reconnect_timeout") +
			", reconnect_interval_max:" + doc.child("Settings").child_value("reconnect_interval_max") + "}";		//combine connectionOptions string
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
		message_queue::size_type recvd_size;
		while(true)
		{
			str.resize(2500);
			mq->receive(&str[0],str.size(),recvd_size,priority);
			logMain->writeLog(7,logMain->setLogData("<KiskisAgent>::<threadsend>","New message comes to messagequeue to be sent:",str));
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
 *  \details   This function is the main thread of KiskisAgent.
 *  		   It sends and receives messages from ActiveMQ broker.
 *  		   It is also responsible from creation new process.
 */
int main(int argc,char *argv[],char *envp[])
{
	string url,connectionOptions,loglevel;
	string Uuid,macaddress,hostname;
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
	logMain.openLogFileWithName("KiskisAgentMain.log");
	logMain.setLogLevel(7);
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","KiskisAgent is starting.."));
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","Settings.xml is reading.."));
	if(!getSettings(url,connectionOptions,loglevel))
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
		ofstream file("/etc/KiskisAgent/config/uuid.txt");
		file << Uuid;
		file.close();
		logMain.writeLog(1,logMain.setLogData("<KiskisAgent>","KiskisAgent UUID:",Uuid));
	}
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","KiskisAgent UUID:",Uuid));
	if(!getMacAddress(macaddress))
	{
		logMain.writeLog(3,logMain.setLogData("<KiskisAgent>","MacAddress cannot be read !!"));
	}
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","KiskisAgent MacID:",macaddress));
	if(!getHostname(hostname))
	{
		logMain.writeLog(3,logMain.setLogData("<KiskisAgent>","Hostname cannot be read !!"));
	}
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","KiskisAgent Hostname:",hostname));
	if(!getIpAddresses(ipadress))
	{
		logMain.writeLog(3,logMain.setLogData("<KiskisAgent>","IpAddresses cannot be read !!"));
	}
	for(unsigned int i=0; i < ipadress.size() ; i++)
	{
		logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","KiskisAgent IpAddresses:",ipadress[i]));
	}

	activemq::library::ActiveMQCPP::initializeLibrary();
	decaf::lang::System::setProperty("decaf.net.ssl.keyStore","/etc/ksks-agent/config/client_ks.pem");
	decaf::lang::System::setProperty("decaf.net.ssl.keyStorePassword",	"client");
	decaf::lang::System::setProperty("decaf.net.ssl.trustStore", "/etc/ksks-agent/config/client_ts.pem" );

	clientaddress = Uuid;
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","Connection url:",url));
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","Server Address:",serveraddress));
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","Cleint Address:",clientaddress));
	KAConnection connection(url,serveraddress,clientaddress);
	logMain.writeLog(6,logMain.setLogData("<KiskisAgent>","Trying to open Connection with ActiveMQ Broker: ",url));
	KACommand command;
	KAResponsePack response;
	string input;
	string sendout;

	response.setIps(ipadress);
	response.setIsLxc(false);
	response.setHostname(hostname);
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

	sendout = response.createRegistrationMessage(response.getUuid(),response.getMacAddress(),response.getHostname(),response.getIsLxc());
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
	//	For responses the type parameter might have the following values: EXECUTE_RESPONSE, EXECUTE_RESPONSE_DONE, REGISTRATION_REQUEST, HEARTBEAT_RESPONSE, TERMINATE_REQUEST_DONE
	//	For commands the type parameter might have the following values: EXECUTE_REQUEST, REGISTRATION_REQUEST_DONE, HEARTBEAT_REQUEST, TERMINATE_REQUEST

	while(true)
	{
		try
		{
			command.clear();
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
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Command RequestSequenceNumber:",toString(command.getRequestSequenceNumber())));
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Command workingDirectory:",command.getWorkingDirectory()));
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Command StdOut:",command.getStandardOutput()));
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Command StdErr:",command.getStandardError()));
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
						logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Execute operation is starting.."));
						KAThread* mypointer = new KAThread;
						mypointer->getLogger().setLogLevel(level);
						mypointer->threadFunction(&messageQueue,&command,argv);
						delete mypointer;
					}
					else if(command.getType()=="HEARTBEAT_REQUEST")
					{
						logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Heartbeat message has been taken.."));
						string resp = response.createHeartBeatMessage(Uuid,command.getRequestSequenceNumber(),macaddress,hostname,false,command.getSource(),command.getTaskUuid());
						connection.sendMessage(resp);
						logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","HeartBeat Response:", resp));
					}
					else if(command.getType()=="TERMINATE_REQUEST")
					{
						logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Termination request ID:",toString(command.getPid())));
						logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Killing given PID.."));
						int retstatus = kill(command.getPid(),SIGKILL);
						if(retstatus==0) //termination is successfully done
						{
							string resp = response.createTerminateMessage(Uuid,command.getRequestSequenceNumber(),command.getSource());
							connection.sendMessage(resp);
							logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Terminate success Response:", resp));
						}
						else if (retstatus ==-1) //termination is failed
						{
							string resp = response.createFailTerminateMessage(Uuid,command.getRequestSequenceNumber(),command.getSource());
							connection.sendMessage(resp);
							logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Terminate Fail Response:", resp));
						}
					}
				}
				else
				{
					logMain.writeLog(7,logMain.setLogData("<KiskisAgent>","Failed at parsing Json String: ",input));
					connection.sendMessage(response.createResponseMessage(Uuid,9999999,command.getRequestSequenceNumber(),819,"Failed to Parse Json!!!","",command.getSource(),command.getTaskUuid()));
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
