/**
 *  @brief     KAResponsePack.h
 *  @class     KAResponsePack.h
 *  @details   KAResponsePack class is designed for creating message packets to be sent to ActiveMQ Broker.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.0
 *  @date      Aug 29, 2013
 *  @copyright GNU Public License.
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
	string createResponseMessage(string,string,int,int,string,string,string,string);			//Creating Response chunk messasge
	string createExitMessage(string,string, int, int,string,string,int);						//Creating Response Exit_DONE messasge
	string createRegistrationMessage(string,string,string,bool);  								//Creating Registration Message
	string createTerminateMessage(string,int,string); 											//Creating Terminate_DONE Message
	string createFailTerminateMessage(string,int,string);										//Creating Fail Terminate Message
	string createHeartBeatMessage(string,int,string,string,bool,string,string);					//Creating HeartBeat Message
	string createTimeoutMessage(string,string,int,int,string,string,string,string);
private:
	string sendout;
};
#endif /* KARESPONSEPACK_H_ */
