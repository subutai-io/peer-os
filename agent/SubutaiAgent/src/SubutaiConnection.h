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
 *  @brief     SubutaiConnection.h
 *  @class     SubutaiConnection.h
 *  @details   SubutaiConnection class is designed for communication with MQTT ACtiveMQ broker.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.1.0
 *  @date      Sep 13, 2014
 */
#ifndef SUBUTAICONNECTION_H_
#define SUBUTAICONNECTION_H_

#include "mosquittopp.h"
#include <iostream>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <functional>
#include "pugixml.hpp"
using namespace std;
using std::string;

class SubutaiConnection : public mosqpp::mosquittopp
{
public:
	SubutaiConnection(const char*,const char*,const char*,const char*,const char*,int);
	virtual ~SubutaiConnection();
	bool openSession();
	bool sendMessage(string, string topic = "");
	bool checkMessageStatus();
	void resetMessageStatus();
	void setMessage(string);
	string getMessage();
	bool reConnect();
	string getID();

private:
	const char*	host;
	const char* id;
	const char* subscribedTopic;
	const char* publishedTopic;
	const char* broadcastTopic;
	int	port;
	int keepalive;
	bool reveivedMessage;
	bool connectionStatus;
	string messsage;
	int bufferSize;
	string certpath;

	void on_connect(int);
	void on_disconnect(int);
	void on_publish(int);
	void on_subscribe(int);
	void on_message(const struct mosquitto_message*);
};
#endif /* SUBUTAICONNECTION_H_ */

