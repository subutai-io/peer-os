/*
 * DaemonConnection.cpp
 *
 *  Created on: Sep 4, 2013
 *      Author: Emin Inal
 */

#include "DaemonConnection.h"

DaemonConnection::DaemonConnection(string myurl, string options): Connection(myurl,options){

}
DaemonConnection::DaemonConnection(){

}
DaemonConnection::~DaemonConnection() {
	// TODO Auto-generated destructor stub
}
string DaemonConnection::getMessage(){
	message = receiver.fetch(Duration::FOREVER);
	return message.getContent();
}
void DaemonConnection::sendMessage(string mymessage){
	message.setContent(mymessage);
	sender.send(message);
	session.acknowledge();
}
Session & DaemonConnection::getMySession(){
	return session;
}
void DaemonConnection::setMySesion(Session mysession){
	session = mysession;
}
Sender & DaemonConnection::getMySender(){
	return sender;
}
void DaemonConnection::SetMySender(Sender mysender){
	sender = mysender;
}
Receiver & DaemonConnection::getMyReceiver(){
	return receiver;
}
void DaemonConnection::SetMyReciever(Receiver myreceiver){
	receiver = myreceiver;
}
bool DaemonConnection::openMySession(string mac_id){

	try {
		this->open();
		session = this->createSession();
		receiver = session.createReceiver(mac_id+"; {create: always, delete:always}");
		sender = session.createSender("service_queue; {create: always}");
		return true;
	}
	catch(const std::exception& error)
	{
		cout << error.what()<< endl;
		this->close();
		return false;
	}
}








