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
 *    @copyright 2014 Safehaus.org
 */
#include "SubutaiResponsePack.h"

/**
 *  \details   Default constructor of the SubutaiResponsePack class.
 */
SubutaiResponsePack::SubutaiResponsePack()
{
}

/**
 *  \details   Default destructor of the SubutaiResponsePack class.
 */
SubutaiResponsePack::~SubutaiResponsePack()
{
	// TODO Auto-generated destructor stub
}

/**
 *  \details   This method creates default chunk message.
 */
string SubutaiResponsePack::createResponseMessage(string uuid,int pid,int requestSeqNum,int responseSeqNum,
		string error,string output,string source,string taskuuid)
{
	clear();
	this->setType("EXECUTE_RESPONSE");			//creating Response chunk message
	this->setSource(source);
	this->setTaskUuid(taskuuid);
	this->setUuid(uuid);
	this->setRequestSequenceNumber(requestSeqNum);
	this->setResponseSequenceNumber(responseSeqNum);
	this->setStandardOutput(output);
	this->setStandardError(error);
	this->setPid(pid);
	this->serialize(sendout);
	return sendout;
}

/**
 *  \details   This method creates Exit done message.
 */
string SubutaiResponsePack::createExitMessage(string uuid,int pid,int requestSeqNum,int responseSeqNum,
		string source, string taskuuid,int exitcode)	//Creating Exit message
{
	clear();
	this->setType("EXECUTE_RESPONSE_DONE");
	this->setSource(source);
	this->setTaskUuid(taskuuid);
	this->setUuid(uuid);
	this->setPid(pid);
	this->setRequestSequenceNumber(requestSeqNum);
	this->setResponseSequenceNumber(responseSeqNum);
	this->setExitCode(exitcode);
	this->serializeDone(sendout);
	return sendout;
}

/**
 *  \details   This method creates Registration message.
 */
string SubutaiResponsePack::createRegistrationMessage(string uuid, string macaddress, string hostname, string parenthostname
		,string environmentID ,vector<string> ips)
{
	this->setType("REGISTRATION_REQUEST");
	this->setIps(ips);
	this->setMacAddress(macaddress);
	this->setHostname(hostname);
	this->setParentHostname(parenthostname);
	this->setUuid(uuid);
	this->setEnvironmentId(environmentID);
	this->serialize(sendout);
	return sendout;
}

/**
 *  \details   This method creates IN_QUEUE Message
 */
string SubutaiResponsePack::createInQueueMessage(string uuid,string taskuuid)	//Creating IN_QUEUE Message
{
	clear();
	this->setType("IN_QUEUE");
	this->setTaskUuid(taskuuid);
	this->setUuid(uuid);
	this->serialize(sendout);
	return sendout;
}

/**
 *  \details   This method creates HeartBeat message.
 *  {
   "response":

    {

        "type":"HEARTBEAT_RESPONSE",

        "id":"56b0ac88-5140-4a32-8691-916d75d62f1c",

        "hostname":"resource_host",

        "ips":["10.10.10.13","172.16.11.89","127.0.0.1"],

        "macAddress":"08:00:27:f2:9b:aa",

        "containers":[

            {
                "hostname" : "container1",

                "id":"56b0ac88-5140-4a32-8691-916d75d62f1c",

                "ips":["10.10.10.12","127.0.0.1"],

                "status":"RUNNING"

            },

            {
                "hostname" : "container1",

                "id":"56b0ac88-5140-4a32-8691-916d75d62f1c",

                "ips":["10.10.10.14","127.0.0.1"],

                "status":"FROZEN"

            }
        ]
    }

}
 */
string SubutaiResponsePack::createHeartBeatMessage(string uuid,	string hostname,string macaddress)	//Creating HeartBeat Message
{
	clear();
	this->setType("HEARTBEAT");
	this->setUuid(uuid);
	this->setHostname(hostname);
	this->setMacAddress(macaddress);
	this->serialize(sendout);

	cout << "Message is : " << sendout << endl;
	return sendout;
}

/**
 *  \details   This method creates  SuccessTermination message.
 *          "type":"TERMINATE_RESPONSE",

        "id":"56b0ac88-5140-4a32-8691-916d75d62f1c"

        "commandId":"c6cd5988-ceac-11e3-82b2-ebd389e743a3",

        "pid":1234,

        "exitCode" : 0
 */
string SubutaiResponsePack::createTerminateMessage(string uuid,int requestSeqNum,string taskuuid, int pid)	//Creating Terminate Message
{
	clear();
	this->setType("TERMINATE_RESPONSE");
	this->setExitCode(0);
	this->setUuid(uuid);
	this->setRequestSequenceNumber(requestSeqNum);
	this->setResponseSequenceNumber(1);
	this->setTaskUuid(taskuuid);
	this->setPid(pid);
	this->serialize(sendout);
	return sendout;
}

/**
 *  \details   This method creates Fail Termination message.
 *
    									"type":"TERMINATE_RESPONSE",

										"id":"56b0ac88-5140-4a32-8691-916d75d62f1c"

										"commandId":"c6cd5988-ceac-11e3-82b2-ebd389e743a3",

										"pid":1234,

										"stdErr":"bash: line 0: kill: (1234) - No such process\n",

										"exitCode" : 1

 */
string SubutaiResponsePack::createFailTerminateMessage(string uuid,int requestSeqNum,string taskuuid, int pid,const string& stderr)
{
	clear();
	this->setType("TERMINATE_RESPONSE");
	this->setExitCode(1);
	this->setUuid(uuid);
	this->setRequestSequenceNumber(requestSeqNum);
	this->setResponseSequenceNumber(1);
	this->setTaskUuid(taskuuid);
	this->setPid(pid);
	this->setStandardError(stderr);
	this->serialize(sendout);
	return sendout;
}

/**
 *  \details   This method creates Timeout message.
 */
string SubutaiResponsePack::createTimeoutMessage(string uuid,int pid,int requestSeqNum,int responseSeqNum,
		string stdOut,string stdErr,string source,string taskuuid)	//Creating Timeout Message
{
	clear();
	this->setType("EXECUTE_TIMEOUT");
	this->setSource(source);
	this->setTaskUuid(taskuuid);
	this->setPid(pid);
	this->setUuid(uuid);
	this->setRequestSequenceNumber(requestSeqNum);
	this->setResponseSequenceNumber(responseSeqNum);
	this->setStandardOutput(stdOut);
	this->setStandardError(stdErr);
	this->serialize(sendout);
	return sendout;
}

/**
 *  \details   This method creates Inotify response message.
 */
string SubutaiResponsePack::createInotifyMessage(string uuid ,string configPoint,string dateTime,string changeType)
{
	clear();
	this->setType("INOTIFY_ACTION_RESPONSE");
	this->setUuid(uuid);
	this->setconfigPoint(configPoint);
	this->setDateTime(dateTime);
	this->setChangeType(changeType);
	this->getConfPoints().clear();
	this->serialize(sendout);
	return sendout;
}

/**
 *  \details   This method creates Inotify showing all watcher message.
 */
string SubutaiResponsePack::createInotifyShowMessage(string uuid ,vector<string>  configPoint)
{
	clear();
	this->setType("INOTIFY_LIST_RESPONSE");
	this->setUuid(uuid);
	this->setConfPoints(configPoint);
	this->serialize(sendout);
	return sendout;
}
