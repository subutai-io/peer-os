/*
 *============================================================================
 Name        : KAConnection.h
 Author      : Emin INAL
 Date		 : Sep 4, 2013
 Version     : 1.0
 Copyright   : Your copyright notice
 Description : KAConnection class is designed for communication with AMQP ACtiveMQ broker.
=============================================================================
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
	KAConnection(string brokerURI, string serverURI, string clientURI);
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
