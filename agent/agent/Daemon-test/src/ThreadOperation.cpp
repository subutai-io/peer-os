/*
 * ThreadOperation.cpp
 *
 *  Created on: Sep 5, 2013
 *      Author: qt-test
 */

#include "ThreadOperation.h"


ThreadOperation::ThreadOperation() {
	// TODO Auto-generated constructor stub
}

ThreadOperation::~ThreadOperation() {
	// TODO Auto-generated destructor stub
}

void ThreadOperation::runThread()
{


}

void ThreadOperation::threadFunction(Command command,DaemonConnection connection)
{

	int seqcount=0,seqnumber=0;
	pid=fork();
	if(pid==0)
	{
		sleep(1);
	}
	else if(pid>0)
	{
		ostringstream convert;
		convert << command.getRequestSeqnum();
		string o1 = convert.str()+".output";
		string e1 = convert.str()+".error";
		freopen(o1.c_str(),"w",stdout);
		freopen(e1.c_str(),"w",stderr);
		ifstream output(o1.c_str());
		ifstream error(e1.c_str());
		string envParam="";
		uid.getIDs(*ruid,*euid,command.getRunAs());
		uid.do_setuid(*euid);

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

		while(!(getline(output,read).eof()) || !(getline(error,read1).eof()))
		{
			if(read!="")
			{
				stdOut = stdOut + read + "\n";
			}
			if(read1!="")
			{
				stdErr = stdErr + read1 + "\n";
			}
			seqcount=seqcount+1;
			if(seqcount==3)
			{
				sendout = response.createResponseMessage("TESTuuid",command.getRequestSeqnum(),seqnumber,stdErr,stdOut);
				connection.sendMessage(sendout);
				stdOut ="";
				seqcount=0;
				seqnumber=seqnumber+1;
			}

		}
		/*while(getline(error,read))
		{
			stdErr = stdErr + read + "\n";
			seqcount=seqcount+1;
			if(seqcount==3)
			{
				sendout = response.createResponseMessage("TESTuuid",command.getRequestSeqnum(),seqnumber,stdErr,stdOut);
				connection.sendMessage(sendout);
				stdErr ="";
				seqcount=0;
				seqnumber=seqnumber+1;
			}

		}*/
		if(seqcount<3 && seqcount!=0)
		{
		sendout = response.createResponseMessage("TESTuuid",command.getRequestSeqnum(),seqnumber,stdErr,stdOut);
		seqnumber=seqnumber+1;
		connection.sendMessage(sendout);
		}

		//exit
		sendout = response.createExitMessage("TESTuuid",command.getRequestSeqnum(),seqnumber);
		connection.sendMessage(sendout);

		uid.undo_setuid(*ruid);
		kill(pid,SIGKILL);
	}
}
