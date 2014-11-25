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
/**
 *  @brief     SubutaiTimer.cpp
 *  @class     SubutaiTimer.cpp
 *  @details   SubutaiTimer Class is
 *  @author    Mikhail Savochkin
 *  @author    Ozlem Ceren Sahin
 *  @version   1.1.0
 *  @date      Oct 31, 2014
 */
#include "SubutaiTimer.h"

using namespace std;
/**
 *  \details   Default constructor of SubutaiEnvironment class.
 */
SubutaiTimer::SubutaiTimer(SubutaiLogger log, SubutaiEnvironment* env, SubutaiContainerManager* cont, SubutaiConnection* conn)
{
    start 			=  	boost::posix_time::second_clock::local_time();
    startQueue			=  	boost::posix_time::second_clock::local_time();
    exectimeout 		=  	30;
    queuetimeout 		=  	30;
    startsec  			=  	start.time_of_day().seconds();
    startsecQueue		=  	start.time_of_day().seconds();
    overflag 			=  	false;
    overflagQueue		=  	false;
    count 			=  	1;
    countQueue			=  	1;
    logMain		 	=	log;
    response 			= 	new SubutaiResponsePack();
    connection			= 	conn;
    environment 		=	env;
    containerManager 	        = 	cont;
}

/**
 *  \details   Default destructor of SubutaiEnvironment class.
 */
SubutaiTimer::~SubutaiTimer()
{
    // TODO Auto-generated destructor stub
}


/**
 *  \details   This method designed for Typically conversion from integer to string.
 */
bool SubutaiTimer::checkExecutionTimeout(unsigned int* startsec,bool* overflag,unsigned int* exectimeout,unsigned int* count)
{
    if (*exectimeout != 0)
    {
        boost::posix_time::ptime current = boost::posix_time::second_clock::local_time();
        unsigned int currentsec  =  current.time_of_day().seconds();

        if((currentsec > *startsec) && *overflag==false)
        {
            if(currentsec != 59)
            {
                *count = *count + (currentsec - *startsec);
                *startsec = currentsec;
            }
            else
            {
                *count = *count + (currentsec - *startsec);
                *overflag = true;
                *startsec = 1;
            }
        }
        if(currentsec == 59)
        {
            *overflag = true;
            *startsec = 1;
        }
        else
        {
            *overflag = false;
        }
        if(*count >= *exectimeout) //timeout
        {
            return true;	//timeout occured now
        }
        else
        {
            return false; //no timeout occured
        }
    }
    return false;	//no timeout occured
}


void SubutaiTimer::sendHeartBeat()
{
    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>", "Starting collecting of HEARTBEAT data"));
    response->clear();
    /*
     * Refresh new agent ip address set for each heartbeat message
     */
    environment->getAgentInterfaces();
    /*
     * Update each field of container nodes and set for each heartbeat message
     */
    containerManager->updateContainerLists();

    response->setInterfaces(environment->getAgentInterfaceValues());
    response->setHostname(environment->getAgentHostnameValue());
    response->setArch(environment->getAgentArch());
    response->setContainerSet(containerManager->getAllContainers());
    string resp = response->createHeartBeatMessage(environment->getAgentUuidValue(), environment->getAgentHostnameValue());
    connection->sendMessage(resp, "HEARTBEAT_TOPIC");

    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>", "HeartBeat:", resp));
}

bool SubutaiTimer::checkHeartBeatTimer(SubutaiCommand command)
{
    if (checkExecutionTimeout(&startsec, &overflag, &exectimeout, &count)) //checking Default Timeout
    {
        sendHeartBeat();
        start =         boost::posix_time::second_clock::local_time();	//Reset Default Timeout value
        startsec =      start.time_of_day().seconds();
        overflag =      false;
        exectimeout =   30;
        count =         1;

        return true;
    }
    return false;
}

bool SubutaiTimer::checkCommandQueueInfoTimer(SubutaiCommand command)
{
    if (checkExecutionTimeout(&startsecQueue,&overflagQueue,&queuetimeout,&countQueue))
    {   //checking IN_QUEUE Default Timeout
        //timeout occured!!
        response->clear();
        ifstream queueFile("/etc/subutai-agent/commandQueue.txt");
        string queueElement;
        if (queueFile.peek() != ifstream::traits_type::eof())
        {
            while(getline(queueFile, queueElement))
            {
                if (command.deserialize(queueElement))
                {
                    string resp = response->createInQueueMessage(environment->getAgentUuidValue(), command.getCommandId());
                    connection->sendMessage(resp);
                    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>", "IN_QUEUE Response:", resp));
                }
                else
                {
                    cout << "error!!" <<endl;
                    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>", "Fetched Element:",queueElement));
                }
            }
        }
        queueFile.close();
        startQueue =            boost::posix_time::second_clock::local_time();	//Reset Default Timeout values
        startsecQueue  =        startQueue.time_of_day().seconds();
        overflagQueue =         false;
        queuetimeout =          30;
        countQueue =            1;


        return true;
    }
    return false;
}
