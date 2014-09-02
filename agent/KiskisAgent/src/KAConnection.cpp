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
#include "KAConnection.h"

/**
 *  \details   Default constructor of KAConnection class.
 */
KAConnection::KAConnection(const char * id, const char * subscribedTopic, const char * publishedTopic
		, const char * broadcastTopic, const char * host, int port) : mosqpp::mosquittopp(id)
{
	this->keepalive = 60;
	this->id = id;
	this->port = port;
	this->host = host;
	this->subscribedTopic = subscribedTopic;
	this->broadcastTopic = broadcastTopic;
	this->publishedTopic = publishedTopic;
	this->reveivedMessage=false;
	this->connectionStatus=false;
	this->bufferSize = 10000;
	this->certpath = "/etc/ksks-agent/config/";
};

/**
 *  \details   Callback method for getting passwords for client key.
 */
static int password_callback(char* buf, int size, int rwflag, void* userdata)
{
	pugi::xml_document doc;
	if(doc.load_file("/etc/ksks-agent/config/settings.xml").status)		//if the settings file does not exist
	{
		return 100;
		exit(1);
	}
	string clientpasswd = doc.child("Settings").child_value("clientpasswd") ;		//reading cleintpassword
	strncpy(buf,clientpasswd.c_str(),size);
	buf[size-1] = '\0';
	return strlen(buf);
}

/**
 *  \details   openSession method is one of the most important function of the KAConnection class.
 *  		   It tries to open a connection to MQTT Broker.
 *  		   if this operation is successfully done it returns true. Otherwise it returns false.
 */
bool KAConnection::openSession()
{
	tls_opts_set(1, "tlsv1.1", NULL);
	string ca = certpath + "ca.crt";
	string clientCrt = certpath + "client.crt";
	string clientKey = certpath + "client.key";
	int sslresult = tls_set(ca.c_str(),NULL,clientCrt.c_str(),clientKey.c_str(),password_callback);
	int result = connect(this->host, this->port, this->keepalive);
	if(result == MOSQ_ERR_SUCCESS && sslresult == MOSQ_ERR_SUCCESS)
	{
		subscribe(NULL,this->subscribedTopic,2); //subscribed to agent own Topic.
		subscribe(NULL,this->broadcastTopic,2); //subscribed to broadcastTopic.
		this->connectionStatus=true;
		return true;
		//	if(result == MOSQ_ERR_SUCCESS)
	}
	else
		return false;
}

/**
 *  \details   This method is tries to reconnect to MQTT Broker.
 *  		   if this operation is successfully done it returns true. Otherwise it returns false.
 */
bool KAConnection::reConnect()
{
	int result;
	result = reconnect();
	if(result == MOSQ_ERR_SUCCESS)
	{
		cout << "Successfully reconnected to server.." << endl;
		return true;
	}
	else
		return false;
}

/**
 *  \details   Default destructor of KAConnection class.
 */
KAConnection::~KAConnection()
{
	loop_stop();              // Kill the thread
	mosqpp::lib_cleanup();    // Mosquitto library cleanup
}

/**
 *  \details   This method sends the given strings about execution responses to MQTT Broker.
 */
bool KAConnection::sendMessage(string message)
{
	const  char * _message = message.c_str();
	int ret = publish(NULL,this->publishedTopic,strlen(_message),_message,2,true);
	return ( ret == MOSQ_ERR_SUCCESS );
}

/**
 *  \details   This method informs the user about the disconnecting from MQTT Broker.
 */
void KAConnection::on_disconnect(int rc)
{
	std::cout << " KAConnection - disconnection(" << rc << ")" << std::endl;
	this->connectionStatus=false;
}

/**
 *  \details   This method informs the user about the connecting to MQTT Broker.
 */
void KAConnection::on_connect(int rc)
{
	if ( rc == 0 )
	{
		cout << " KAConnection - connected with server" << endl;
		if(connectionStatus==false)
		{
			subscribe(NULL,this->subscribedTopic,2); // resubscribe to agent own Topic.
			subscribe(NULL,this->broadcastTopic,2); //resubscribe to broadcastTopic.
		}
		connectionStatus=true;
	}
	else
	{
		cout << " KAConnection - Impossible to connect with server(" << rc << ")" << endl;
	}
}

/**
 *  \details   This method informs the user about the published message to MQTT Broker.
 */
void KAConnection::on_publish(int mid)
{
	cout << " KAConnection - Message (" << mid << ") succeed to be published " << endl;
}

/**
 *  \details   This method informs the user about the published message to MQTT Broker.
 */
void KAConnection::on_subscribe(int mid)
{
	cout << "Subscriptions succesfully Done!"  << endl;
}

/**
 *  \details   This method gets the message from buffer asynchronously and changes status of the received message flag.
 */
void KAConnection::on_message(const struct mosquitto_message *message)
{
	char buf[this->bufferSize+1];

	if(!strcmp(message->topic, this->subscribedTopic) || !strcmp(message->topic, this->broadcastTopic))
	{
		memset(buf, 0, (this->bufferSize+1)*sizeof(char));
		/* Copy N-1 bytes to ensure always 0 terminated. */
		memcpy(buf, message->payload, this->bufferSize*sizeof(char));

		this->reveivedMessage = true; //setting message status true
		setMessage((string)buf); //setting message from buffer
	}
	else
		cout << "Error!" << endl;
}

/**
 *  \details   This method returns the status of the new message.
 */
bool KAConnection::checkMessageStatus()
{
	return reveivedMessage;
}

/**
 *  \details   This method resets the status of the new message flag.
 */
void KAConnection::resetMessageStatus()
{
	this->reveivedMessage = false;
}

/**
 *  \details   This method is one of the most important function of the KAConnection class.
 *  		   It tries to return the message from MQTT Broker in none-blocking mode.
 */
string KAConnection::getMessage()
{
	return this->messsage;
}

/**
 *  \details   setting "message" private variable of the KAConnection class.
 *
 */
void KAConnection::setMessage(string message)
{
	this->messsage = message;
}

/**
 *  \details   setting "id" private variable of the KAConnection class.
 *
 */
string KAConnection::getID()
{
	string uuid= this->id;
	return uuid;
}

