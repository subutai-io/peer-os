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
 *  @brief     SubutaiResponsePack.h
 *  @class     SubutaiResponsePack.h
 *  @details   SubutaiResponsePack class is designed for creating message packets to be sent to ActiveMQ Broker.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.1.0
 *  @date      Sep 13, 2014
 */
#ifndef SUBUTAIRESPONSEPACK_H_
#define SUBUTAIRESPONSEPACK_H_
#include "SubutaiResponse.h"
using namespace std;
class SubutaiResponsePack : public SubutaiResponse
{
public:
	SubutaiResponsePack();
	virtual ~SubutaiResponsePack();
	string createResponseMessage(string,int,int,int,string,string,string);
	string createExitMessage(string,int, int, int,string,int);
	string createTerminateMessage(string,string,int, int);
	string createInQueueMessage(string,string);
	string createHeartBeatMessage(string,string);
    string createPsResponse(string, string);
	string createTimeoutMessage(string,int,int,int,string,string,string);
	string createInotifyMessage(string,string,string,string);
	string createInotifyShowMessage(string, string, vector<string>);
	string setInotifyResponse(string, string);
	string unsetInotifyResponse(string, string);
private:
	string sendout;
	SubutaiHelper helper;
};
#endif /* SUBUTAIRESPONSEPACK_H_ */
