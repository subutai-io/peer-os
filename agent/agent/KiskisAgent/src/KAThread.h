/**
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    @copyright 2013 Safehaus.org
 */
/**
 *  @brief     KAThread.h
 *  @class     KAThread.h
 *  @details   KAThread Class is designed to handle executions.
 *  		   Each Execution runs concurrently and does the given command job.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.0.1
 *  @date      Dec 17, 2013
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
#include "KALogger.h"
#include <boost/interprocess/shared_memory_object.hpp>
#include <boost/interprocess/mapped_region.hpp>
#include <boost/interprocess/managed_shared_memory.hpp>
#include <boost/interprocess/ipc/message_queue.hpp>
#include <boost/thread.hpp>
#include <ctime>

using namespace std;
using namespace boost::pthread;
using namespace boost::interprocess;
using namespace boost;

class KAThread
{
public:
	KAThread();
	virtual ~KAThread();
	bool threadFunction(message_queue*,KACommand*,char*[]);			//Execute command concurrently
	bool checkCWD(KACommand*);
	bool checkUID(KACommand*);
	static string getProcessPid(const char*);
	string createExecString(KACommand*);
	KAUserID& getUserID();
	KAResponsePack& getResponse();
	KALogger& getLogger();
	KAStreamReader& getErrorStream();
	KAStreamReader& getOutputStream();
	bool& getCWDERR();
	bool& getUIDERR();
	int& getEXITSTATUS();
	void setEXITSTATUS(int);
	void setCWDERR(bool);
	void setUIDERR(bool);
	bool& getACTFLAG();
	void setACTFLAG(bool);
	int& getResponsecount();
	void setResponsecount(int);
	int& getPpid();
	void setPpid(int);
	string& getoutBuff();
	string& geterrBuff();
	void setoutBuff(string);
	void seterrBuff(string);
	void setLogger(KALogger);
	int optionReadSend(message_queue*,KACommand*,int,int*);
	void checkAndWrite(message_queue*,KACommand*);
	void checkAndSend(message_queue*,KACommand*);
	void lastCheckAndSend(message_queue*,KACommand*);
	bool checkExecutionTimeout(unsigned int*,bool*,unsigned int*,unsigned int*);
	static string toString(int);
private:
	KAUserID uid;
	KALogger logger;
	KAResponsePack response;
	KAStreamReader errorStream;
	KAStreamReader outputStream;
	string argument,exec,sendout,environment;
	string outBuff, errBuff;	//general buffers for error and output
	pid_t pid;
	uid_t euid, ruid;
	int responsecount;
	int processpid;
	bool ACTFLAG;	    //flag for acrivity check for stderr and stdout
	bool CWDERR;	    //CWD error flag
	bool UIDERR;		//UID error flag
	int EXITSTATUS;    //Execution Error Detection Flag
};
#endif /* KATHREAD_H_ */
