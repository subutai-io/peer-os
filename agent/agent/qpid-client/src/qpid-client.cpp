#include <qpid/messaging/Address.h>
#include <qpid/messaging/Connection.h>
#include <qpid/messaging/Message.h>
#include <qpid/messaging/Receiver.h>
#include <qpid/messaging/Sender.h>
#include <qpid/messaging/Session.h>

#include <algorithm>
#include <cstdlib>
#include <iostream>
#include <fstream>
#include <memory>
#include <sstream>
#include <list>
#include "Command.h"
#include "Response.h"
#include <jsoncpp/json.h>

using namespace std;
using namespace qpid::messaging;
using namespace qpid::types;

using std::stringstream;
using std::string;

int main(int argc, char** argv) {
	const char* url = argc>1 ? argv[1] : "amqp:tcp:127.0.0.1:5672";

	std::string connectionOptions = argc > 2 ? argv[2] : "";
	std::list <string> hostlist;
	std::list <Sender> mysenderlist;

	std::cout << url << std::endl;
	std::cout << connectionOptions << std::endl;

	Command command;
	Response response;
	Json::Value recieve;
	Json::Value send;
	string input,output;
	string mac="10:20:30:40:50:60";
	Message message;
	string tmp;

	Connection connection(url, connectionOptions);
	try {
		connection.open();
		Session session = connection.createSession();
		Receiver receiver = session.createReceiver(mac + "; {create: always, delete:always}"); //client always listen his address
		Sender 	sender = session.createSender("service_queue; {create: always, delete:always}");	//client send message to server address

		response.setType("registration");				//sending registration information to server
		response.setUuid(mac);
		response.setStderr("no");
		response.setStdout("no");
		response.setRequestSeqnum(0);
		response.setResponseSeqnum(1);

		response.Serialize(send,output);
		message.setContent(output);
		sender.send(message);

		response.setType("registration-done");				//sending done information
		response.setUuid(mac);
		response.setRequestSeqnum(0);
		response.setExitcode("100");

		response.SerializeDone(send,output);
		message.setContent(output);
		sender.send(message);

		while (true){
			try{

				message = receiver.fetch(Duration::IMMEDIATE); //fetch a message from service_queue
				input = message.getContent();
				session.acknowledge();
				cout << input << endl;

				if(command.Deserialize(recieve,input))
				{
					if(command.getType() == "command-registration-done")				//host is registered to server
					{

					}
					else if(command.getType() == "command-execute"){

						response.setType("response-execute");				//sending a chunk
						response.setUuid(Uuid(true).str());
						response.setStderr("no");
						response.setStdout("no");
						response.setRequestSeqnum(command.getRequestSeqnum());
						response.setResponseSeqnum(0);

						response.Serialize(send,output);
						message.setContent(output);
						sender.send(message);

						response.setType("response-execute-done");				//sending done information
						response.setUuid(Uuid(true).str());
						response.setRequestSeqnum(command.getRequestSeqnum());
						response.setExitcode("100");

						response.SerializeDone(send,output);
						message.setContent(output);
						sender.send(message);
					}
				}

			}catch(const std::exception& error)
			{
			}
		}

		connection.close();
		return 0;
	} catch(const std::exception& error) {
		std::cout << error.what() << std::endl;
		connection.close();
	}
	return 1;
}
