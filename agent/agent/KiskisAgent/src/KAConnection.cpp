#include "KAConnection.h"
/**
 *  \details   Default constructor of KAConnection class.
 */
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
/**
 *  \details   Default destructor of KAConnection class.
 */
KAConnection::~KAConnection()
{
	//closeConnection();								//destroy all pointers
}
/**
 *  \details   openSession method is one of the most important function of the KAConnection class.
 *  		   It tries to open a connection to ActiveMQ Broker.
 *  		   if this operation is successfully done it returns true. Otherwise it returns false.
 */
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
/**
 *  \details   This method sends the given strings to ActiveMQ Broker.
 */
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
/**
 *  \details   fetchMessage method is one of the most important function of the KAConnection class.
 *  		   It tries to fetch a message from ActiveMQ Broker in blocking mode.
 *  		   It reads the message from SERVICE_QUEUE sequentially
 *  		   if new message comes, it returns true and returning reference textmessage which has a real message.
 *  		   otherwise it just return false.
 */
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
/**
 *  \details   setting "senderdestination" private variable of the KAConnection class.
 */
void KAConnection::setSenderDestination(Destination* mydestination)
{	//setting sender pointer
	senderdestination = mydestination;
}
/**
 *  \details   getting "senderdestination" private variable of the KAConnection class.
 */
Destination* KAConnection::getSenderDestination()
{						//gettin sender pointer
	return senderdestination;
}
/**
 *  \details   getting "sender" private variable of the KAConnection class.
 */
MessageProducer* KAConnection::getSender()
{						//getting sender pointer
	return sender;
}
/**
 *  \details   setting "sender" private variable of the KAConnection class.
 */
void KAConnection::setSender(MessageProducer* mysender)
{		//setting sender pointer
	sender = mysender;
}
/**
 *  \details   getting "sendmessage" private variable of the KAConnection class.
 */
TextMessage* KAConnection::getSendMessage()
{						//getting sendmessage pointer
	return sendmessage;
}
/**
 *  \details   setting "sendmessage" private variable of the KAConnection class.
 */
void KAConnection::setSendMessage(TextMessage* mymessage)
{			//setting sendmessage pointer
	this->sendmessage=mymessage;
}
/**
 *  \details   getting "session" private variable of the KAConnection class.
 */
Session* KAConnection::getSession()
{					//getting session pointer
	return session;
}
/**
 *  \details   setting "session" private variable of the KAConnection class.
 */
void KAConnection::setSesion(Session* mysession)
{ 		//setting session pointer
	session = mysession;
}
/**
 *  \details   getting "brokerURI" private variable of the KAConnection class.
 */
string KAConnection::getBrokerURI()
{					//getting Broker URI
	return brokerURI;
}
/**
 *  \details   setting "brokerURI" private variable of the KAConnection class.
 */
void KAConnection::setBrokerURI(string mybrokerURI)
{	//setting Broker URI
	brokerURI=mybrokerURI;
}
/**
 *  \details   getting "connection" private variable of the KAConnection class.
 */
Connection* KAConnection::getConnection()
{				//getting Connection pointer
	return connection;
}
/**
 *  \details   setting "connection" private variable of the KAConnection class.
 */
void KAConnection::setConnection(Connection* myconnection)
{		//setting connection pointer
	connection = myconnection;
}
/**
 *  \details   getting "receiver" private variable of the KAConnection class.
 */
MessageConsumer * KAConnection::getReceiver()
{						//getting receiver
	return receiver;
}
/**
 *  \details   setting "receiver" private variable of the KAConnection class.
 */
void KAConnection::setReciever(MessageConsumer* myreceiver)
{			//setting receiver
	receiver = myreceiver;
}
/**
 *  \details   getting "receivedmessage" private variable of the KAConnection class.
 */
TextMessage* KAConnection::getReceivedMessage()
{						//getting receivedmesssage pointer
	return receivedmessage;
}
/**
 *  \details   setting "receivedmessage" private variable of the KAConnection class.
 */
void KAConnection::setReceivedMessage(TextMessage* mymessage)
{			//setting receivedmesssage pointer
	this->receivedmessage=mymessage;
}
/**
 *  \details   setting "receiverdestination" private variable of the KAConnection class.
 */
void KAConnection::setReceiverDestination(Destination* mydestination)
{
	receiverdestination = mydestination;
}
/**
 *  \details   getting "receiverdestination" private variable of the KAConnection class.
 */
Destination* KAConnection::getReceiverDestination()
{
	return receiverdestination;
}
/**
 *  \details   This method close the connections and session to the ActiveMQ Broker
 *   		   It also clear the all pointers in the called KAConnection instance.
 */
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
