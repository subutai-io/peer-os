/*
 * ThreadOperation.h
 *
 *  Created on: Sep 5, 2013
 *      Author: qt-test
 */

#ifndef THREADOPERATION_H_
#define THREADOPERATION_H_
#include <boost/threadpool/boost/threadpool.hpp>
#include <boost/thread/mutex.hpp>
#include <fstream>
#include <signal.h>
#include "UserID.h"
#include "Command.h"
#include "DaemonConnection.h"
#include "responsePack.h"
using namespace std;
using namespace boost::threadpool;
using namespace boost::pthread;
class ThreadOperation {
public:
	ThreadOperation();
	virtual ~ThreadOperation();
	void threadFunction(Command , DaemonConnection );
	void runThread();
private:
	UserID uid;
	responsePack response;
	pid_t pid;
	string argument,exec,sendout;
	string stdOut,read1,read2,stdErr,read;
	uid_t *euid, *ruid;
};

#endif /* THREADOPERATION_H_ */
