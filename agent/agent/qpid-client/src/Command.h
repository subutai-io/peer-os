/*
 * Command.h
 *
 *  Created on: Aug 28, 2013
 *      Author: Emin inal
 */

#ifndef COMMAND_H_
#define COMMAND_H_

#include <iostream>
#include <jsoncpp/json.h>
#include <string>
#include <vector>
using namespace std;
using std::stringstream;
using std::string;

class Command
{
public:
	Command( void );
	virtual ~Command( void );

	string& getFooPath();
	void setFooPath(const string& FooPath);
	string& getBarValue();
	void setBarValue(const string& BarValue);
	void setArguments(vector<string> myvector);
	vector<string>& getArguments();
	string& getUuid();
	void setUuid(const string& uu_id);
	string& getCwd();
	void setCwd(const string& cw_d);
	string& getProgram();
	void setProgram(const string& pro_gram);
	int getRequestSeqnum() ;
	void setRequestSeqnum(int requestSeqnum);
	string& getRunAs() ;
	void setRunAs(const string& runAs);
	string& getStderr() ;
	void setStderr(const string& std_err);
	string& getStdout() ;
	void setStdout(const string& std_out);
	string& getType() ;
	void setType(const string& type);

	void Serialize(Json::Value& root , std::string& output  );
	bool Deserialize( Json::Value& root, std::string& input );

private:
	string        	type;
	string        	cwd;
	string		    uuid;
	int   		  	request_seqnum;
	string       	stdout;
	string        	stderr;
	string       	program;
	string        	run_as;
	vector<string>	arguments;
	string        	FOO_PATH;
	string 			BAR_VALUE;
};
#endif /* COMMAND_H_ */

