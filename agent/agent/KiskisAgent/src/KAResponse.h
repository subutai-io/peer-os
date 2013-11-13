/*
 *============================================================================
 Name        : KAResponse.h
 Author      : Emin INAL
 Date		 : Aug 29, 2013
 Version     : 1.0
 Copyright   : Your copyright notice
 Description : KAResponse class is designed for marshaling and unmarshalling response messages
==============================================================================
 */
#ifndef KARESPONSE_H_
#define KARESPONSE_H_

#include <syslog.h>
#include <iostream>
#include <jsoncpp/json.h>
#include <string>
#include <fstream>
using namespace std;
using std::stringstream;
using std::string;

class KAResponse
{
public:
	KAResponse( void );
	virtual ~KAResponse( void );
	string& getType();
	string& getUuid();
	int getRequestSequenceNumber();
	int getResponseSequenceNumber();
	int getExitCode();
	string& getStandardError();
	string& getStandardOutput();
	string& getPid();
	void setPid(const string&);
	void setType(const string&);
	void setUuid(const string&);
	void setRequestSequenceNumber(int);
	void setResponseSequenceNumber(int);
	void setStandardError(const string&);
	void setStandardOutput(const string&);
	void setExitCode(int);
	void serialize(string&);						//Serializing a Chunk Response message to a Json String
	void serializeDone(string&);					//Serializing a Last Done Response message to a Json string
	bool deserialize(string&);						//Deserializing a Json string to Response instance
	void clear();
private:
	string        	type;
	string		    uuid;
	int			 	requestSequenceNumber;
	int			 	responseSequenceNumber;
	int				exitCode;
	string			pid;
	string       	stdOut;
	string        	stdErr;
};
#endif /* KARESPONSE_H_ */
