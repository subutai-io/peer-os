/*
 *============================================================================
 Name        : KAThread.cpp
 Author      : Bilal Bal
 Date		 : Sep 5, 2013
 Version     : 1.0
 Copyright   : Your copyright notice
 Description : KAThread Class is designed for management of threads. Each Threads concurrently execute and command.
==============================================================================
 */
#ifndef KATHREAD_H_
#define KATHREAD_H_
#include <pthread.h>
#include <list>
#include <boost/thread/mutex.hpp>
#include <boost/thread/thread.hpp>
#include "KAUserID.h"
#include "KACommand.h"
#include "KAConnection.h"
#include "KAResponsePack.h"
#include "KAStreamReader.h"
#include <boost/interprocess/shared_memory_object.hpp>
#include <boost/interprocess/mapped_region.hpp>
#include <boost/interprocess/managed_shared_memory.hpp>
#include <boost/interprocess/ipc/message_queue.hpp>
#include <boost/thread.hpp>

using namespace std;
using namespace boost::pthread;
using namespace boost::interprocess;
using namespace boost;

class KAThread
{
public:
	KAThread();
	virtual ~KAThread();
	bool threadFunction(message_queue*,KACommand*);			//Execute command concurrently
	bool checkCWD(KACommand*);
	bool checkUID(KACommand*);
	string createExecString(KACommand*);
	int toInteger(string*);
	string toString(int);
	KAUserID& getUserID();
	KAResponsePack& getResponse();
	static string getProcessPid(const char*);
	static void taskTimeout(message_queue*,bool*,KACommand*,string*,string*,string*,int*);
	static void capture(message_queue*,KACommand*,KAStreamReader*,bool*,mutex*,string*,string*,string*,int*);
	static void checkAndWrite(message_queue*,KAStreamReader*,string*,string*,KACommand*,string*,int*);
	static void checkAndSend(message_queue*,KAStreamReader*,string*,string*,KACommand*,string*,int*);
	static void lastCheckAndSend(message_queue *,KACommand*,string*,string*,string*,int*);
	int optionReadSend(message_queue*,KACommand*,KAStreamReader*,KAStreamReader*,int);
private:
	KAUserID uid;
	pid_t pid;
	KAResponsePack response;
	string argument,exec,sendout,environment;
	uid_t euid, ruid;
};
#endif /* KATHREAD_H_ */
