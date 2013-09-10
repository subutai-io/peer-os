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
#include <cstdlib>
#include <sstream>
#include <list>

using namespace std;
using std::stringstream;
using std::string;

class Command
{
public:
	Command( void );
	virtual ~Command( void );
	list<pair<string,string> >& getEnv();
	void setEnv(list<pair<string,string> >& envr);
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
	void setType(const string& mac);
	string& getMacID() ;
	void setMacID(const string& mac);
	void Serialize(string& output);
	bool Deserialize(string input);
private:
	string        	type;
	string        	cwd;
	string		    uuid;
	string			macID;
	int			 	request_seqnum;
	string       	stdout;
	string        	stderr;
	string       	program;
	string        	run_as;
	vector<string>	arguments;
	list<pair<string,string> > env;
};
#endif /* COMMAND_H_ */
