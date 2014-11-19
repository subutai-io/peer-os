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
 *  @brief     SubutaiThread.h
 *  @class     SubutaiThread.h
 *  @details   SubutaiThread Class is designed to handle executions.
 *  		   Each Execution runs concurrently and does the given command job.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.1.0
 *  @date      Sep 13, 2014
 */
#ifndef SUBUTAITHREAD_H_
#define SUBUTAITHREAD_H_
#include <sys/wait.h>
#include <pthread.h>
#include <list>
#include <lxc/lxccontainer.h>
#include "SubutaiUserID.h"
#include "SubutaiCommand.h"
#include "SubutaiResponsePack.h"
#include "SubutaiStreamReader.h"
#include "SubutaiHelper.h"
#include "SubutaiLogger.h"
#include "SubutaiContainer.h"
#include <boost/interprocess/shared_memory_object.hpp>
#include <boost/interprocess/mapped_region.hpp>
#include <boost/interprocess/managed_shared_memory.hpp>
#include <boost/interprocess/ipc/message_queue.hpp>
#include <ctime>

using namespace std;
using namespace boost::interprocess;

class SubutaiThread
{
    public:
        SubutaiThread();
        virtual ~SubutaiThread();
        int threadFunction(message_queue*, SubutaiCommand*, char*[], SubutaiContainer* cont = NULL);
        bool checkCWD(SubutaiCommand*, SubutaiContainer* cont = NULL);
        bool checkUID(SubutaiCommand*, SubutaiContainer* cont = NULL);
        static string getProcessPid(const char*);
        string createExecString(SubutaiCommand*);
        bool ExecuteCommand(SubutaiCommand*, SubutaiContainer* cont);
        SubutaiUserID& getUserID();
        SubutaiResponsePack& getResponse();
        SubutaiLogger& getLogger();
        SubutaiStreamReader& getErrorStream();
        SubutaiStreamReader& getOutputStream();
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
        void setLogger(SubutaiLogger);
        int optionReadSend(message_queue*, SubutaiCommand*, int, int*);
        void checkAndWrite(message_queue*, SubutaiCommand*);
        void checkAndSend(message_queue*, SubutaiCommand*);
        void retrieveDaemonOutput(SubutaiCommand* command);
        void lastCheckAndSend(message_queue*, SubutaiCommand*);
        void captureOutputBuffer(message_queue*, SubutaiCommand*, bool output_buffer, bool error_buffer);
        bool checkExecutionTimeout(unsigned int*, bool*, unsigned int*, unsigned int*);
    private:
        SubutaiUserID           uid;
        SubutaiLogger           logger;
        SubutaiResponsePack     response;
        SubutaiStreamReader     errorStream;
        SubutaiStreamReader     outputStream;
        SubutaiHelper 			helper;
        SubutaiContainer*       _container;          // container attached to this thread
        string                  argument, exec, sendout, environment;
        string                  outBuff, errBuff;   //general buffers for error and output
        pid_t                   pid;
        uid_t                   euid, ruid;
        int                     responsecount;
        int                     processpid;
        bool                    ACTFLAG;	        //flag for acrivity check for stderr and stdout
        bool                    CWDERR;             //CWD error flag
        bool                    UIDERR;		        //UID error flag
        int                     EXITSTATUS;         //Execution Error Detection Flag
        bool                    _isContainer;       // Command need to be executed on container
};
#endif /* SUBUTAITHREAD_H_ */
