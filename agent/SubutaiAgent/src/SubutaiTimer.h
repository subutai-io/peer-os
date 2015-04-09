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
/**
 *  @brief     SubutaiTimer.h
 *  @class     SubutaiTimer.h
 *  @details   SubutaiTimer Class is designed for periodical tasks.
 *  @author    Mikhail Savochkin
 *  @author    Ozlem Ceren Sahin
 *  @version   1.1.0
 *  @date      Nov 3, 2014
 */
#ifndef SUBUTAITIMER_H_
#define SUBUTAITIMER_H_
#include <syslog.h>
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <cstdlib>
#include <stdio.h>
#include <unistd.h>
#include <sstream>
#include <list>
#include <lxc/lxccontainer.h>
#include "pugixml.hpp"
#include <boost/uuid/uuid.hpp>
#include <boost/uuid/uuid_generators.hpp>
#include <boost/uuid/uuid_io.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/thread/thread.hpp>
#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/ini_parser.hpp>
#include "SubutaiCommand.h"
#include "SubutaiResponse.h"
#include "SubutaiUserID.h"
#include "SubutaiThread.h"
#include "SubutaiConnection.h"
#include "SubutaiWatch.h"
#include "SubutaiEnvironment.h"
#include "SubutaiContainerManager.h"
using namespace std;
using std::stringstream;
using std::string;
#define LOG_HEARTBEAT_PERIOD 1

class SubutaiTimer {
public:
	SubutaiTimer(SubutaiLogger, SubutaiEnvironment*, SubutaiContainerManager*,
			SubutaiConnection*);
	virtual ~SubutaiTimer(void);
	bool checkExecutionTimeout(unsigned int*, bool*,
			unsigned int*, unsigned int*);
	void sendHeartBeat(bool*);
	bool checkHeartBeatTimer(bool*);
	bool checkCommandQueueInfoTimer();
	bool checkIfDestroyCommandInProgress();
	bool checkIfCloneCommandInProgress();

private:
	SubutaiEnvironment* environment;
	SubutaiContainerManager* containerManager;
	SubutaiResponsePack* response;
	SubutaiConnection* connection;

	boost::posix_time::ptime start;
	boost::posix_time::ptime startQueue;
	unsigned int exectimeout;
	unsigned int queuetimeout;
	unsigned int startsec;
	unsigned int startsecQueue;
	bool overflag;
	bool overflagQueue;
	unsigned int count;
	unsigned int countQueue;
	unsigned int numHeartbeatmod5;

	SubutaiLogger logMain;
	SubutaiHelper helper;
};
#endif /* SUBUTAICONTAINER_H_ */

