/**
 *  @brief     KAResponse.h
 *  @class     KAResponse.h
 *  @details   KAResponse class is designed for marshaling and unmarshalling response messages.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.0
 *  @date      Aug 29, 2013
 *  @copyright GNU Public License.
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
	string& getHostname();
	string& getMacAddress();
	string& getTaskUuid();
	bool& 	getIsLxc();
	vector<string>& getIps();
	string& getSource();
	void setSource(const string&);
	void setHostname(const string&);
	void setMacAddress(const string&);
	void setTaskUuid(const string&);
	void setIsLxc(bool);
	void setIps(vector<string>);
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
	string			taskUuid;
	bool			isLxc;
	string			macAddress;
	string			hostname;
	vector<string>  ips;
	string			source;
};
#endif /* KARESPONSE_H_ */
