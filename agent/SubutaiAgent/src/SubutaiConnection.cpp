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
#include "SubutaiConnection.h"

/**
 *  \details   Default constructor of SubutaiConnection class.
 */
SubutaiConnection::SubutaiConnection(const char * id,
		const char * subscribedTopic, const char * publishedTopic,
		const char * broadcastTopic, const char * host, int port,
		SubutaiEnvironment* env, SubutaiLogger* log) :
		mosqpp::mosquittopp(id), logger(log) {
	this->keepalive = 60;
	this->id = id;
	this->port = port;
	this->host = host;
	this->subscribedTopic = subscribedTopic;
	this->broadcastTopic = broadcastTopic;
	this->publishedTopic = publishedTopic;
	this->receivedMessage = false;
	this->connectionStatus = false;
	this->bufferSize = 10000;
	this->certpath = "/etc/subutai-agent/";
	this->environment = env;
	initializeQueue();
}
;

/**
 *  \details   Callback method for getting passwords for client key.
 */
static int password_callback(char* buf, int size, int rwflag, void* userdata) {
	pugi::xml_document doc;
	if (doc.load_file("/etc/subutai-agent/agent.xml").status) //if the settings file does not exist
	{
		return 100;
		exit(1);
	}
	string clientpasswd = doc.child("Settings").child_value("clientpasswd"); //reading cleintpassword
	strncpy(buf, clientpasswd.c_str(), size);
	buf[size - 1] = '\0';
	return strlen(buf);
}

/**
 *  \details   openSession method is one of the most important function of the SubutaiConnection class.
 *  		   It tries to open a connection to MQTT Broker.
 *  		   if this operation is successfully done it returns true. Otherwise it returns false.
 */
bool SubutaiConnection::openSession() {
	tls_opts_set(1, "tlsv1.1", NULL);
	string ca = certpath + "ca.crt";
	string clientCrt = certpath + "client.crt";
	string clientKey = certpath + "client.key";
	int sslresult = tls_set(ca.c_str(), NULL, clientCrt.c_str(),
			clientKey.c_str(), password_callback);
	int result = connect(this->host, this->port, this->keepalive);
	if (result == MOSQ_ERR_SUCCESS && sslresult == MOSQ_ERR_SUCCESS) {
		subscribe(NULL, this->subscribedTopic, 2); //subscribed to agent own Topic.
		subscribe(NULL, this->broadcastTopic, 2); //subscribed to broadcastTopic.
		this->connectionStatus = true;
		return true;
		//	if(result == MOSQ_ERR_SUCCESS)
	} else {
		return false;
	}
}

/**
 *  \details   This method is tries to reconnect to MQTT Broker.
 *  		   if this operation is successfully done it returns true. Otherwise it returns false.
 */
bool SubutaiConnection::reConnect() {
	int result;
	result = reconnect();
	if (result == MOSQ_ERR_SUCCESS) {
		cout << "Successfully reconnected to server.." << endl;
		return true;
	} else {
		return false;
	}
}

/**
 *  \details   Default destructor of SubutaiConnection class.
 */
SubutaiConnection::~SubutaiConnection() {
	loop_stop();              // Kill the thread
	mosqpp::lib_cleanup();    // Mosquitto library cleanup
}

/**
 *  \details   This method sends the given strings about execution responses to MQTT Broker.
 */
bool SubutaiConnection::sendMessage(string message, string topic) {
	fflush(stdout);
	const char * _message = message.c_str();
	if (topic.size() == 0) {
		topic = this->publishedTopic;
	}
	int ret = publish(NULL, topic.c_str(), strlen(_message), _message, 2, true);
	return (ret == MOSQ_ERR_SUCCESS);
}

/**
 *  \details   This method informs the user about the disconnecting from MQTT Broker.
 */
void SubutaiConnection::on_disconnect(int rc) {
	std::cout << " SubutaiConnection - disconnection(" << rc << ")"
			<< std::endl;
	this->connectionStatus = false;
}

/**
 *  \details   This method informs the user about the connecting to MQTT Broker.
 */
void SubutaiConnection::on_connect(int rc) {
	if (rc == 0) {
		cout << " SubutaiConnection - connected with server" << endl;
		if (connectionStatus == false) {
			subscribe(NULL, this->subscribedTopic, 2); // resubscribe to agent own Topic.
			subscribe(NULL, this->broadcastTopic, 2); //resubscribe to broadcastTopic.
		}
		connectionStatus = true;
	} else {
		cout << " SubutaiConnection - Impossible to connect with server(" << rc
				<< ")" << endl;
	}
}

/**
 *  \details   This method informs the user about the published message to MQTT Broker.
 */
void SubutaiConnection::on_publish(int mid) {
#if _DEBUG
	cout << " SubutaiConnection - Message (" << mid << ") succeed to be published " << endl;
#endif
}

/**
 *  \details   This method informs the user about the published message to MQTT Broker.
 */
void SubutaiConnection::on_subscribe(int mid) {
	cout << "Subscriptions succesfully Done!" << endl;
}

/**
 *  \details   This method gets the message from buffer asynchronously and changes status of the received message flag.
 */
void SubutaiConnection::on_message(const struct mosquitto_message *message) {
	char buf[this->bufferSize + 1];
	SubutaiCommand new_command;
	if (!strcmp(message->topic, this->subscribedTopic)
			|| !strcmp(message->topic, this->broadcastTopic)) {
		memset(buf, 0, (this->bufferSize + 1) * sizeof(char));
		/* Copy N-1 bytes to ensure always 0 terminated. */
		memcpy(buf, message->payload, this->bufferSize * sizeof(char));

		string& input = (string&) buf;
		if (new_command.deserialize(input)) {
			if (new_command.getType() == "EXECUTION_REQUEST") {
				addMessageToExecQueue(&new_command, input);
			} else {
				addMessageToQueue(&new_command);
			}
		} else {
			logger->writeLog(3,
					logger->setLogData("<SubutaiAgent>",
							"Failed to parsing JSON String: ", input));
			if (input.size() >= 10000) {
				this->sendMessage(
						response.createResponseMessage(
								environment->getAgentUuidValue(), 9999999,
								new_command.getRequestSequenceNumber(), 1,
								"Request Size is greater than Maximum Size", "",
								new_command.getCommandId()));
				this->sendMessage(
						response.createExitMessage(
								environment->getAgentUuidValue(), 9999999,
								new_command.getRequestSequenceNumber(), 2,
								new_command.getCommandId(), 1));
			} else {
				this->sendMessage(
						response.createResponseMessage(
								environment->getAgentUuidValue(), 9999999,
								new_command.getRequestSequenceNumber(), 1,
								"Request is not a valid JSON string", "",
								new_command.getCommandId()));
				this->sendMessage(
						response.createExitMessage(
								environment->getAgentUuidValue(), 9999999,
								new_command.getRequestSequenceNumber(), 2,
								new_command.getCommandId(), 1));
			}
		}
	}
	//this->receivedMessage = true; //setting message status true
	//setMessage((string)buf); //setting message from buffer
	else
		cout << "Error!" << endl;
}

/**
 *  \details   This method adds new message to message queue.
 */
bool SubutaiConnection::addMessageToQueue(SubutaiCommand* cmd) {
	if (msg_queue.size() < MAX_MSG_NUM) {
		msg_queue.push(cmd);
	} else {
		logger->writeLog(3,
				logger->setLogData("<SubutaiConnection>",
						"Message queue is full. New request won't be executed."));
	}
	return true;
}

/**
 *  \details   This method adds new message to execution message queue.
 */
bool SubutaiConnection::addMessageToExecQueue(SubutaiCommand* cmd,
		string& input) {
	if (execution_queue.size() < MAX_MSG_NUM) {
		execution_queue.push(cmd);
		helper.writeToFile(commandQueuePath, input);
	} else {
		logger->writeLog(3,
				logger->setLogData("<SubutaiConnection>",
						"Message queue is full. New request won't be executed."));
	}
	return true;
}

/**
 *  \details   This method returns the status of the new message.
 */
bool SubutaiConnection::checkMessageStatus() {
	if (msg_queue.empty()) {
		return false;
	} else {
		return true;
	}
	//return receivedMessage;
}

/**
 *  \details   This method returns the status of the new message.
 */
bool SubutaiConnection::checkExecutionMessageStatus() {
	if (execution_queue.empty()) {
		return false;
	} else {
		return true;
	}
	//return receivedMessage;
}

/**
 *  \details   This method resets the status of the new message flag.
 */
void SubutaiConnection::resetMessageStatus() {
	this->receivedMessage = false;
}

/**
 *  \details   This method is one of the most important function of the SubutaiConnection class.
 *  		   It tries to return the message from MQTT Broker in none-blocking mode.
 */
SubutaiCommand* SubutaiConnection::getMessage() {
	SubutaiCommand* tmp = msg_queue.front();
	msg_queue.pop();
	return tmp;
}

/**
 *  \details   This method is one of the most important function of the SubutaiConnection class.
 *  		   It tries to return the message from MQTT Broker in none-blocking mode.
 */
SubutaiCommand* SubutaiConnection::getExecutionMessage() {
	SubutaiCommand* tmp = execution_queue.front();
	execution_queue.pop();
	helper.removeFromFile(commandQueuePath, tmp->getCommandId());
	return tmp;
}

/**
 *  \details   setting "message" private variable of the SubutaiConnection class.
 *
 */
void SubutaiConnection::setMessage(string message) {
	this->message = message;
}

/**
 *  \details   setting "id" private variable of the SubutaiConnection class.
 *
 */
string SubutaiConnection::getID() {
	string uuid = this->id;
	return uuid;
}

void SubutaiConnection::initializeQueue() {
	string str;
	ifstream file(commandQueuePath.c_str());
	if (file.peek() != ifstream::traits_type::eof()) {
		while (getline(file, str)) {
			SubutaiCommand cmd;
			cmd.deserialize(str);
			execution_queue.push(&cmd);
		}
	}
}
