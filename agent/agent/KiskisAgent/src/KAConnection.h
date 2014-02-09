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
 *  @brief     KAConnection.h
 *  @class     KAConnection.h
 *  @details   KAConnection class is designed for communication with AMQP ACtiveMQ broker.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.0.2
 *  @date      Feb 03, 2014
 */
#ifndef KACONNECTION_H_
#define KACONNECTION_H_

#include <activemq/core/ActiveMQConnection.h>
#include <activemq/transport/DefaultTransportListener.h>
#include <decaf/lang/Integer.h>
#include <decaf/lang/Thread.h>
#include <decaf/lang/Runnable.h>
#include <decaf/util/concurrent/CountDownLatch.h>
#include <decaf/lang/Long.h>
#include <decaf/util/Date.h>
#include <decaf/lang/System.h>
#include <activemq/core/ActiveMQConnectionFactory.h>
#include <activemq/util/Config.h>
#include <activemq/library/ActiveMQCPP.h>
#include <cms/Connection.h>
#include <cms/Session.h>
#include <cms/TextMessage.h>
#include <cms/BytesMessage.h>
#include <cms/MapMessage.h>
#include <cms/ExceptionListener.h>
#include <cms/MessageListener.h>
#include <stdlib.h>
#include <stdio.h>
#include <memory>
#include <syslog.h>
#include <cstdlib>
#include <iostream>
#include <sstream>
#include <string>
#include <fstream>
#include <sys/types.h>
#include <unistd.h>

using namespace std;
using std::stringstream;
using std::string;
using namespace activemq;
using namespace activemq::core;
using namespace decaf;
using namespace activemq::transport;
using namespace decaf::lang;
using namespace decaf::util;
using namespace decaf::util::concurrent;
using namespace cms;

class KAConnection : public Runnable , public ExceptionListener, public DefaultTransportListener
{
public:
	KAConnection(string,string,string);
	~KAConnection();

	string getBrokerURI();
	Connection* getConnection();
	Session* getSession();
	void setSesion(Session*);
	void setConnection(Connection*);
	void setBrokerURI(string);
	bool openSession();							//open TCP session on the queue
	void close();
	MessageProducer* getSender();
	TextMessage* getSendMessage();
	Destination* getSenderDestination();
	void setMessage(TextMessage*);
	void setSenderDestination(Destination*);
	void setSender(MessageProducer*);
	void setSendMessage(TextMessage*);
	void sendMessage(string);			//send a message to the queue
	MessageConsumer* getReceiver();
	TextMessage* getReceivedMessage();
	Destination* getReceiverDestination();
	void setReciever(MessageConsumer*);
	void setReceivedMessage(TextMessage*);
	void setReceiverDestination(Destination*);
	bool fetchMessage(string&);

	virtual void run()
	{
	}
	virtual void transportResumed()
	{				//Resuming Connection
		std::cout << "The Connection's Transport has been Restored." << std::endl;
	}
	virtual void transportInterrupted()
	{			//Connection interrupted
		std::cout << "The Connection's Transport has been Interrupted." << std::endl;
	}
	virtual void onException( const CMSException& ex AMQCPP_UNUSED )
	{		//Exception received
		printf("CMS Exception occurred.  Shutting down client.\n");
		exit(1);
	}
private:
	Connection* connection;
	Session* session;
	string brokerURI;							//broker URI address to open session
	MessageProducer* sender;					//sender
	TextMessage* sendmessage;					//sent message
	Destination* senderdestination;				//destination
	string serverURI;							//Server Destination for sending messages
	MessageConsumer* receiver;				//receiver
	string clientURI;						//clientURI for receiving message
	TextMessage* receivedmessage;			//received message
	Destination* receiverdestination;		//destination
};
#endif /* KACONNECTION_H_ */
