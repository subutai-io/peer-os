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
 *  @brief     KAResponsePack.h
 *  @class     KAResponsePack.h
 *  @details   KAResponsePack class is designed for creating message packets to be sent to ActiveMQ Broker.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.0.4
 *  @date      May 8, 2014
 */
#ifndef KARESPONSEPACK_H_
#define KARESPONSEPACK_H_
#include "KAResponse.h"
using namespace std;
class KAResponsePack : public KAResponse
{
public:
	KAResponsePack();
	virtual ~KAResponsePack();
	string createResponseMessage(string,int,int,int,string,string,string,string);			//Creating Response chunk messasge
	string createExitMessage(string,int, int, int,string,string,int);						//Creating Response Exit_DONE messasge
	string createRegistrationMessage(string,string,string,string);  								//Creating Registration Message
	string createTerminateMessage(string,int,string,string); 											//Creating Terminate_DONE Message
	string createFailTerminateMessage(string,int,string,string);										//Creating Fail Terminate Message
	string createInQueueMessage(string,string);	//Creating IN_QUEUE Message
	string createHeartBeatMessage(string,int,string,string,string,string,string);					//Creating HeartBeat Message
	string createTimeoutMessage(string,int,int,int,string,string,string,string);
private:
	string sendout;
};
#endif /* KARESPONSEPACK_H_ */
