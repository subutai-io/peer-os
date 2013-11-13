/**
 *  \brief     KiskisAgent.cpp
 *  \details   KiskisAgent Software main process
 *  \author    Emin INAL
 *  \author    Bilal BAL
 *  \version   1.0
 *  \date      Sep 27, 2013
 *  \copyright GNU Public License.
 */
#include "KACommand.h"
#include "KAResponse.h"
#include "KAUserID.h"
#include "KAResponsePack.h"
#include "KAThread.h"
#include "KAConnection.h"
#include "pugixml/pugixml.hpp"
#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_generators.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/lexical_cast.hpp>

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
bool getUuid(string& Uuid)
{
	try{
		ifstream file("/etc/KiskisAgent/config/uuid.txt");	//opening mac.txt
		getline(file,Uuid);
		file.close();
		if(Uuid.empty())		//if mac is null or not reading successfully
		{
			return false;
		}
		return true;
	}
	catch(const std::exception& error){
		cout << error.what()<< endl;
	}
	return false;
}

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
	catch(interprocess_exception &ex){
		message_queue::remove("message_queue");
		std::cout << ex.what() << std::endl;
	}
}
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
						mypointer->threadFunction(&messageQueue,&command);
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
	kill(getpid(),SIGKILL);
	activemq::library::ActiveMQCPP::shutdownLibrary();
	return 0;
}
