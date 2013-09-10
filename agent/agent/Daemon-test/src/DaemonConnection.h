/*
 * DaemonConnection.h
 *
 *  Created on: Sep 4, 2013
 *      Author: Emin INAL
 */
#ifndef DAEMONCONNECTION_H_
#define DAEMONCONNECTION_H_

#include <qpid/messaging/Address.h>
#include <qpid/messaging/Connection.h>
#include <qpid/messaging/Message.h>
#include <qpid/messaging/Receiver.h>
#include <qpid/messaging/Sender.h>
#include <qpid/messaging/Session.h>
#include <cstdlib>
#include <iostream>
#include <sstream>
#include <string>
#include <fstream>
using namespace std;
using namespace qpid::messaging;
using namespace qpid::types;
using std::stringstream;
using std::string;

class DaemonConnection : public  Connection
{

public:
	DaemonConnection();
	DaemonConnection(string myurl, string options);
	~DaemonConnection();

	Sender & getMySender();
	void SetMySender(Sender mysender);
	Receiver & getMyReceiver();
	void SetMyReciever(Receiver  myreceiver);
	Session & getMySession();
	void setMySesion(Session mysession);
	Message & getMyMessage();
	void setMyMessage(Message mymessage);
	string getMessage();
	void sendMessage(string mymessage);
	bool openMySession(string mac_id);
private:
	Session 	session;
	Receiver 	receiver;
	Sender		sender;
	Message		message;
};
#endif /* DAEMONCONNECTION_H_ */
