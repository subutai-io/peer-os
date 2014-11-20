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
 *  @brief     SubutaiAgent.cpp
 *  @class     SubutaiAgent.cpp
 *  @details   This is SubutaiAgent Software main process.
 *  		   It's main responsibility is that send and receive messages from MQTT broker.
 *  		   It also creates a new process using KAThread Class when the new Execute Request comes.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.1.0
 *  @date      Sep 13, 2014
 */
/** \mainpage  Welcome to Project Subutai Agent
 *	\section   Subutai Agent
 * 			   The Subutai Agent is a simple daemon designed to connect securely to an MQTT server to
 * 			   reliably receive and send messages on queues and topics.
 * 	 	 	   It's purpose is to perform a very simple reduced set of instructions to
 * 	 	 	   manage any system administration task.
 * 	 	 	   The agent may run on physical servers, virtual machines or inside Linux Containers.
 */

#include "SubutaiCommand.h"
#include "SubutaiResponse.h"
#include "SubutaiUserID.h"
#include "SubutaiResponsePack.h"
#include "SubutaiThread.h"
#include "SubutaiConnection.h"
#include "SubutaiLogger.h"
#include "SubutaiWatch.h"
#include "SubutaiEnvironment.h"
#include "SubutaiContainerManager.h"
#include "SubutaiTimer.h"
#include <stdio.h>
#include <pthread.h>
#include <iostream>
#include <string.h>
#include <unistd.h>

using namespace std;

/**
 *  \details   threadSend function sends string messages in the Shared Memory buffer to MQTT Broker.
 *  	       This is a thread with working concurrently with main thread.
 *  	       It is main purpose that checking the Shared Memory Buffer in Blocking mode and sending them to Broker
 */
void threadSend(message_queue *mq,SubutaiConnection *connection,SubutaiLogger* logMain)
{

    try
    {
        string str;
        unsigned int priority;
        size_t recvd_size;
        while(true)
        {
            str.resize(2500);
            mq->receive(&str[0], str.size(), recvd_size, priority);
            logMain->writeLog(7, logMain->setLogData("<SubutaiAgent>::<threadsend>",
                        "New message comes to messagequeue to be sent:", str));
            connection->sendMessage(str.c_str());
            str.clear();
        }
        message_queue::remove("message_queue");
    }
    catch(interprocess_exception &ex)
    {
        message_queue::remove("message_queue");
        std::cout << ex.what() << std::endl;
        logMain->writeLog(3, logMain->setLogData("<SubutaiAgent>::<threadsend>", "New exception Handled:", ex.what()));
    }
}


/**
 *  \details   This function is the main thread of SubutaiAgent.
 *  		   It sends and receives messages from MQTT broker.
 *  		   It is also responsible from creation new process.
 */
int main(int argc,char *argv[],char *envp[])
{
	string serverAddress        = "RESPONSE_TOPIC";              // Default RESPONSE TOPIC
    string broadcastAddress     = "BROADCAST_TOPIC";	        // Default BROADCAST TOPIC
    string clientAddress;
    SubutaiHelper helper;
    SubutaiThread thread;
    SubutaiLogger logMain;
    SubutaiCommand command;
    SubutaiResponsePack response;
    SubutaiEnvironment environment(&logMain);
    string input = "";
    string sendout;

    if (!thread.getUserID().checkRootUser()) {
        //user is not root SubutaiAgent Will be closed
        cout << "Main Process User is not root.. Subutai Agent is going to be closed.." << endl;
        close(STDIN_FILENO);
        close(STDOUT_FILENO);
        close(STDERR_FILENO);
        return 100;
    }

    if (!logMain.openLogFileWithName("subutai-agent.log")) {
        cout << "/var/log/subutai-agent/ folder does not exist.. Subutai Agent is going to be closed.."<<endl;
        FILE* dumplog = fopen("/etc/subutai-agent_dump.log","a+");
        string log = "<DEBUG> /var/log/subutai-agent/ folder does not exist.. "
            "Subutai Agent is going to be closed.. \n";
        fputs(log.c_str(), dumplog);
        fflush(dumplog);
        close(STDIN_FILENO);
        close(STDOUT_FILENO);
        close(STDERR_FILENO);
        return 200;
    }

    bool hasLxc = true;
    if (system("which lxc-ls") != 0) {
        hasLxc = false;
    }

    logMain.setLogLevel(7);
    logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>", "Subutai Agent is starting.."));
    logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>", "Agent.xml is reading.."));

    /*
     * Getting Environment Settings..
     */

    environment.getAgentSettings();
    environment.getAgentUuid();
    environment.getAgentInterfaces();
    environment.getAgentHostname();
    environment.getAgentEnvironmentId();
    clientAddress = environment.getAgentUuidValue();

    /*
     * Starting Container Manager
     */
    SubutaiContainerManager cman("/var/lib/lxc", &logMain);

    /*
     * Opening MQTT Connection
     */
    class SubutaiConnection *connection;
    int rc;
    mosqpp::lib_init();
    connection = new SubutaiConnection(
            clientAddress.c_str(),
            clientAddress.c_str(),
            serverAddress.c_str(),
            broadcastAddress.c_str(),
            environment.getAgentConnectionUrlValue().c_str(),
            atoi(environment.getAgentConnectionPortValue().c_str()));

    logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>", "Trying to open Connection with MQTT Broker: ",
                environment.getAgentConnectionUrlValue(), " Port: ", environment.getAgentConnectionPortValue()));

    int reconnectDelay = atoi(environment.getAgentConnectionOptionsValue().c_str()) - 4;

    if (reconnectDelay <= 0) {
        logMain.writeLog(3, logMain.setLogData("<SubutaiAgent>", "Reconnect Delay cannot be under 0",
                    " Using default timeout(10).", helper.toString(reconnectDelay)));
        reconnectDelay = 10;
    }

    while (true) {
        if (!connection->openSession()) {
            sleep(reconnectDelay);
            logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>", "Trying connect to MQTT Broker:", 
                        environment.getAgentConnectionUrlValue()));
            if (connection->reConnect()) {
                break;
            }
        } else {
            break;
        }
    }

    logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>", "Connection Successfully opened with MQTT Broker: ",
                environment.getAgentConnectionUrlValue()));
    logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>", "Registration Message is sending to MQTT Broker"));

    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Shared Memory MessageQueue is initializing.."));
    message_queue messageQueue
        (open_or_create,        //only create
         "message_queue",       //name
         100,                   //max message number
         2500                   //max message size
        );

    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>", "Sending Thread is starting.."));
    boost::thread thread1(threadSend, &messageQueue, connection, &logMain);
    /* Change the file mode mask */
    umask(0);

    list<int> pidList;
    int ncores =                                -1;
    ncores =                                    sysconf(_SC_NPROCESSORS_CONF);
    int currentProcess =                        0;
    string str, str2; 
    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>", "Number of cpu core:", helper.toString(ncores)));
    logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>", "Client Address:", clientAddress));

    /*
     * initializing inotify handler
     */
    SubutaiWatch Watcher(connection, &response, &logMain);
    Watcher.initialize(20000);
    logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>", "Watcher is initializing.."));

    /*
     * initializing timer for periodical operations
     */
    SubutaiTimer timer(logMain, &environment, &cman, connection);
    logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>", "Timer is initializing.."));

    /*
     * Send initial heartbeat for registration of resource host and container nodes attached to this host.
     */
    timer.sendHeartBeat();
    logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>", "Sending first heartbeat.."));
    //cman.getRunningContainers()[0].tryLongCommand();
    while(true)
    {
        try
        {
            /*
             * In 30 second periods send heartbeat and in_queue responses.
             */
            timer.checkHeartBeatTimer(command);
            timer.checkCommandQueueInfoTimer(command);
            command.clear();
            for (list<int>::iterator iter = pidList.begin(); iter != pidList.end(); iter++) {
                if (pidList.begin() != pidList.end()) {
                    int status;
                    pid_t result = waitpid(*iter, &status, WNOHANG);
                    if (result != 0) {
                        iter = pidList.erase(iter);
                        currentProcess--;
                        string resp = response.createInQueueMessage(environment.getAgentUuidValue(), command.getCommandId());
                    }
                }
            }

            Watcher.checkNotification(&cman); //checking the watch event status

            rc = connection->loop(1); // checking the status of the new message
            if (rc) {
                logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","RC:",helper.toString(rc)));
                connection->reconnect();
            }
            if (connection->checkMessageStatus()) { //checking new message arrived ? 
                logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","total running process:",helper.toString(currentProcess)));
                connection->resetMessageStatus(); //reseting message status
                input = connection->getMessage(); //fetching message..
                logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Fetched Message:",input));

                if (command.deserialize(input))	{  //deserialize the message
                    logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>","New Message is received"));
                    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","New Message:", input));
                    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Request type:", command.getType()));
                    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Request ID:", command.getUuid()));
                    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Request CommandID:", command.getCommandId()));
                    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Request RequestSequenceNumber:",
                                helper.toString(command.getRequestSequenceNumber())));
                    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Request workingDirectory:", command.getWorkingDirectory()));
                    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Request StdOut:", command.getStandardOutput()));
                    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Request StdErr:", command.getStandardError()));
                    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Request Command:", command.getCommand()));
                    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Request runAs:", command.getRunAs()));
                    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Request timeout:", helper.toString(command.getTimeout())));

                    // Check if this uuid belongs the resource host or one of child containers
                    bool isLocal = true;
                    SubutaiContainer* target_container;
                    if (hasLxc) {
                        target_container = cman.findContainerById(command.getUuid());
                    }

                    if (hasLxc && target_container) {
                        logMain.writeLog(3, logMain.setLogData("<SubutaiAgent>", "Container received a command to execute"));
                        isLocal = false;
                    } else {
                        logMain.writeLog(3, logMain.setLogData("<SubutaiAgent>", "Resource host received command to execute"));
                    }

                    if (command.getWatchArguments().size() != 0 ) {
                        for (unsigned int i = 0; i < command.getWatchArguments().size(); i++) {
                            logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>", "Request WatchArgs:", command.getWatchArguments()[i]));
                        }
                    }

                    if (command.getType() == "EXECUTE_REQUEST")	//execution request will be executed in other process.
                    {
                        logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Received Message to internal currentProcess!"));
                        fstream file;	//opening commandQueue.txt
                        file.open("/etc/subutai-agent/commandQueue.txt",fstream::in | fstream::out | fstream::app);
                        file << input;
                        file.close();
                        commandManager->addCommand(&command);
                    } else if (command.getType()=="PS_REQUEST") {
                        logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","PS execution operation is starting.."));
                        SubutaiThread* subprocess = new SubutaiThread;
                        subprocess->getLogger().setLogLevel(logMain.getLogLevel());
                        if (!target_container) {
                            command.setCommand("for i in `ps aux | grep '[s]h -c' | awk -F \" \" '{print $2}'`; do ps aux | grep `pgrep -P $i` | sed '/grep/d' ; done 2> /dev/null");
                            command.setWorkingDirectory("/");
                        } else {
                            string cmdline = "for i in `ps aux | grep '[s]h -c' | awk -F \" \" '{print $2}'`; do ps aux | grep `pgrep -P $i` | sed '/grep/d' ; done 2> /dev/null";
                            target_container->PutToFile("/opt/psrun", cmdline);
                            vector<string> chmod;
                            chmod.push_back("+x");
                            chmod.push_back("/opt/psrun");
                            target_container->RunProgram("chmod", chmod);
                            command.setCommand("/opt/psrun");
                        }
                        subprocess->threadFunction(&messageQueue,&command,argv);

                        delete subprocess;
                    }
                    else if (command.getType() == "TERMINATE_REQUEST")
                    {
                        int retstatus; string resp;
                        logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Termination request ID:",helper.toString(command.getPid())));

                        if (command.getPid() > 0)
                        {
                            if( isLocal)
                            {
                                logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Killing given PID on resource host: " + command.getPid()));
                                retstatus = kill(command.getPid(),SIGKILL);
                                resp = response.createTerminateMessage(environment.getAgentUuidValue(),
                                        command.getCommandId(), command.getPid(), retstatus);
                            }
                            else
                            {
                                logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Killing given PID on container node: " + command.getPid()));

                                command.setCommand("/bin/kill -9 " + helper.toString(command.getPid()) );
                                ExecutionResult execResult = target_container->RunCommand(&command);
                                retstatus  = execResult.exit_code;

                                resp = response.createTerminateMessage(target_container->getContainerIdValue(),
                                        command.getCommandId(), command.getPid(), retstatus);

                            }

                            connection->sendMessage(resp);
                            logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Terminate response: " + resp));

                        }
                        else
                        {
                            logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>", "Irrelevant Terminate Request"));
                        }
                    }
                    else if (command.getType()=="SET_INOTIFY_REQUEST")
                    {
                        string preArg = "";
                        if(target_container) preArg = "/var/lib/lxc/" + target_container->getContainerHostnameValue() + "/rootfs";

                        logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>","executing SET_INOTIFY_REQUEST.."));
                        for (unsigned int i=0; i<command.getWatchArguments().size();i++) {
                            Watcher.addWatcher(preArg + command.getWatchArguments()[i]);
                            logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>","adding Watcher: ",
                                        preArg + command.getWatchArguments()[i]));
                        }
                        Watcher.stats();
                        sendout = response.setInotifyResponse(environment.getAgentUuidValue(),command.getCommandId());
                        logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Set_Inotify response: " + sendout));
                        connection->sendMessage(sendout, "RESPONSE_TOPIC");
                        Watcher.stats();
                    }
                    else if (command.getType() == "UNSET_INOTIFY_REQUEST")
                    {
                        string preArg = "";
                        if(target_container) preArg = "/var/lib/lxc/" + target_container->getContainerHostnameValue() + "/rootfs";

                        logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>", "executing INOTIFY_CANCEL_REQUEST.."));
                        for (unsigned int i = 0; i < command.getWatchArguments().size(); i++) {
                            Watcher.eraseWatcher(preArg + command.getWatchArguments()[i]);
                            logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>", "Erasing Watcher: ",
                                        preArg + command.getWatchArguments()[i]));
                        }
                        Watcher.stats();
                        sendout = response.unsetInotifyResponse(environment.getAgentUuidValue(),command.getCommandId());
                        logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Unset_Inotify response: " + sendout));
                        connection->sendMessage(sendout, "RESPONSE_TOPIC");
                        Watcher.stats();
                    }
                    else if (command.getType() == "LIST_INOTIFY_REQUEST") {
                        logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>", "executing INOTIFY_SHOW_REQUEST.."));
                        Watcher.stats();
                        sendout = response.createInotifyShowMessage(environment.getAgentUuidValue(), command.getCommandId(), response.getConfPoints());
                        connection->sendMessage(sendout, "RESPONSE_TOPIC");
                        Watcher.stats();
                        logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>", "Sending Inotify Show Message: ", sendout));
                    }
                }
                else {
                    logMain.writeLog(3, logMain.setLogData("<SubutaiAgent>","Failed to parsing JSON String: ", input));
                    if (input.size() >= 10000) {
                        connection->sendMessage(response.createResponseMessage(environment.getAgentUuidValue(), 9999999,
                                    command.getRequestSequenceNumber(), 1,
                                    "Request Size is greater than Maximum Size", "", command.getCommandId()));
                        connection->sendMessage(response.createExitMessage(environment.getAgentUuidValue(), 9999999,
                                    command.getRequestSequenceNumber(), 2,
                                    command.getCommandId(), 1));
                    } else {
                        connection->sendMessage(response.createResponseMessage(environment.getAgentUuidValue(),9999999,
                                    command.getRequestSequenceNumber(),1,
                                    "Request is not a valid JSON string","",command.getCommandId()));
                        connection->sendMessage(response.createExitMessage(environment.getAgentUuidValue(),9999999,
                                    command.getRequestSequenceNumber(),2,
                                    command.getCommandId(),1));
                    }
                }
            } else {
                if (currentProcess < ncores) {
                    ifstream file2("/etc/subutai-agent/commandQueue.txt");
                    if (file2.peek() != ifstream::traits_type::eof()) {
                        ofstream file3("/etc/subutai-agent/commandQueue2.txt");
                        input = "";
                        getline(file2, str2);
                        input = str2;
                        while(getline(file2, str2)) {
                            file3 << str2 << endl;
                        }
                        file3.close();
                        rename("/etc/subutai-agent/commandQueue2.txt", "/etc/subutai-agent/commandQueue.txt");
                        logMain.writeLog(6, logMain.setLogData("<SubutaiAgent>","Message Fetched from internal queue!"));
                        if (input != "\n" && command.deserialize(input)) {
                            logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Execute operation is starting.."));
                            SubutaiThread* subprocess = new SubutaiThread;
                            subprocess->getLogger().setLogLevel(logMain.getLogLevel());
                            //command.setUuid(environment.getAgentUuidValue()); /*command uuid should be set to agents uuid */
                            SubutaiContainer* target_container = cman.findContainerById(command.getUuid());
                            pidList.push_back(subprocess->threadFunction(&messageQueue, &command, argv, target_container));
                            currentProcess++;
                            delete subprocess;
                        } else {
                            cout << "error!" << endl;
                        }
                    }
                }
            }
        } catch(const std::exception& error) {
            logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>","Exception is raised: ", error.what()));
            cout << error.what() << endl;
        }
    }
    close(STDIN_FILENO);
    close(STDOUT_FILENO);
    close(STDERR_FILENO);
    logMain.writeLog(7, logMain.setLogData("<SubutaiAgent>", "Subutai Agent is closing Successfully.."));
    logMain.closeLogFile();
    kill(getpid(), SIGKILL);
    mosqpp::lib_cleanup();
    return 0;
}
