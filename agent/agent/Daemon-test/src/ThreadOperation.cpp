/*
 * ThreadOperation.cpp
 *
 *  Created on: Sep 5, 2013
 *      Author: Bilal Bal
 */

#include "ThreadOperation.h"

ThreadOperation::ThreadOperation() {
	// TODO Auto-generated constructor stub
}

ThreadOperation::~ThreadOperation() {
	// TODO Auto-generated destructor stub
}

bool ThreadOperation::threadFunction(Command command,DaemonConnection connection)
{

	int seqcount=0,seqnumber=0;
	pid=fork();

	if(pid==0)
	{
		sleep(1);
		return false;
	}
	else if(pid>0)
	{
		ostringstream convert;

		string envParam="";
		convert << command.getRequestSeqnum();

	//	cout << command.getRequestSeqnum() << endl;
		//cout << convert << endl ;
//
		string o1 = convert.str()+".output";
		string e1 = convert.str()+".error";

		freopen(o1.c_str(),"w",stdout);
		freopen(e1.c_str(),"w",stderr);
		ifstream output(o1.c_str());
		ifstream error(e1.c_str());

		if(uid.getIDs(this->ruid, this->euid,command.getRunAs()))
		{
			uid.do_setuid(this->euid);
		}

		/*for(std::list<pair<string,string> >::iterator it = command.getEnv().begin(); it != command.getEnv().end(); it++ ){
			cout<<it->first.c_str()<<" "<< it->second.c_str()<<endl;
		}
		 */

		if ((chdir(command.getCwd().c_str())) < 0) {

			exit(EXIT_FAILURE);
		}

		for(unsigned int i=0;i<command.getArguments().size();i++)
		{
			argument = argument + command.getArguments()[i] + " ";
		}

		exec = command.getProgram() + " " + argument;
		system(exec.c_str());
//
		while(!(getline(output,read).eof()) || !(getline(error,read1).eof())) //read 3 lines of both files
		{
//			if(read!="")
//			{
//				stdOut = stdOut + read + "\n";
//			}
//			if(read1!="")
//			{
//				stdErr = stdErr + read1 + "\n";
//			}
//			seqcount=seqcount+1;
//			if(seqcount==3)
//			{
//				sendout = response.createResponseMessage(command.getUuid(),command.getRequestSeqnum(),seqnumber,stdErr,stdOut);
//				connection.sendMessage(sendout);
//				stdOut ="";
//				seqcount=0;
//				seqnumber=seqnumber+1;
//				stdErr = "";
//				stdOut = "";
//			}
//
		}

//			if(seqcount<3 && seqcount!=0) //if left 1 or 2 lines from error and out files. send them
//		{
//			sendout = response.createResponseMessage(command.getUuid(),command.getRequestSeqnum(),seqnumber,stdErr,stdOut);
//			seqnumber=seqnumber+1;
//			connection.sendMessage(sendout);
//			stdErr = "";
//			stdOut = "";
//		}
//
//		//exit send done response
//		sendout = response.createExitMessage(command.getUuid(),command.getRequestSeqnum(),seqnumber);
//		connection.sendMessage(sendout);

		uid.undo_setuid(ruid);
		kill(pid,SIGKILL);
		return true;
	}
}
