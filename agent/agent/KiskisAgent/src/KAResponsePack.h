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
	string createResponseMessage(string,string,int,int,string,string);			//Creating Response chunk messasge
	string createExitMessage(string,string, int, int);							//Creating Response Exit messasge
	string createRegistrationMessage(string uuid);								//Creating Registration Message
	string createTerminateMessage(string,int); 									//Creating Terminate Message
	string createHeartBeatMessage(string,int);									//Creating HeartBeat Message
	string createTimeoutMessage(string,string,int,int,string,string);
private:
	string sendout;
};
#endif /* KARESPONSEPACK_H_ */
