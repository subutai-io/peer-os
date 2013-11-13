/*
 *============================================================================
 Name        : KAResponsePack.h
 Author      : Bilal BAL & Emin INAL
 Date		 : Sep 4, 2013
 Version     : 1.0
 Copyright   : Your copyright notice
 Description : KAResponsePack class is designed for sending response chunk messages to Broker
==============================================================================
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
