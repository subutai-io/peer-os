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
#include <queue>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <functional>
#include <iostream>
#include <fstream>
#include "pugixml.hpp"

#include "SubutaiResponsePack.h"
#include "SubutaiEnvironment.h"

#define MAX_MSG_NUM 300
using namespace std;
using std::string;

class SubutaiConnection: public mosqpp::mosquittopp {
public:
	SubutaiConnection(const char*, const char*, const char*, const char*,
			const char*, int, SubutaiEnvironment*, SubutaiLogger*);
	virtual ~SubutaiConnection();
	bool openSession();
	bool sendMessage(string,string topic = "");
	bool checkMessageStatus();
	bool addMessageToQueue(	SubutaiCommand*);
	bool addMessageToExecQueue(SubutaiCommand*,	string&);
	SubutaiCommand* getMessage();
	bool reConnect();
	string getID();
	SubutaiCommand* getExecutionMessage();
	bool checkExecutionMessageStatus();
	void initializeQueue();

private:
	const char* host;
	const char* id;
	const char* subscribedTopic;
	const char* publishedTopic;
	const char* broadcastTopic;
	int port;
	int keepalive;
	bool receivedMessage;
	bool connectionStatus;
	string message;
	int bufferSize;
	string certpath;
	queue<SubutaiCommand*> msg_queue;
	queue<SubutaiCommand*> execution_queue;
	SubutaiLogger* logger;
	SubutaiResponsePack response;
	SubutaiEnvironment* environment;
	SubutaiHelper helper;
	string commandQueuePath = "/etc/subutai-agent/commandQueue.txt";

	void on_connect(int);
	void on_disconnect(int);
	void on_publish(int);
	void on_subscribe(int);
	void on_message(const struct mosquitto_message*);
};
#endif /* SUBUTAICONNECTION_H_ */

