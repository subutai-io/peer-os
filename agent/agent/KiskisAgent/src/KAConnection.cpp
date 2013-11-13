/*
 *============================================================================
 Name        : KAConnection.cpp
 Author      : Emin INAL & Bilal BAL
 Date		 : Sep 4, 2013
 Version     : 1.0
 Copyright   : Your copyright notice
 Description : KAConnection class is designed for communication with AMQP ActiveMQ broker.
==============================================================================
 */
#include "KAConnection.h"

KAConnection::KAConnection(string brokerURI, string serverURI, string clientURI)
{
	this->brokerURI=brokerURI;
	this->connection=NULL;
	this->session=NULL;
	this->sender=NULL;
	this->serverURI=serverURI;
	this->senderdestination=NULL;

	this->receiver=NULL;
	this->clientURI=clientURI;
	this->receiverdestination=NULL;
}
KAConnection::~KAConnection()
{
	//closeConnection();								//destroy all pointers
}
bool KAConnection::openSession()
{							//opening a TCP session on Broker
	try
	{
		ActiveMQConnectionFactory* connectionFactory =  new ActiveMQConnectionFactory( brokerURI ); 	//Create a ConnectionFactory
		if(connectionFactory != NULL)
		{
			connection = connectionFactory->createConnection();						// Creating Connection
			connection->start();													// Starting Connection
			connection->setExceptionListener(this);
			session = connection->createSession( Session::AUTO_ACKNOWLEDGE );		// Create a Session

			senderdestination = session->createQueue( serverURI );
			sender = session->createProducer(senderdestination);
			sender->setDeliveryMode( DeliveryMode::NON_PERSISTENT );

			receiverdestination = session->createQueue( clientURI );
			receiver = session->createConsumer(receiverdestination);
		}
		else
		{
			return false;
		}
		return true;
	}
	catch ( CMSException& error )
	{
		error.printStackTrace();
		cout << error.what()<< endl;
	}
	return false;
}
void KAConnection::sendMessage(string mymessage)
{				//sending a message to queue
	try
	{
		sendmessage = session->createTextMessage(mymessage);
		sender->send(sendmessage);
		delete sendmessage;
	}
	catch(const std::exception& error)
	{
		cout << error.what()<< endl;;
	}
}
bool KAConnection::fetchMessage(string& textmessage)
{		//fetch a message from queue with no wait
	Message* message = receiver->receive();				//return true if the message exist on queue
	if(message!=NULL)
	{											//return false if the messsage does not exist on queue
		receivedmessage = dynamic_cast< TextMessage* >(message);
		textmessage =  receivedmessage->getText();
		delete receivedmessage;
		return true;
	}
	else
	{
		return false;
	}
}
void KAConnection::setSenderDestination(Destination* mydestination)
{	//setting sender pointer
	senderdestination = mydestination;
}
Destination* KAConnection::getSenderDestination()
{						//gettin sender pointer
	return senderdestination;
}
MessageProducer* KAConnection::getSender()
{						//getting sender pointer
	return sender;
}
void KAConnection::setSender(MessageProducer* mysender)
{		//setting sender pointer
	sender = mysender;
}
TextMessage* KAConnection::getSendMessage()
{						//getting sendmessage pointer
	return sendmessage;
}
void KAConnection::setSendMessage(TextMessage* mymessage)
{			//setting sendmessage pointer
	this->sendmessage=mymessage;
}
Session* KAConnection::getSession()
{					//getting session pointer
	return session;
}
void KAConnection::setSesion(Session* mysession)
{ 		//setting session pointer
	session = mysession;
}
string KAConnection::getBrokerURI()
{					//getting Broker URI
	return brokerURI;
}
void KAConnection::setBrokerURI(string mybrokerURI)
{	//setting Broker URI
	brokerURI=mybrokerURI;
}
Connection* KAConnection::getConnection()
{				//getting Connection pointer
	return connection;
}
void KAConnection::setConnection(Connection* myconnection)
{		//setting connection pointer
	connection = myconnection;
}
MessageConsumer * KAConnection::getReceiver()
{						//getting receiver
	return receiver;
}
void KAConnection::setReciever(MessageConsumer* myreceiver)
{			//setting receiver
	receiver = myreceiver;
}
TextMessage* KAConnection::getReceivedMessage()
{						//getting receivedmesssage pointer
	return receivedmessage;
}
void KAConnection::setReceivedMessage(TextMessage* mymessage)
{			//setting receivedmesssage pointer
	this->receivedmessage=mymessage;
}
void KAConnection::setReceiverDestination(Destination* mydestination)
{
	receiverdestination = mydestination;
}
Destination* KAConnection::getReceiverDestination()
{
	return receiverdestination;
}
void KAConnection::close()
{			// Destroy all resources.
	// Close open resources such as session and connection.
	try
	{
		if( session != NULL ) session->close();
		if( connection != NULL ) connection->close();
	}
	catch ( CMSException& e )
	{
		e.printStackTrace();
	}
	try
	{
		if( session != NULL )
			delete session;
	}
	catch ( CMSException& e )
	{
		e.printStackTrace();
	}
	session = NULL;
	try
	{
		if( connection != NULL )
			delete connection;
	}
	catch ( CMSException& e )
	{
		e.printStackTrace();
	}
	connection = NULL;
	try
	{
		if( sender != NULL )
			delete sender;
	}
	catch ( CMSException& e )
	{
		e.printStackTrace();
	}
	sender = NULL;
	try
	{
		if( sendmessage != NULL )
			delete sendmessage;
	}
	catch ( CMSException& e )
	{
		e.printStackTrace();
	}
	sendmessage = NULL;
	try{
		if( senderdestination != NULL )
			delete senderdestination;
	}
	catch ( CMSException& e )
	{
		e.printStackTrace();
	}
	senderdestination = NULL;
	try
	{
		if( receiver != NULL )
			delete receiver;
	}catch ( CMSException& e )
	{
		e.printStackTrace();
	}
	receiver = NULL;
	try
	{
		if( receivedmessage != NULL )
			delete receivedmessage;
	}
	catch ( CMSException& e )
	{
		e.printStackTrace();
	}
	receivedmessage = NULL;
	try
	{
		if( receiverdestination != NULL )
			delete receiverdestination;
	}
	catch ( CMSException& e )
	{
		e.printStackTrace();
	}
	receiverdestination = NULL;
}
