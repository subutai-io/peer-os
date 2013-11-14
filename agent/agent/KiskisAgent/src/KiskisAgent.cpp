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
#include "pugixml/pugixml.hpp"
#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_generators.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/lexical_cast.hpp>

/**
 *  \details   KiskisAgent's settings.xml is read by this function.
 *  		   url: Broker address is fetched. (for instance: url = "failover://(ssl://localhost:61167))
 *  		   connectionOptions: ReconnectDelay and Reconnect feature settings.
 *  		   loglevel: Debugging Loglevel. (0-8)
 */
int getSettings(string & url, string & connectionOptions, string & loglevel)
{
	pugi::xml_document doc;

	if(doc.load_file("/etc/KiskisAgent/config/settings.xml").status)		//if the settings file does not exist
	{
		return 100;
	}
	url = doc.child("Settings").child_value("BrokerIP") ;		//reading url
	loglevel = doc.child("Settings").child_value("log_level") ;		//reading loglevel

	url = "failover://(ssl://" + url +":"+  doc.child("Settings").child_value("Port") + ")";		//combine url and port

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
		ifstream file("/etc/KiskisAgent/config/uuid.txt");	//opening mac.txt
		getline(file,Uuid);
		file.close();
		if(Uuid.empty())		//if mac is null or not reading successfully
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
 *  \details   threadSend function sends string messages in the Shared Memory buffer to ActiveMQ Broker.
 *  		   This is a thread with working concurrently with main thread.
 *  		   It is main purpose that checking the Shared Memory Buffer in Blocking mode and sending them to Broker
 */
void threadSend(message_queue *mq,KAConnection *connection)
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
			connection->sendMessage(str);
			str.clear();
		}
		message_queue::remove("message_queue");
	}
	catch(interprocess_exception &ex)
	{
		message_queue::remove("message_queue");
		std::cout << ex.what() << std::endl;
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
	string Uuid;
	string serveraddress="SERVICE_QUEUE";
	string clientaddress;

	activemq::library::ActiveMQCPP::initializeLibrary();
	decaf::lang::System::setProperty("decaf.net.ssl.keyStore","/etc/KiskisAgent/config/client_ks.pem");
	decaf::lang::System::setProperty("decaf.net.ssl.keyStorePassword",	"123456");
	decaf::lang::System::setProperty("decaf.net.ssl.trustStore", "/etc/KiskisAgent/config/client_ts.pem" );

	if(getSettings(url,connectionOptions,loglevel))	//if there is an error from reading settings.xml
	{
		cout << "settings.xml cannot be read!! KiskisAgent is going to close.."<<endl;
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
	}
	int level;
	stringstream(loglevel) >> level;

	clientaddress = Uuid;
	KAThread thread;
	KAConnection connection(url,serveraddress,clientaddress);
	KACommand command;
	KAResponsePack response;
	string input;
	string sendout;

	if(! thread.getUserID().checkRootUser())
	{
		//user is not root KiskisAgent Will be closed
		cout << "Main Process User is not root.. KiskisAgent is going to be closed.."<<endl;
		close(STDIN_FILENO);
		close(STDOUT_FILENO);
		close(STDERR_FILENO);

		return 300;
	}

	response.setUuid(Uuid); 	//setting Uuid for response messages.
	if(!connection.openSession())
	{
		return 400;
	}

	sendout = response.createRegistrationMessage(response.getUuid());
	connection.sendMessage(sendout);

	message_queue messageQueue
	(open_or_create              //only create
			,"message_queue"           //name
			,5                       //max message number
			,2500             //max message size
	);
	KALogger logMain;
	logMain.openLogFile(getpid(),0);

	boost::thread thread1(threadSend,&messageQueue,&connection);

	/* Change the file mode mask */
	umask(0);
	//	For responses the type parameter might have the following values: EXECUTE_RESPONSE, EXECUTE_RESPONSE_DONE, REGISTRATION_REQUEST, HEARTBEAT_RESPONSE, TERMINATE_REQUEST_DONE
	//	For commands the type parameter might have the following values: EXECUTE_REQUEST, REGISTRATION_REQUEST_DONE, HEARTBEAT_REQUEST, TERMINATE_REQUEST
	while(true)
	{
		try
		{
			if(connection.fetchMessage(input)) 	//check and wait if new message comes?
			{
				if(command.deserialize(input))
				{
					if(command.getType()=="REGISTRATION_REQUEST_DONE") //type is registration done
					{
						//agent is registered to server now
					}
					else if(command.getType()=="EXECUTE_REQUEST")	//execution request will be executed in other process.
					{
						KAThread* mypointer = new KAThread;
						while(!mypointer->threadFunction(&messageQueue,&command,&level));
						delete mypointer;
					}
					else if(command.getType()=="HEARTBEAT_REQUEST")
					{
						connection.sendMessage(response.createHeartBeatMessage(Uuid,command.getRequestSequenceNumber()));
					}
					else if(command.getType()=="TERMINATE_REQUEST")
					{
						if(atoi(command.getPid().c_str()))
						{
							kill(atoi(command.getPid().c_str()),SIGKILL);
						}
						else
						{
						}
						connection.sendMessage(response.createTerminateMessage(Uuid,command.getRequestSequenceNumber()));
					}
				}
				else
				{
					connection.sendMessage(response.createResponseMessage(Uuid,"9999999",command.getRequestSequenceNumber(),819,"Failed to Parse Json!!!",""));
				}
			}
		}
		catch(const std::exception& error)
		{
			cout<<error.what()<<endl;
		}
	}
	close(STDIN_FILENO);
	close(STDOUT_FILENO);
	close(STDERR_FILENO);
	logMain.closeLogFile();
	kill(getpid(),SIGKILL);
	activemq::library::ActiveMQCPP::shutdownLibrary();
	return 0;
}
