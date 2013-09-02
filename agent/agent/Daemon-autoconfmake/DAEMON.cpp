//============================================================================
// Name        : DAEMON.cpp
// Author      : Bilal BAL
// Version     :
// Copyright   : Your copyright notice
// Description : DAEMON
//============================================================================
#include <boost/threadpool/boost/threadpool.hpp>
#include <boost/thread/mutex.hpp>
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <stdio.h>
#include <qpid/messaging/Address.h>
#include <qpid/messaging/Connection.h>
#include <qpid/messaging/Message.h>
#include <qpid/messaging/Receiver.h>
#include <qpid/messaging/Sender.h>
#include <qpid/messaging/Session.h>
#include <pthread.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <syslog.h>
#include <string.h>
#include <signal.h>
#include <pthread.h>
#include <stdarg.h>
#include <iostream>
#include <fstream>
#include <string>
#include <syslog.h>




#define SHMSZ     27

//dynamically creating fork
using namespace std;
using namespace boost::threadpool;
using namespace boost::pthread;
using namespace qpid::messaging;
using namespace qpid::types;


boost::mutex accesss;

va_list arglist;


pid_t *create_fork(pid_t *pids,int *sizeof_pids)
{
	pids = (pid_t*)malloc((*sizeof_pids+1)*sizeof(pid_t));
	*sizeof_pids=*sizeof_pids+1;
	return pids;
}
pid_t *kill_fork(pid_t *pids,int *sizeof_pids)
{
	setlogmask (LOG_UPTO(LOG_NOTICE));
	openlog("DAEMON",LOG_CONS|LOG_PID|LOG_NDELAY,LOG_LOCAL1);
	pids=(pid_t*)realloc(pids,(*sizeof_pids-1)*(sizeof(pid_t)));
	*sizeof_pids =*sizeof_pids-1;

	exit(EXIT_SUCCESS);
	return pids;
}
void envparam(char *envp[])
{
	char **env;
	for (env = envp; *env != 0; env++)
	{
		char* thisEnv = *env;
		printf("%s\n", thisEnv);
	}
}

void runThread(string command, Sender &sender)
{
	pid_t pid;
	pid=fork();
	if(pid==0)
	{
		sleep(1);
	}
	else if(pid>0)
	{
		freopen("output.file","w",stdout);
		freopen("error.file","w",stderr);
		const char *txt = command.c_str();
		cout<<command<<endl;
		system(txt);
		ifstream output("output.file");
		ifstream error("error.file");
		string readed,read,er;

		Variant::Map content;
		Message message;
		while(getline(output,read))
		{
			readed = readed + read + "\n";
		}
		cout<<readed<<endl;
		content["command"]="OUTPUT";
		content["par1"]=readed;
		content["par2"]="DAEMON1";
		while(getline(error,read))
				{
					er = er + read + "\n";
				}
		content["error"]=er;




		encode(content, message);
		sender.send(message,true);

		kill(pid,SIGKILL);
		//exit(EXIT_SUCCESS);
	}
}

int main(int argc,char *argv[],char *envp[]) {


	const char* url = argc>1 ? argv[1] : "amqp:tcp:172.16.25.16:5672";
	std::string connectionOptions = argc > 2 ? argv[2] : "";

	Connection connection(url, connectionOptions);
	Message message;
	Variant::Map content;
	Variant::Map content1;
	Sender sender;
	Receiver receiver;
	Session session;
	ifstream mac("/root/mac.txt");
	string macc,mac2;
	getline(mac,macc);

	mac2 = macc + ";{create: always,delete: always}";
	cout<<macc<<endl;
	try
	{
		connection.open();
		session = connection.createSession();
		sender = session.createSender("service_queue;{create:always}");
		receiver = session.createReceiver(mac2);

		content["command"]="REG";
		content["par1"] = macc ;
		//content["uuid"] = Uuid(true);
		encode(content, message);
		sender.send(message,true);
		session.acknowledge();

		message = receiver.fetch();
		//cout<<message.getContent();
		/*decode(receiver.fetch(), content1);*/
	}
	catch(const std::exception& error)
	{
		std::cout << error.what() << std::endl;
		connection.close();
	}


	pid_t *pids;
	int sizeof_pids;
	sizeof_pids=0;
	int x=10;
	int sid;
	pool tp(x);

	/* Change the file mode mask */
	umask(0);

	/* Create a new SID for the child process */


	/* Change the current working directory */
	if ((chdir("/home/qt-test/workspace/DAEMON/Debug")) < 0) {

		exit(EXIT_FAILURE);
	}

	/* Close out the standard file descriptors */

	while(true)
	{
		try
		{
			content.clear();
			content1.clear();

			decode(receiver.fetch(), content1);
			cout<<content1<<endl;
			string command;
			command = command + content1["command"].asString() +" " + content1["par1"].asString() + " " + content1["par2"].asString()+ " " + content1["par3"].asString()+ " " + content1["par4"].asString();
			tp.schedule(boost::bind(&runThread,command,sender));


		}
		catch(const std::exception& error)
		{
			std::cout << error.what() << std::endl;
		}

	}

	close(STDIN_FILENO);
	close(STDOUT_FILENO);
	close(STDERR_FILENO);
	connection.close();

	return 0;
}
