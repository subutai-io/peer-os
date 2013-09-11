/*
 * ThreadOperation.h
 *
 *  Created on: Sep 5, 2013
 *      Author: qt-test
 */

#ifndef THREADOPERATION_H_
#define THREADOPERATION_H_
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <syslog.h>
#include <stdarg.h>

#include <boost/threadpool/boost/threadpool.hpp>
#include <boost/thread/mutex.hpp>
#include <fstream>
#include <signal.h>
#include "UserID.h"
#include "Command.h"
#include "DaemonConnection.h"
#include "ResponsePack.h"

using namespace std;
using namespace boost::threadpool;
using namespace boost::pthread;

class ThreadOperation {
public:
	ThreadOperation();
	virtual ~ThreadOperation();
	bool threadFunction(Command , DaemonConnection );
private:
	UserID uid;
	ResponsePack response;
	pid_t pid;
	string argument,exec,sendout;
	string stdOut,read1,read2,stdErr,read;
	uid_t  euid, ruid;
};
#endif /* THREADOPERATION_H_ */
