/*
 * Response.h
 *
 *  Created on: Aug 29, 2013
 *      Author: Emin inal
 */

#ifndef RESPONSE_H_
#define RESPONSE_H_

#include <iostream>
#include <jsoncpp/json.h>
#include <string>
using namespace std;
using std::stringstream;
using std::string;

class Response
{
public:
	Response( void );
	virtual ~Response( void );

	string& getType() ;
	void setType(const string& type);
	string& getUuid();
	void setUuid(const string& uu_id);
	int getRequestSeqnum() ;
	void setRequestSeqnum(int requestSeqnum);
	int getResponseSeqnum() ;
	void setResponseSeqnum(int responseSeqnum);
	string& getStderr() ;
	void setStderr(const string& std_err);
	string& getStdout() ;
	void setStdout(const string& std_out);

	void Serialize(Json::Value& root , std::string& output  );
	bool Deserialize( Json::Value& root, std::string& input );

private:
	string        	type;
	string		    uuid;
	int   		  	request_seqnum;
	int				response_seqnum;
	string       	stdout;
	string        	stderr;
};
#endif /* RESPONSE_H_ */
