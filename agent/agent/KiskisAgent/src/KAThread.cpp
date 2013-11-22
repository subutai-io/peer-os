#include "KAThread.h"
/**
 *  \details   Default constructor of the KAThread class.
 */
KAThread::KAThread()
{
	// TODO Auto-generated constructor stub
}
/**
 *  \details   Default destructor of the KAThread class.
 */
KAThread::~KAThread()
{
	// TODO Auto-generated destructor stub
}
/**
 *  \details   This method checks CurrentWorking Directory in the command
 *  		   If given CWD does not exist on system, it returns false otherwise it returns true.
 */
bool KAThread::checkCWD(KACommand *command)
{
	if ((chdir(command->getWorkingDirectory().c_str())) < 0)
	{		//changing working directory first
		logger.writeLog(3,logger.setLogData("<KAThread::threadFunction> " " Changing working Directory failed..","pid",toString(getpid()),"CWD",command->getWorkingDirectory()));
		return false;
	}
	else
		return true;
}
/**
 *  \details   This method checks and add user Directory in the command
 *  		   If given CWD does not exist on system, it returns false otherwise it returns true.
 */
bool KAThread::checkUID(KACommand *command)
{
	if(uid.getIDs(ruid,euid,command->getRunAs()))
	{	//checking user id is on system ?
		logger.writeLog(4,logger.setLogData("<KAThread::threadFunction> " "User id successfully found on system..","pid",toString(getpid()),"RunAs",command->getRunAs()));
		uid.doSetuid(this->euid);
		return true;
	}
	else
	{
		logger.writeLog(3,logger.setLogData("<KAThread::threadFunction> " "User id could not found on system..","pid",toString(getpid()),"RunAs",command->getRunAs()));
		logger.writeLog(3,logger.setLogData("<KAThread::threadFunction> " "Thread will be closed..","pid",toString(getpid()),"RunAs",command->getRunAs()));
		uid.undoSetuid(ruid);
		return false;
	}
}
/**
 *  \details   This method creates execution string.
 *  		   It combines (environment parameters set if it is exist) && program + arguments.
 *  		   It returns this combined string to be execution.
 */
string KAThread::createExecString(KACommand *command)
{
	string arg,env;
	exec.clear();
	logger.writeLog(6,logger.setLogData("<KAThread::createExecString>""Method starts...","pid",toString(getpid())));
	for(unsigned int i=0;i<command->getArguments().size();i++)	//getting arguments for creating Execution string
	{
		arg = command->getArguments()[i];
		argument = argument + arg + " ";
		logger.writeLog(7,logger.setLogData("<KAThread::createExecString>","pid",toString(getpid()),"Argument",arg.c_str()));
	}
	for (std::list<pair<string,string> >::iterator it = command->getEnvironment().begin(); it != command->getEnvironment().end(); it++ )
	{
		arg = it->first.c_str();
		env = it->second.c_str();
		logger.writeLog(7,logger.setLogData("<KAThread::createExecString> Environment Parameters","Parameter",arg,"=",env));
		environment = environment + " export "+ arg+"="+env+" && ";
	}
	if(environment.empty())
	{
		exec = command->getProgram() + " " + argument ;		//arguments added execution string
		logger.writeLog(7,logger.setLogData("<KAThread::createExecString> " "Execution command has been created","Command:",exec));
	}
	else
	{
		exec = environment + command->getProgram() + " " + argument ;
		logger.writeLog(7,logger.setLogData("<KAThread::createExecString> " "Execution command has been created","Command:",exec));
	}
	logger.writeLog(6,logger.setLogData("<KAThread::createExecString>""Method finished....","pid",toString(getpid())));
	return exec;
}
/**
 *  \details   This method check lastly buffer results and sends the buffers to the ActiveMQ broker.
 *  		   This method is only called when the timeout occured or process is done.
 */
void KAThread::lastCheckAndSend(message_queue *messageQueue,KACommand* command,string* outBuff,
		string* errBuff,numbers* block,KALogger* logger)
{
	logger->writeLog(6,logger->setLogData("<KAThread::lastCheckAndSend> " "The method starts..."));
	logger->writeLog(7,logger->setLogData("<KAThread::lastCheckAndSend> " "Execution command has been created","outbuff:",*outBuff));
	logger->writeLog(7,logger->setLogData("<KAThread::lastCheckAndSend> " "Execution command has been created","errbuff:",*errBuff));
	unsigned int outBuffsize = outBuff->size();					//real output buffer size
	unsigned int errBuffsize = errBuff->size();					//real error buffer size
	KAResponsePack response;
	if(outBuffsize !=0 || errBuffsize!=0)
	{
		if(outBuffsize !=0 && errBuffsize!=0)
		{
			if(command->getStandardOutput()!="CAPTURE" && command->getStandardError()!="CAPTURE")
			{
				/*
				 * send main buffers without blocking output and error
				 */
				string message = response.createResponseMessage(command->getUuid(),*block->processpid,
						command->getRequestSequenceNumber(),*block->responsecount,*errBuff,*outBuff,command->getSource(),command->getTaskUuid());
				logger->writeLog(7,logger->setLogData("<KAThread::lastCheckAndSend1> " "Message was created for send to the shared memory","Message:",message));
				while(!messageQueue->try_send(message.data(), message.size(), 0));
				*block->responsecount=*block->responsecount+1;
				logger->writeLog(7,logger->setLogData("<KAThread::lastCheckAndSend1> " "Message was created and sent to the shared memory"));
				outBuff->clear();
				errBuff->clear();
			}
			else if(command->getStandardOutput()!="CAPTURE")
			{
				/*
				 * send main buffers with block error buff
				 */
				errBuff->clear();
				string message = response.createResponseMessage(command->getUuid(),*block->processpid,
						command->getRequestSequenceNumber(),*block->responsecount,*errBuff,*outBuff,command->getSource(),command->getTaskUuid());
				logger->writeLog(7,logger->setLogData("<KAThread::lastCheckAndSend2> " "Message was created for send to the shared memory","Message:",message));
				while(!messageQueue->try_send(message.data(), message.size(), 0));
				*block->responsecount=*block->responsecount+1;
				logger->writeLog(6,logger->setLogData("<KAThread::lastCheckAndSend2> " "Message was created and sent to the shared memory"));
				outBuff->clear();
			}
			else if(command->getStandardError()!="CAPTURE")
			{
				outBuff->clear();
				/*
				 * send main buffers with blocking error buff
				 */
				string message = response.createResponseMessage(command->getUuid(),*block->processpid,
						command->getRequestSequenceNumber(),*block->responsecount,*errBuff,*outBuff,command->getSource(),command->getTaskUuid());
				logger->writeLog(7,logger->setLogData("<KAThread::lastCheckAndSend3> " "Message was created for send to the shared memory","Message:",message));
				while(!messageQueue->try_send(message.data(), message.size(), 0));
				*block->responsecount=*block->responsecount+1;
				logger->writeLog(6,logger->setLogData("<KAThread::lastCheckAndSend3> " "Message was created and sent to the shared memory"));
				errBuff->clear();
			}
			else
			{
				outBuff->clear();
				errBuff->clear();
			}
		}
		else if(outBuffsize !=0)
		{
			if(command->getStandardOutput()!="CAPTURE")
			{
				/*
				 * send main buffers without block output. (errbuff size is zero)
				 */
				errBuff->clear();
				string message = response.createResponseMessage(command->getUuid(),*block->processpid,
						command->getRequestSequenceNumber(),*block->responsecount,*errBuff,*outBuff,command->getSource(),command->getTaskUuid());
				logger->writeLog(7,logger->setLogData("<KAThread::lastCheckAndSend4> " "Message was created for send to the shared memory","Message:",message));
				while(!messageQueue->try_send(message.data(), message.size(), 0));
				*block->responsecount=*block->responsecount+1;
				logger->writeLog(6,logger->setLogData("<KAThread::lastCheckAndSend4> " "Message was created and sent to the shared memory"));
				outBuff->clear();
			}
			else
			{
				outBuff->clear();
				errBuff->clear();
			}
		}
		else if(errBuffsize !=0)
		{
			if(command->getStandardError()!="CAPTURE")
			{
				/*
				 * send main buffers without block output. (errbuff size is zero)
				 */
				outBuff->clear();
				string message = response.createResponseMessage(command->getUuid(),*block->processpid,
						command->getRequestSequenceNumber(),*block->responsecount,*errBuff,*outBuff,command->getSource(),command->getTaskUuid());
				logger->writeLog(7,logger->setLogData("<KAThread::lastCheckAndSend5> " "Message was created for send to the shared memory","Message:",message));
				while(!messageQueue->try_send(message.data(), message.size(), 0));
				*block->responsecount=*block->responsecount+1;
				logger->writeLog(6,logger->setLogData("<KAThread::lastCheckAndSend5> " "Message was created and sent to the shared memory"));
				errBuff->clear();
			}
			else
			{
				outBuff->clear();
				errBuff->clear();
			}
		}
	}
}
/**
 *  \details   This method check buffer results and sends the buffers to the ActiveMQ broker.
 *  		   This method calls when any buffer result overflow 1000 bytes.
 */
void KAThread::checkAndSend(message_queue* messageQueue,KAStreamReader* Stream,string* outBuff,string* errBuff,
		KACommand* command,numbers* block,KALogger* logger)
{
	logger->writeLog(6,logger->setLogData("<KAThread::checkAndSend> Method starts..."));
	logger->writeLog(7,logger->setLogData("<KAThread::checkAndSend> ","outbuff:",*outBuff));
	KAResponsePack response;
	if(Stream->getIdentity()=="output")
	{
		if( Stream->getMode()=="RETURN" || Stream->getMode()=="CAPTURE_AND_RETURN" )	//send to ActiveMQ
		{
			if(command->getStandardError() == "CAPTURE")
			{
				/*
				 * send main buffers with blocking error
				 */
				errBuff->clear();
				string message = response.createResponseMessage(command->getUuid(),*block->processpid,
						command->getRequestSequenceNumber(),*block->responsecount,*errBuff,*outBuff,command->getSource(),command->getTaskUuid());
				logger->writeLog(7,logger->setLogData("<KAThread::checkAndSend1> " "Message was created for sending to the shared memory","Message:",message));
				while(!messageQueue->try_send(message.data(), message.size(), 0));
				*block->responsecount=*block->responsecount+1;
				logger->writeLog(6,logger->setLogData("<KAThread::checkAndSend1> " "Message was created and sent to the shared memory"));
			}
			else	//stderr is not in capture mode so it will not be blocked
			{
				/*
				 * send main buffers without block error
				 */
				string message = response.createResponseMessage(command->getUuid(),*block->processpid,
						command->getRequestSequenceNumber(),*block->responsecount,*errBuff,*outBuff,command->getSource(),command->getTaskUuid());
				logger->writeLog(7,logger->setLogData("<KAThread::checkAndSend2> " "Message was created for sending to the shared memory","Message:",message));
				while(!messageQueue->try_send(message.data(), message.size(), 0));
				*block->responsecount=*block->responsecount+1;
				logger->writeLog(6,logger->setLogData("<KAThread::checkAndSend2> " "Message was created and sent to the shared memory"));
			}
		}
	}
	else if(Stream->getIdentity()=="error")
	{
		if( Stream->getMode()=="RETURN" || Stream->getMode()=="CAPTURE_AND_RETURN" )	//send to ActiveMQ
		{
			if(command->getStandardOutput() == "CAPTURE")
			{
				/*
				 * send main buffers with blocking output
				 */
				outBuff->clear();

				string message = response.createResponseMessage(command->getUuid(),*block->processpid,
						command->getRequestSequenceNumber(),*block->responsecount,*errBuff,*outBuff,command->getSource(),command->getTaskUuid());
				logger->writeLog(7,logger->setLogData("<KAThread::checkAndSend3> " "Message was created for sending to the shared memory","Message:",message));
				while(!messageQueue->try_send(message.data(), message.size(), 0));
				*block->responsecount=*block->responsecount+1;
				logger->writeLog(6,logger->setLogData("<KAThread::checkAndSend3> " "Message was created and sent to the shared memory"));
			}
			else	//stdout is not in capture mode so it will not be blocked
			{
				/*
				 * send main buffers without block output
				 */
				string message = response.createResponseMessage(command->getUuid(),*block->processpid,
						command->getRequestSequenceNumber(),*block->responsecount,*errBuff,*outBuff,command->getSource(),command->getTaskUuid());
				logger->writeLog(7,logger->setLogData("<KAThread::checkAndSend4> " "Message was created for sending to the shared memory","Message:",message));
				while(!messageQueue->try_send(message.data(), message.size(), 0));
				*block->responsecount=*block->responsecount+1;
				logger->writeLog(6,logger->setLogData("<KAThread::checkAndSend5> " "Message was created and sent to the shared memory"));
			}
		}
	}
}
/**
 *  \details   This method is mainly writes the buffers to the files if the modes are capture.
 *  		   This method calls when any response comes to the error or output buffer.
 */
void KAThread::checkAndWrite(message_queue *messageQueue,KAStreamReader* Stream,string* outBuff,string* errBuff,
		KACommand* command,numbers* block,KALogger* logger)
{
	logger->writeLog(6,logger->setLogData("<KAThread::checkAndWrite> Method starts... "));
	logger->writeLog(7,logger->setLogData("<KAThread::checkAndWrite> ","outBuff:",*outBuff));
	/*
	 * Appending output and error buffer results to real buffers
	 */
	if(Stream->getIdentity()=="output")
	{
		outBuff->append(Stream->getBuffer());
		if(Stream->getMode()!="RETURN")		//if the mode is different from RETURN it should have to be written to the file..
		{
			if(Stream->openFile())
			{
				Stream->appendFile(Stream->getBuffer());
				Stream->closeFile();
			}
		}
	}
	else if(Stream->getIdentity()=="error")
	{
		errBuff->append(Stream->getBuffer());
		if(Stream->getMode()!="RETURN")		//if the mode is different from RETURN it should have to be written to the file..
		{
			if(Stream->openFile())
			{
				Stream->appendFile(Stream->getBuffer());
				Stream->closeFile();
			}
		}
	}
	unsigned int outBuffsize = outBuff->size();					//real output buffer size
	logger->writeLog(7,logger->setLogData("<KAThread::checkAndWrite2> ","OutBuffSize:",toString(outBuffsize)));
	unsigned int errBuffsize = errBuff->size();					//real error buffer size
	logger->writeLog(7,logger->setLogData("<KAThread::checkAndWrite3> ","errBuffSize:",toString(errBuffsize)));
	if( outBuffsize >= 1000 || errBuffsize >= 1000 )
	{
		if( outBuff->size() >= 1000 && errBuff->size() >= 1000 )		//Both buffer is big enough than standard size ?
		{
			string divisionOut = outBuff->substr(1000,(outBuffsize-1000));	//cut the excess string from buffer
			*outBuff = outBuff->substr(0,1000);

			string divisionErr= errBuff->substr(1000,(errBuffsize-1000));	//cut the excess string from buffer
			*errBuff = errBuff->substr(0,1000);
			logger->writeLog(6,logger->setLogData("<KAThread::checkAndWrite> checkAndSend method is calling..."));
			checkAndSend(messageQueue,Stream,outBuff,errBuff,command,block,logger);
			logger->writeLog(6,logger->setLogData("<KAThread::checkAndWrite> checkAndSend method finished"));

			outBuff->clear();
			errBuff->clear();
			*outBuff=divisionOut;
			*errBuff=divisionErr;
		}
		else if( outBuffsize >= 1000 )
		{
			string divisionOut = outBuff->substr(1000,(outBuffsize-1000));	//cut the excess string from buffer
			*outBuff = outBuff->substr(0,1000);
			logger->writeLog(7,logger->setLogData("<KAThread::checkAndWrite> ","Excess_DataSize:",toString(divisionOut.size())));
			logger->writeLog(7,logger->setLogData("<KAThread::checkAndWrite> ","CuttedDataSize:",toString(outBuffsize)));
			logger->writeLog(7,logger->setLogData("<KAThread::checkAndWrite> ","Excess_Data:",divisionOut));
			logger->writeLog(7,logger->setLogData("<KAThread::checkAndWrite> ","CuttedData:",*outBuff));
			checkAndSend(messageQueue,Stream,outBuff,errBuff,command,block,logger);

			outBuff->clear();
			errBuff->clear();
			*outBuff=divisionOut;
		}
		else if( errBuffsize >= 1000 )
		{
			string divisionErr = errBuff->substr(1000,(errBuffsize-1000));	//cut the excess string from buffer
			*errBuff = errBuff->substr(0,1000);
			logger->writeLog(6,logger->setLogData("<KAThread::checkAndWrite> checkAndSend method is calling..."));
			checkAndSend(messageQueue,Stream,outBuff,errBuff,command,block,logger);
			logger->writeLog(6,logger->setLogData("<KAThread::checkAndWrite> checkAndSend method finished..."));
			outBuff->clear();
			errBuff->clear();
			*errBuff=divisionErr;
		}
	}
}
/**
 *  \details   This method is one of the most important method of the KAThread class.
 *  		   It captures intermediate response from pipeline.
 */
void KAThread::capture(message_queue *messageQueue,KACommand* command,KAStreamReader* Stream,
		mutex* mymutex,string* outBuff,string* errBuff,numbers* block,KALogger* logger)
{
	Stream->setTimeout(command->getTimeout());
	Stream->prepareFileDec();
	logger->writeLog(6,logger->setLogData("<KAThread::capture> " "Capturing Started!!"));

	boost::posix_time::ptime startingpoint = boost::posix_time::second_clock::local_time();
	int startingsec =  startingpoint.time_of_day().seconds();

	while(true)
	{
		Stream->startSelection();
		logger->writeLog(7,logger->setLogData("<KAThread::capture> " "Selection:",toString(Stream->getSelectResult()),"Identity:",Stream->getIdentity()));
		Stream->clearBuffer();

		if(command->getTimeout()!=0)
		{
			boost::posix_time::ptime current = boost::posix_time::second_clock::local_time();
			int currentsec =  current.time_of_day().seconds();

			if((currentsec-startingsec) >= 0)
			{
				if((currentsec-startingsec) >= command->getTimeout())
				{
					logger->writeLog(4,logger->setLogData("<KAThread::capture> " "Timeout Occured!!"));
					Stream->setSelectResult(0);
					break;
				}
			}
			else
			{
				if((60+currentsec-startingsec) >= command->getTimeout())
				{
					logger->writeLog(4,logger->setLogData("<KAThread::capture> " "Timeout Occured!!"));
					Stream->setSelectResult(0);
					break;
				}
			}
		}
		if (Stream->getSelectResult()==0)
		{
			/*
			 * Timeout occured!!
			 */
			logger->writeLog(6,logger->setLogData("<KAThread::capture> " "Timeout has been occured","Identity:",Stream->getIdentity()));
			break;
		}
		else if (Stream->getSelectResult()==-1)
		{
			logger->writeLog(3,logger->setLogData("<KAThread::capture> " "Error opening pipe.."));
			break;
		}
		else
		{
			if (Stream->getSelectResult() != 0)
			{
				Stream->clearBuffer();
				Stream->startReading();
			}
			if (Stream->getReadResult() > 0)
			{
				*(block->flag)=true;

				mymutex->lock();
				checkAndWrite(messageQueue,Stream,outBuff,errBuff,command,block,logger);
				mymutex->unlock();
				logger->writeLog(6,logger->setLogData("<KAThread::capture> " "Result:",toString(Stream->getReadResult()),"Identity:",Stream->getIdentity()));
				logger->writeLog(6,logger->setLogData("<KAThread::capture> " "Buffer:",Stream->getBuffer(),"Identity:",Stream->getIdentity()));
			}
			else
			{
				/*
				 * End of file.
				 */
				logger->writeLog(6,logger->setLogData("<KAThread::capture> " "Result:",toString(Stream->getReadResult()),"Identity:",Stream->getIdentity()));
				logger->writeLog(7,logger->setLogData("<KAThread::capture> " "Breaking!!","Identity:",Stream->getIdentity()));
				break;
			}
		}
	}
	logger->writeLog(6,logger->setLogData("<KAThread::capture> ""Thread is closing!!","Identity:",Stream->getIdentity()));
}
/**
 *  \details   This method is creating the capturing threads and timeout thread.
 *  		   It also gets the process id of the execution.
 *  		   It manages the lifecycle of the threads and handles capturing and sending execution responses using these threads.
 */
int KAThread::optionReadSend(message_queue* messageQueue,KACommand* command,
		KAStreamReader* errorStream,KAStreamReader* outputStream,int newpid)
{
	/*
	 *	Getting system pid of child process
	 *	For example, after this block, processpid should be pid of running command (e.g. tail)
	 */
	int status;
	int processpid = newpid;
	pid_t result = waitpid(newpid, &status, WNOHANG);
	logger.writeLog(6,logger.setLogData("<KAThread::optionReadSend> " "Find pid start","current pid:",toString(newpid)));
	while ((result = waitpid(newpid, &status, WNOHANG)) == 0)
	{
		string cmd;
		cmd = "pgrep -P "+toString(newpid);
		cmd = this->getProcessPid(cmd.c_str());

		cmd = "pgrep -P "+ cmd;
		cmd = this->getProcessPid(cmd.c_str());

		processpid = atoi(cmd.c_str());
		if(processpid)
		{
			break;
		}
		processpid=newpid;
	}
	if(result > 0)
	{
		processpid=newpid;
		//return 1;
	}
	logger.writeLog(6,logger.setLogData("<KAThread::optionReadSend> " "Find pid finished","current pid:",toString(processpid)));

	/*
	 * if the execution is done process pid could not be read and should be skipped now..
	 */

	int responsecount=1;
	bool flag=false;			//flag for acrivity check for stderr and stdout
	boost::mutex mymutex;
	numbers block;
	block.flag=&flag;
	block.processpid=&processpid;
	block.responsecount=&responsecount;
	block.logger=&logger;
	string outBuff, errBuff;	//general buffers for error and output
	/*
	 * timeoutthread is used for send I'm alive message in periodically if there is no activity on stderr and stdout.
	 */
	if(command->getTimeout() == 0)
	{
		boost::thread timeoutthread(taskTimeout,messageQueue,command,&processpid,&outBuff,&errBuff,&block,&(this->getLogger()));
		logger.writeLog(5,logger.setLogData("<KAThread::optionReadSend> " "taskTimeout thread was started"));
	}
	if(command->getStandardOutput()!="NO" && command->getStandardError()!="NO" )
	{
		/*
		 * StandardOutput and StandardError will not be ignored
		 */
		boost::thread outthread(capture,messageQueue,command,outputStream,&mymutex,&outBuff,&errBuff,&block,&(this->getLogger()));
		logger.writeLog(5,logger.setLogData("<KAThread::optionReadSend> " "Output Capture thread was started"));
		boost::thread errorthread(capture,messageQueue,command,errorStream,&mymutex,&outBuff,&errBuff,&block,&(this->getLogger()));
		logger.writeLog(5,logger.setLogData("<KAThread::optionReadSend> " "Error Capture thread was started"));

		outthread.join();
		errorthread.join();
	}
	else if(command->getStandardOutput()!="NO")
	{
		/*
		 * StandardOutput will be ignored
		 */
		boost::thread outthread(capture,messageQueue,command,outputStream,&mymutex,&outBuff,&errBuff,&block,&(this->getLogger()));
		logger.writeLog(5,logger.setLogData("<KAThread::optionReadSend> " "Output Capture thread was started"));
		outthread.join();
		errorStream->setReadResult(0);
		errorStream->setSelectResult(0);
	}
	else if(command->getStandardError()!="NO")
	{
		/*
		 * StandardError will be ignored
		 */
		boost::thread errorthread(capture,messageQueue,command,errorStream,&mymutex,&outBuff,&errBuff,&block,&(this->getLogger()));
		logger.writeLog(5,logger.setLogData("<KAThread::optionReadSend> " "Error Capture thread was started"));
		errorthread.join();
		outputStream->setReadResult(0);
		outputStream->setSelectResult(0);
	}
	if( errorStream->getReadResult() == 0 && outputStream->getReadResult() == 0 )
	{
		/*
		 * Execute Done Response is sending..
		 */
		int exitcode = 0; // to detect is there any error in the message so if there is an error exitcode should be set to 1. Otherwise is 0.
		if(!errBuff.empty())
			exitcode = 1;
		lastCheckAndSend(messageQueue,command,&outBuff,&errBuff,&block,&(this->getLogger()));
		logger.writeLog(6,logger.setLogData("<KAThread::optionReadSend> " "LastCheckSend function has finished at ReadResult"));
		string message = response.createExitMessage(command->getUuid(),processpid,
				command->getRequestSequenceNumber(),responsecount,command->getSource(),command->getTaskUuid(),exitcode);
		while(!messageQueue->try_send(message.data(), message.size(), 0));
		logger.writeLog(6,logger.setLogData("<KAThread::optionReadSend> " "Process Last Message",message));
	}
	if(command->getStandardOutput()=="NO" && command->getStandardError()=="NO" )
	{
		sleep(command->getTimeout());
		if(command->getTimeout()==0)
		{
			pause();
		}
		string message = response.createTimeoutMessage(command->getUuid(),processpid,
				command->getRequestSequenceNumber(),responsecount,"","",command->getSource(),command->getTaskUuid());
		while(!messageQueue->try_send(message.data(), message.size(), 0));
		logger.writeLog(6,logger.setLogData("<KAThread::optionReadSend> " "Process Last Message",message));
		if(processpid)
		{
			logger.writeLog(6,logger.setLogData("<KAThread::optionReadSend> " "Process will be killed.","pid:",toString(processpid)));
			kill(processpid,SIGKILL); //killing the process after timeout
		}
		else
		{
			logger.writeLog(3,logger.setLogData("<KAThread::optionReadSend> " "Process pid is not valid.","pid:",toString(processpid)));
		}
	}
	if( errorStream->getSelectResult() == 0 && outputStream->getSelectResult() == 0 )
	{
		/*
		 * Timeout Response is sending..
		 */
		lastCheckAndSend(messageQueue,command,&outBuff,&errBuff,&block,&(this->getLogger()));
		logger.writeLog(7,logger.setLogData("<KAThread::optionReadSend> " "LastCheckSend function has finished at SelectResult"));
		string message = response.createTimeoutMessage(command->getUuid(),processpid,
				command->getRequestSequenceNumber(),responsecount,"","",command->getSource(),command->getTaskUuid());
		while(!messageQueue->try_send(message.data(), message.size(), 0));
		logger.writeLog(7,logger.setLogData("<KAThread::optionReadSend> " "Process Last Message",message));
		if(processpid)
		{
			logger.writeLog(7,logger.setLogData("<KAThread::optionReadSend> " "Process will be killed.","pid:",toString(processpid)));
			kill(processpid,SIGKILL); //killing the process after timeout
		}
		else
		{
			logger.writeLog(6,logger.setLogData("<KAThread::optionReadSend> " "Process pid is not valid.","pid:",toString(processpid)));
		}
	}
	logger.writeLog(6,logger.setLogData("<KAThread::optionReadSend> " "Capturing is Done!!"));
	return true;
}
/**
 *  \details   This method is the main method that forking a new process.
 *  		   It execute the command.
 *  		   It also uses Output and Error Streams for capturing the execution responses.
 *  		   if the execution successfully done, it returns true.
 *  		   Otherwise it returns false.
 */
bool KAThread::threadFunction(message_queue* messageQueue,KACommand *command,char* argv[])
{
	signal(SIGCHLD, SIG_IGN);		//when the child process done it will be raped by kernel. We do not allowed zombie processes.
	pid=fork();						//creating a child process
	if(pid==0)		//child process is starting
	{
		int argv0size = strlen(argv[0]);
		strncpy(argv[0],"ksks-agent-child",argv0size);
		logger.openLogFile(getpid(),command->getRequestSequenceNumber());
		string pidparnumstr = toString(getpid());		//geting pid number of the process
		string processpid="";	//processpid for execution
		logger.writeLog(6,logger.setLogData("<KAThread::threadFunction> " "New Main Fork is Starting!!",toString(getpid())));
		KAStreamReader outputStream(command->getStandardOutput(),command->getStandardOutputPath(),"output");
		KAStreamReader errorStream(command->getStandardError(),command->getStandardErrPath(),"error");
		if(outputStream.openPipe()==false || errorStream.openPipe()==false)
		{
			/* an error occurred pipe of pipeerror or output */
			logger.writeLog(6,logger.setLogData("<KAThread::threadFunction> " "Error opening pipes!!"));
		}
		int newpid=fork();
		if(newpid==0)
		{	// Child execute the command
			string pidchldnumstr = toString(getpid());
			logger.writeLog(6,logger.setLogData("<KAThread::threadFunction> " "New Child Process is starting for pipes","Parentpid",pidparnumstr,"pid",pidchldnumstr));
			outputStream.PreparePipe();
			errorStream.PreparePipe();

			if(!checkCWD(command))
			{
				string message = response.createResponseMessage(command->getUuid(),getpid(),
						command->getRequestSequenceNumber(),1,"Working Directory Does Not Exist on System","",command->getSource(),command->getTaskUuid());
				while(!messageQueue->try_send(message.data(), message.size(), 0));
				message = response.createExitMessage(command->getUuid(),atoi(processpid.c_str()),
						command->getRequestSequenceNumber(),2,command->getSource(),command->getTaskUuid(),1);
				while(!messageQueue->try_send(message.data(), message.size(), 0));
				logger.writeLog(7,logger.setLogData("<KAThread::threadFunction> " "CWD id not found on system..","CWD:",command->getWorkingDirectory()));
				kill(getpid(),SIGKILL);		//killing child
				exit(1);
				//problem about absolute path
			}
			if(!checkUID(command))
			{
				string message = response.createResponseMessage(command->getUuid(),getpid(),
						command->getRequestSequenceNumber(),1,"User Does Not Exist on System","",command->getSource(),command->getTaskUuid());
				while(!messageQueue->try_send(message.data(), message.size(), 0));
				message = response.createExitMessage(command->getUuid(),atoi(processpid.c_str()),
						command->getRequestSequenceNumber(),2,command->getSource(),command->getTaskUuid(),1);
				while(!messageQueue->try_send(message.data(), message.size(), 0));
				logger.writeLog(6,logger.setLogData("<KAThread::threadFunction> " "USer id not found on system..","RunAs:",command->getRunAs()));
				kill(getpid(),SIGKILL);		//killing child
				exit(1);
				//problem about UID
			}
			logger.writeLog(6,logger.setLogData("<KAThread::threadFunction> " "Execution is starting!!","pid",pidchldnumstr));
			system(createExecString(command).c_str());	//execution of command is starting now..
			logger.writeLog(6,logger.setLogData("<KAThread::threadFunction> " "Execution is done!!","pid",pidchldnumstr));
			exit(EXIT_SUCCESS);
		}
		else if (newpid==-1)
		{
			cout << "ERROR!!" << endl;
			return false;
		}
		else
		{
			//Parent read the result and send back
			try
			{
				errorStream.closePipe(1);
				outputStream.closePipe(1);
				logger.writeLog(6,logger.setLogData("<KAThread::threadFunction> " "optionReadSend is starting!!","pid",toString(getpid())));
				optionReadSend(messageQueue,command,&errorStream,&outputStream,newpid);
				logger.writeLog(6,logger.setLogData("<KAThread::threadFunction> " "optionReadSend has finished!!","pid",toString(getpid())));
				errorStream.closePipe(0);
				outputStream.closePipe(0);
				logger.writeLog(6,logger.setLogData("<KAThread::threadFunction> " "New Main Thread is Stopping!!","pid",toString(getpid())));
				logger.closeLogFile();
				kill(getpid(),SIGKILL);		//killing child
				return true; //thread successfully done its work.
			}
			catch(const std::exception& error)
			{
				logger.writeLog(3,logger.setLogData("<KAThread::threadFunction> " "Problem TF:",error.what()));
				cout<<error.what()<<endl;
			}
		}
	}
	else if (pid == -1)
	{
		//log.writeLog(7,logger.setLogData("<KAThread::threadFunction> " "ERROR DURING MAIN FORK!!","pid",toString(getpid())));
		return false;
	}
	else if( pid > 0 )	//parent continue its process and return back
	{
		//logger.writeLog(7,logger.setLogData("<KAThread::threadFunction> " "Parent Process continue!!","pid",toString(getpid())));
		return true;	//parent successfully done
	}
	return true; //child successfully done
}
/**
 *  \details   getting "logger" private variable of KAThread instance.
 */
KALogger& KAThread::getLogger()
{
	return this->logger;
}
/**
 *  \details   setting "logger" private variable of KAThread instance.
 */
void KAThread::setLogger(KALogger mylogger)
{
	this->logger=mylogger;
}
/**
 *  \details   getting "uid" private variable of KAThread instance.
 */
KAUserID& KAThread::getUserID()
{
	return this->uid;
}
/**
 *  \details   This method checks the flag result that indicates activity on the error and output streams.
 *  		   If there is no activity during 60 seconds(default timeout) this thread sends a I'm alive message to the ActiveMQ Broker.
 */
void KAThread::taskTimeout(message_queue *messageQueue,KACommand* command,
		int* pid,string* outBuff,string* errBuff,numbers* block,KALogger* logger)
{
	logger->writeLog(6,logger->setLogData("<KAThread::taskTimeout> " "taskTİmeOut is starting!!","pid",toString(getpid())));
	try
	{
		KAResponsePack myresponse;
		unsigned int counter = 0;
		while (true)
		{
			sleep(1);
			logger->writeLog(6,logger->setLogData("<KAThread::taskTimeout> " "Flag","pid",toString(getpid())));
			if ((int)*block->flag)	//if any activity on the process flag is changed to true, if not is changed to false and send message
			{
				//do nothing
				*block->flag=false;
				counter=0;
			}
			else
			{
				counter++;
			}
			if(counter==30)
			{
				if(outBuff->empty() && errBuff->empty())
				{
					/*
					 * sending I'm alive message
					 */
					string message = myresponse.createResponseMessage(command->getUuid(),*pid,
							command->getRequestSequenceNumber(),-1,"","",command->getSource(),command->getTaskUuid());
					while(!messageQueue->try_send(message.data(), message.size(), 0));
					*block->responsecount=*block->responsecount+1;
					logger->writeLog(6,logger->setLogData("<KAThread::taskTimeout> " "taskTİmeOut sends message!!","pid",toString(getpid()),"Message:",message));
					counter=0;
				}
				else
				{
					if(command->getStandardOutput()=="CAPTURE" && command->getStandardError()=="CAPTURE")
					{
						errBuff->clear();
						outBuff->clear();
					}
					else if(command->getStandardOutput()=="CAPTURE")
					{
						outBuff->clear();
					}
					else if(command->getStandardError()=="CAPTURE")
					{
						errBuff->clear();
					}
					/*
					 * sending I'm alive message
					 */
					string message = myresponse.createResponseMessage(command->getUuid(),*pid,
							command->getRequestSequenceNumber(),-1,*errBuff,*outBuff,command->getSource(),command->getTaskUuid());
					while(!messageQueue->try_send(message.data(), message.size(), 0));
					outBuff->clear();
					errBuff->clear();
					*block->responsecount=*block->responsecount+1;
					logger->writeLog(6,logger->setLogData("<KAThread::taskTimeout> " "taskTİmeOut sends message!!","pid",toString(getpid()),"Message:",message));
					counter=0;
				}
			}
		}
	}
	catch(const std::exception& error)
	{
		logger->writeLog(3,logger->setLogData("<KAThread::taskTimeout> " "Problem is occured:",error.what()));
	}
}
/**
 *  \details   This method executes the given command and returns its answer.
 *  		   This is used for getting pid of the execution.
 */
string KAThread::getProcessPid(const char* cmd)
{
	FILE* pipe = popen(cmd, "r");
	if (!pipe)
	{
		return "ERROR";
	}
	char buffer[128];
	string result = "";
	while(!feof(pipe))
	{
		if(fgets(buffer, 128, pipe) != NULL)
		{
			buffer[strlen(buffer)-1]='\0';
			result = buffer;
		}
	}
	pclose(pipe);
	return result;
}
/**
 *  \details   This method designed for Typically conversion from integer to string.
 */
string KAThread::toString(int intcont)
{		//integer to string conversion
	ostringstream dummy;
	dummy << intcont;
	return dummy.str();
}
