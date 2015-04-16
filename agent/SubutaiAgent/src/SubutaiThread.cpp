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
#include "SubutaiThread.h"

/**
 *  \details   Default constructor of the SubutaiThread class.
 */
SubutaiThread::SubutaiThread() :
		_isContainer(false) {
	setCWDERR(false);
	setUIDERR(false);
	setResponsecount(1);
	setACTFLAG(false);
	setEXITSTATUS(0);
}

/**
 *  \details   Default destructor of the SubutaiThread class.
 */
SubutaiThread::~SubutaiThread() {
	// TODO Auto-generated destructor stub
}

/**
 *  \details   This method checks CurrentWorking Directory in the command
 *  		   If given CWD does not exist on system, it returns false otherwise it returns true.
 */
bool SubutaiThread::checkCWD(SubutaiCommand *command, SubutaiContainer* cont) {
	if (cont && command->getWorkingDirectory() != "") {
		try {
			return cont->checkCWD(command->getWorkingDirectory());
		} catch (SubutaiException e) {
			logger.writeLog(3,
					logger.setLogData(
							"<SubutaiThread::checkCWD> " "Exception: ",
							e.displayText()));
		} catch (std::exception e) {
			logger.writeLog(3,
					logger.setLogData("<SubutaiThread::checkCWD> " "Exception:",
							e.what()));
		}
	} else {
		if ((chdir(command->getWorkingDirectory().c_str())) < 0) {
			//changing working directory first
			logger.writeLog(3,
					logger.setLogData(
							"<SubutaiThread::checkCWD> " " Changing working Directory failed..",
							"pid", helper.toString(getpid()), "CWD",
							command->getWorkingDirectory()));
			return false;
		} else {
			return true;
		}
	}
}

/**
 *  \details   This method checks and add user Directory in the command
 *  		   If given CWD does not exist on system, it returns false otherwise it returns true.
 */
bool SubutaiThread::checkUID(SubutaiCommand *command,
		SubutaiContainer* container) {
	if (container && command->getRunAs() != "") {
		try {
			return container->checkUser(command->getRunAs());
		} catch (SubutaiException e) {
			logger.writeLog(3,
					logger.setLogData(
							"<SubutaiThread::checkCWD> " "Exception: ",
							e.displayText()));
		} catch (std::exception e) {
			logger.writeLog(3,
					logger.setLogData("<SubutaiThread::checkCWD> " "Exception:",
							e.what()));
		}
	} else {
		if (uid.getIDs(ruid, euid, command->getRunAs())) {
			logger.writeLog(4,
					logger.setLogData(
							"<SubutaiThread::checkUID> " "User id successfully found on system..",
							"pid", helper.toString(getpid()), "RunAs",
							command->getRunAs()));
			uid.doSetuid(this->euid);
			return true;
		} else {
			logger.writeLog(3,
					logger.setLogData(
							"<SubutaiThread::checkUID> " "User id could not found on system..",
							"pid", helper.toString(getpid()), "RunAs",
							command->getRunAs()));
			logger.writeLog(3,
					logger.setLogData(
							"<SubutaiThread::checkUID> " "Thread will be closed..",
							"pid", helper.toString(getpid()), "RunAs",
							command->getRunAs()));
			uid.undoSetuid(ruid);
			return false;
		}
	}
}

/**
 *  \details   This method creates execution string.
 *  		   It combines (environment parameters set if it is exist) && program + arguments.
 *  		   It returns this combined string to be execution.
 */
string SubutaiThread::createExecString(SubutaiCommand *command) {
	string arg, env;
	exec.clear();
	logger.writeLog(6,
			logger.setLogData(
					"<SubutaiThread::createExecString>" "Method starts...",
					"pid", helper.toString(getpid())));
	argument.clear();
	environment.clear();
	for (unsigned int i = 0; i < command->getArguments().size(); i++)//getting arguments for creating Execution string
			{
		arg = command->getArguments()[i];
		argument = argument + arg + " ";
		logger.writeLog(7,
				logger.setLogData("<SubutaiThread::createExecString>", "pid",
						helper.toString(getpid()), "Argument", arg.c_str()));
	}
	logger.writeLog(7,
			logger.setLogData(
					"<SubutaiThread::createExecString> " "Full Argument List: ",
					"Arguments:", argument));
	if (!command->getIsDaemon()) {
		for (std::list<pair<string, string> >::iterator it =
				command->getEnvironment().begin();
				it != command->getEnvironment().end(); it++) {
			arg = it->first.c_str();
			env = it->second.c_str();
			logger.writeLog(7,
					logger.setLogData(
							"<SubutaiThread::createExecString> Environment Parameters",
							"Parameter", arg, " = ", env));
			environment = environment + " export " + arg + " = " + env + " && ";
		}
		logger.writeLog(7,
				logger.setLogData(
						"<SubutaiThread::createExecString> " "Full Environment List: ",
						"Environments:", environment));
	}
	if (environment.empty()) {
		exec = command->getCommand() + " " + argument;//arguments added execution string
		logger.writeLog(7,
				logger.setLogData(
						"<SubutaiThread::createExecString> " "Execution command has been created without environment",
						"Command:", exec));
	} else {
		exec = environment + command->getCommand() + " " + argument;
		logger.writeLog(7,
				logger.setLogData(
						"<SubutaiThread::createExecString> " "Execution command has been created with environment",
						"Command:", exec));
	}
	logger.writeLog(6,
			logger.setLogData(
					"<SubutaiThread::createExecString>" "CreateExecString Method finished....",
					"pid", helper.toString(getpid())));
	return exec;
}

/*
 *
 */
bool SubutaiThread::ExecuteCommand(SubutaiCommand* command,
		SubutaiContainer* container) {
	try {
		container->RunCommand(command);
	} catch (SubutaiException e) {
		logger.writeLog(7,
				logger.setLogData("<SubutaiThread::createExecString> " "",
						e.displayText()));
	}
	return true;
}

void SubutaiThread::captureOutputBuffer(message_queue* messageQueue,
		SubutaiCommand* command, bool outputBuffer, bool errorBuffer) {
	string message = this->getResponse().createResponseMessage(
			command->getUuid(), this->getPpid(),
			command->getRequestSequenceNumber(), this->getResponsecount(),
			this->geterrBuff(), this->getoutBuff(), command->getCommandId());
	this->getLogger().writeLog(7,
			this->getLogger().setLogData(
					"<SubutaiThread::lastCheckAndSend> " "Message was created for send to the shared memory",
					"Message:", message));
	while (!messageQueue->try_send(message.data(), message.size(), 0))
		;
	this->getResponsecount() = this->getResponsecount() + 1;
	this->getLogger().writeLog(7,
			this->getLogger().setLogData(
					"<SubutaiThread::lastCheckAndSend> " "Message was created and sent to the shared memory"));
	if (outputBuffer) {
		this->getoutBuff().clear();
	}
	if (errorBuffer) {
		this->geterrBuff().clear();
	}
}

/**
 *  \details   This method check lastly buffer results and sends the buffers to the ActiveMQ broker.
 *  		   This method is only called when the timeout occured or process is done.
 */
void SubutaiThread::lastCheckAndSend(message_queue *messageQueue,
		SubutaiCommand* command) {
	this->getLogger().writeLog(6,
			this->getLogger().setLogData(
					"<SubutaiThread::lastCheckAndSend> " "The method starts..."));
	unsigned int outBuffsize = this->getoutBuff().size();//real output buffer size
	unsigned int errBuffsize = this->geterrBuff().size();//real error buffer size

	if (outBuffsize != 0 || errBuffsize != 0) {
		if (outBuffsize != 0 && errBuffsize != 0) {
			if (command->getStandardOutput() == "RETURN"
					&& command->getStandardError() == "RETURN") {
				// Send main buffers without blocking output and error
				this->captureOutputBuffer(messageQueue, command, true, true);
			} else if (command->getStandardOutput() == "RETURN") {
				// send main buffers with block error buff
				this->geterrBuff().clear();
				this->captureOutputBuffer(messageQueue, command, true, false);
			} else if (command->getStandardError() == "RETURN") {
				// send main buffers with blocking error buff
				this->getoutBuff().clear();
				this->captureOutputBuffer(messageQueue, command, false, true);
			} else {
				this->getoutBuff().clear();
				this->geterrBuff().clear();
			}
		} else if (outBuffsize != 0) {
			if (command->getStandardOutput() == "RETURN") {
				// send main buffers without block output. (errbuff size is zero)
				this->geterrBuff().clear();
				this->captureOutputBuffer(messageQueue, command, true, false);
			} else {
				this->getoutBuff().clear();
				this->geterrBuff().clear();
			}
		} else if (errBuffsize != 0) {
			if (command->getStandardError() == "RETURN") {
				// send main buffers without block output. (errbuff size is zero)
				this->getoutBuff().clear();
				this->captureOutputBuffer(messageQueue, command, false, true);
			} else {
				this->getoutBuff().clear();
				this->geterrBuff().clear();
			}
		}
	}
}

/**
 *  \details   This method check buffer results and sends the buffers to the ActiveMQ broker.
 *  		   This method calls when any buffer result overflow MaxBuffSize = 1000 bytes.
 */
void SubutaiThread::checkAndSend(message_queue* messageQueue,
		SubutaiCommand* command) {
	this->getLogger().writeLog(6,
			this->getLogger().setLogData(
					"<SubutaiThread::checkAndSend> Method starts..."));

	//if output is RETURN
	if (this->getOutputStream().getMode() == "RETURN") {
		if (command->getStandardError() == "NO") {
			// send main buffers with blocking error
			this->geterrBuff().clear();
			this->captureOutputBuffer(messageQueue, command, false, false);
		} else {
			//stderr is not in capture mode so it will not be blocked
			// send main buffers without blocking
			this->captureOutputBuffer(messageQueue, command, false, false);
		}
	} else {	//out capture or No so it should be blocked
		if (command->getStandardError() != "NO") {
			// send main buffers without block output
			this->getoutBuff().clear();
			this->captureOutputBuffer(messageQueue, command, false, false);
		}

	}
}

/**
 *  \details   This method is mainly writes the buffers to the files if the modes are capture.
 *  		   This method calls when any response comes to the error or output buffer.
 */
void SubutaiThread::checkAndWrite(message_queue *messageQueue,
		SubutaiCommand* command) {
	unsigned int MaxBuffSize = 1000; //max Packet size is setting with 1000 chars.
	this->getLogger().writeLog(6,
			this->getLogger().setLogData(
					"<SubutaiThread::checkAndWrite> Method starts... "));
	/*
	 * Appending output and error buffer results to real buffers
	 */
	this->getoutBuff().append(this->getOutputStream().getBuffer());	//appending buffers.
	if (this->getOutputStream().getMode() == "CAPTURE"
			|| this->getOutputStream().getMode() == "CAPTURE_AND_RETURN") {
		this->getLogger().writeLog(7,
				this->getLogger().setLogData(
						"<SubutaiThread::checkAndWrite> Starting Capturing Output.."));
		if (this->getOutputStream().openFile()) {
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::checkAndWrite> CAPTURE Output: ",
							this->getOutputStream().getBuffer()));
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::checkAndWrite> CAPTURE Output is written to file... "));
			this->getOutputStream().appendFile(
					this->getOutputStream().getBuffer());
			this->getOutputStream().closeFile();
		}
	}
	this->getOutputStream().clearBuffer();	//clear stream buffer
	this->geterrBuff().append(this->getErrorStream().getBuffer()); //appending buffers.
	if (this->getErrorStream().getMode() == "CAPTURE"
			|| this->getErrorStream().getMode() == "CAPTURE_AND_RETURN") {
		this->getLogger().writeLog(7,
				this->getLogger().setLogData(
						"<SubutaiThread::checkAndWrite> Starting Capturing Error.."));
		if (this->getErrorStream().openFile()) {
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::checkAndWrite> CAPTURE Error: ",
							this->getErrorStream().getBuffer()));
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::checkAndWrite> CAPTURE Error is written to file... "));
			this->getErrorStream().appendFile(
					this->getErrorStream().getBuffer());
			this->getErrorStream().closeFile();
		}
	}
	this->getErrorStream().clearBuffer();	//clear stream buffer

	unsigned int outBuffsize = this->getoutBuff().size();//real output buffer size
	this->getLogger().writeLog(7,
			this->getLogger().setLogData("<SubutaiThread::checkAndWrite2> ",
					"OutBuffSize:", helper.toString(outBuffsize)));
	unsigned int errBuffsize = this->geterrBuff().size();//real error buffer size
	this->getLogger().writeLog(7,
			this->getLogger().setLogData("<SubutaiThread::checkAndWrite3> ",
					"errBuffSize:", helper.toString(errBuffsize)));

	if (errBuffsize > 0)	//if there is an error in the pipe, it will be set.
		this->setEXITSTATUS(1);

	if (outBuffsize >= MaxBuffSize || errBuffsize >= MaxBuffSize) {
		if (outBuffsize >= MaxBuffSize && errBuffsize >= MaxBuffSize)//Both buffer is big enough than standard size ?
				{
			string divisionOut = this->getoutBuff().substr(MaxBuffSize,
					(outBuffsize - MaxBuffSize));//cut the excess string from output buffer
			this->getoutBuff() = this->getoutBuff().substr(0, MaxBuffSize);
			string divisionErr = this->geterrBuff().substr(MaxBuffSize,
					(errBuffsize - MaxBuffSize));//cut the excess string from buffer
			this->geterrBuff() = this->geterrBuff().substr(0, MaxBuffSize);
			this->getLogger().writeLog(6,
					this->getLogger().setLogData(
							"<SubutaiThread::checkAndWrite> checkAndSend method is calling..."));
			checkAndSend(messageQueue, command);
			this->getLogger().writeLog(6,
					this->getLogger().setLogData(
							"<SubutaiThread::checkAndWrite> checkAndSend method finished"));
			this->getoutBuff().clear();
			this->geterrBuff().clear();
			this->setoutBuff(divisionOut);
			this->seterrBuff(divisionErr);
		} else if (outBuffsize >= MaxBuffSize) {
			string divisionOut = this->getoutBuff().substr(MaxBuffSize,
					(outBuffsize - MaxBuffSize));//cut the excess string from buffer
			this->getoutBuff() = this->getoutBuff().substr(0, MaxBuffSize);
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::checkAndWrite> ",
							"Excess_DataSize:",
							helper.toString(divisionOut.size())));
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::checkAndWrite> ",
							"CuttedDataSize:", helper.toString(outBuffsize)));
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::checkAndWrite> ", "Excess_Data:",
							divisionOut));
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::checkAndWrite> ", "CuttedData:",
							this->getoutBuff()));
			checkAndSend(messageQueue, command);
			this->getLogger().writeLog(6,
					this->getLogger().setLogData(
							"<SubutaiThread::checkAndWrite> checkAndSend method finished"));
			this->getoutBuff().clear();
			this->geterrBuff().clear();
			this->setoutBuff(divisionOut);
		} else if (errBuffsize >= MaxBuffSize) {
			string divisionErr = this->geterrBuff().substr(MaxBuffSize,
					(errBuffsize - MaxBuffSize));//cut the excess string from buffer
			this->geterrBuff() = this->geterrBuff().substr(0, MaxBuffSize);
			this->getLogger().writeLog(6,
					this->getLogger().setLogData(
							"<SubutaiThread::checkAndWrite> checkAndSend method is calling..."));
			checkAndSend(messageQueue, command);
			this->getLogger().writeLog(6,
					this->getLogger().setLogData(
							"<SubutaiThread::checkAndWrite> checkAndSend method finished..."));
			this->getoutBuff().clear();
			this->geterrBuff().clear();
			this->seterrBuff(divisionErr);
		}
	}
}

/**
 *  \details   This method checks the execution timeout value.
 *  		   if execution timeout is occured it returns true. Otherwise it returns false.
 */
bool SubutaiThread::checkExecutionTimeout(unsigned int* startsec,
		bool* overflag, unsigned int* exectimeout, unsigned int* count) {
	if (*exectimeout != 0) {
		boost::posix_time::ptime current =
				boost::posix_time::second_clock::local_time();
		unsigned int currentsec = current.time_of_day().seconds();

		if ((currentsec > *startsec) && *overflag == false) {
			if (currentsec != 59) {
				*count = *count + (currentsec - *startsec);
				*startsec = currentsec;
			} else {
				*count = *count + (currentsec - *startsec);
				*overflag = true;
				*startsec = 0;
			}
		}
		if (currentsec == 59) {
			*overflag = true;
			*startsec = 0;
		} else {
			*overflag = false;
		}
		if (*count >= *exectimeout) { //timeout
			this->getLogger().writeLog(4,
					this->getLogger().setLogData(
							"<SubutaiThread::checkTimeout> " "Timeout Occured!!"));
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::checkTimeout> " "count:",
							helper.toString(*count)));
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::checkTimeout> " "exectimeout:",
							helper.toString(*exectimeout)));
			return true;	//timeout occured now
		} else {
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::checkTimeout> " "count:",
							helper.toString(*count)));
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::checkTimeout> " "exectimeout:",
							helper.toString(*exectimeout)));
			return false; //no timeout occured
		}
	}
	return false;	//no timeout occured
}

/**
 *  \details   This method is creating the capturing threads and timeout thread.
 *  		   It also gets the process id of the execution.
 *  		   It manages the lifecycle of the threads and handles capturing and sending execution responses using these threads.
 */
int SubutaiThread::optionReadSend(message_queue* messageQueue,
		SubutaiCommand* command, int newpid, int* ret, int* sub_pid) {
	/*
	 *	Getting system pid of child process
	 *	For example, after this block, processpid should be pid of running command (e.g. tail)
	 */

	int status;
	this->setPpid(newpid);
	pid_t result = waitpid(newpid, &status, WNOHANG);
	this->getLogger().writeLog(6,
			this->getLogger().setLogData(
					"<SubutaiThread::optionReadSend> " "Find pid start",
					"current pid:", helper.toString(newpid)));
	this->getLogger().writeLog(6,
			this->getLogger().setLogData("<SubutaiThread::optionReadSend> ",
					"*** Pid on start", "current pid:",
					helper.toString(*sub_pid)));
	if (!_container) {
		while ((result = waitpid(newpid, &status, WNOHANG)) == 0) {
			string cmd;
			cmd = "pgrep -P " + helper.toString(newpid);
			cmd = this->getProcessPid(cmd.c_str());
			cmd = "pgrep -P " + cmd;
			cmd = this->getProcessPid(cmd.c_str());
			this->setPpid(atoi(cmd.c_str()));
			if (this->getPpid()) {
				break;
			}
			this->setPpid(newpid);
		}
	}

	if (result > 0) {
		this->setPpid(newpid);
	}
	this->getLogger().writeLog(6,
			this->getLogger().setLogData(
					"<SubutaiThread::optionReadSend> " "Find pid finished",
					"current pid:", helper.toString(processpid)));
	/*
	 * if the execution is done process pid could not be read and should be skipped now..
	 */
	if (!_container && !checkCWD(command)) {
		this->setCWDERR(true);
		string message = this->getResponse().createResponseMessage(
				command->getUuid(), this->getPpid(),
				command->getRequestSequenceNumber(), 1,
				"Working Directory Does Not Exist on System", "",
				command->getCommandId());
		while (!messageQueue->try_send(message.data(), message.size(), 0))
			;
		this->getLogger().writeLog(7,
				this->getLogger().setLogData(
						"<SubutaiThread::optionReadSend> " "CWD id not found on system..",
						"CWD:", command->getWorkingDirectory()));
		//problem about absolute path
	}
	if (!_container && !checkUID(command)) {
		this->setUIDERR(true);
		string message = this->getResponse().createResponseMessage(
				command->getUuid(), this->getPpid(),
				command->getRequestSequenceNumber(), 1,
				"User Does Not Exist on System", "", command->getCommandId());
		while (!messageQueue->try_send(message.data(), message.size(), 0))
			;
		this->getLogger().writeLog(6,
				this->getLogger().setLogData(
						"<SubutaiThread::optionReadSend> " "User id not found on system..",
						"RunAs:", command->getRunAs()));
		//problem about UID
	}

	/*
	 * Starting Execution Timeout
	 */
	boost::posix_time::ptime start =
			boost::posix_time::second_clock::local_time();
	unsigned int exectimeout = command->getTimeout();
	unsigned int startsec = start.time_of_day().seconds();
	bool overflag = false;
	unsigned int count = 0;

	/*
	 * Starting Heartbeat Timeout
	 */
	boost::posix_time::ptime startheart =
			boost::posix_time::second_clock::local_time();
	unsigned int startheartsec = startheart.time_of_day().seconds();
	unsigned int exectimeoutheat = 10;
	bool overflagheat = false;
	unsigned int countheat = 0;

	bool EXECTIMEOUT = false;
	/*
	 * Starting Pipeline Read operation
	 */
	while (true) {
		this->getOutputStream().setTimeout(50000); //50 milisec for pipe timeout
		this->getErrorStream().setTimeout(50000); //50 milisec for pipe timeout
		this->getOutputStream().prepareFileDec();
		this->getErrorStream().prepareFileDec();

		this->getOutputStream().startSelection();	//Selecting Output first
		this->getLogger().writeLog(7,
				this->getLogger().setLogData(
						"<SubutaiThread::optionReadSend> " "Output Selection:",
						helper.toString(
								this->getOutputStream().getSelectResult())));

		this->getErrorStream().startSelection();	//Selecting Error Second
		this->getLogger().writeLog(7,
				this->getLogger().setLogData(
						"<SubutaiThread::optionReadSend> " "Error Selection:",
						helper.toString(
								this->getErrorStream().getSelectResult())));

		if (this->checkExecutionTimeout(&startsec, &overflag, &exectimeout,
				&count)) //checking general Execution Timeout.
				{
			//Execution timeout occured!!
			EXECTIMEOUT = true;
			this->getLogger().writeLog(6,
					this->getLogger().setLogData(
							"<SubutaiThread::optionReadSend> " "EXECUTION TIMEOUT OCCURED!!"));
			break;
		}
		if (this->getACTFLAG() == true) //Reset Heartbeat Timeout.
		{
			setACTFLAG(false);
			startheart = boost::posix_time::second_clock::local_time();	//Reset HeartBeat Timeout values
			startheartsec = startheart.time_of_day().seconds();
			overflagheat = false;
			exectimeoutheat = 10;
			countheat = 0;
		} else	//check the Heartbeat Timeout
		{
			if (this->checkExecutionTimeout(&startheartsec, &overflagheat,
					&exectimeoutheat, &countheat)) {
				//send HeartBeat Message..
				if (this->getoutBuff().empty() && this->geterrBuff().empty()) {
					/*
					 * sending I'm alive message with no output and error buffers
					 *
					 *         	"type":"EXECUTE_RESPONSE",


					 "id":"56b0ac88-5140-4a32-8691-916d75d62f1c"

					 "commandId":"c6cd5988-ceac-11e3-82b2-ebd389e743a3",

					 "pid":32247
					 */
					this->getLogger().writeLog(6,
							this->getLogger().setLogData(
									"<SubutaiThread::optionReadSend> " "(HEARTBEAT TIMEOUT)Sending I'm alive Message!!"));
					string message = this->getResponse().createResponseMessage(
							command->getUuid(), this->getPpid(),
							command->getRequestSequenceNumber(),
							this->getResponsecount(), "", "",
							command->getCommandId());
					this->getLogger().writeLog(7,
							this->getLogger().setLogData(
									"<SubutaiThread::optionReadSend> " "Sending I'm alive Message!!",
									"Message:", message));
					while (!messageQueue->try_send(message.data(),
							message.size(), 0))
						;
					this->getResponsecount() = this->getResponsecount() + 1;
				} else //there is some data on the buffer..
				{
					if ((command->getStandardOutput() == "CAPTURE"
							|| command->getStandardOutput() == "NO")
							&& (command->getStandardError() == "CAPTURE"
									|| command->getStandardError() == "NO")) {
						this->geterrBuff().clear();
						this->getoutBuff().clear();
					} else if (command->getStandardOutput() == "CAPTURE"
							|| command->getStandardOutput() == "NO") {
						this->getoutBuff().clear();
					} else if (command->getStandardError() == "CAPTURE"
							|| command->getStandardError() == "NO") {
						this->geterrBuff().clear();
					}
					/*
					 * sending I'm alive message
					 */
					string message = this->getResponse().createResponseMessage(
							command->getUuid(), this->getPpid(),
							command->getRequestSequenceNumber(),
							this->getResponsecount(), this->geterrBuff(),
							this->getoutBuff(), command->getCommandId());
					while (!messageQueue->try_send(message.data(),
							message.size(), 0))
						;
					this->geterrBuff().clear();
					this->getoutBuff().clear();
					this->getResponsecount() = this->getResponsecount() + 1;
					this->getLogger().writeLog(6,
							this->getLogger().setLogData(
									"<SubutaiThread::optionReadSend> " "(HEARTBEAT TIMEOUT)Sending I'm alive Message!!"));
					this->getLogger().writeLog(7,
							this->getLogger().setLogData(
									"<SubutaiThread::optionReadSend> " "Sending I'm alive Message!!",
									"Message:", message));
				}
				startheart = boost::posix_time::second_clock::local_time();	//Reset HeartBeat Timeout values
				startheartsec = startheart.time_of_day().seconds();
				overflagheat = false;
				exectimeoutheat = 10;
				countheat = 0;
			}
		}
		if (this->getOutputStream().getSelectResult() == -1
				|| this->getErrorStream().getSelectResult() == -1) {
			cout << "ERROR selecting pipelines" << endl;
			return -1;
			//ERROR selecting
		}

		if (this->getOutputStream().getSelectResult() == 0) {
			//dummy timeout
			//TIMEOUT for 50ms with no data
		} else if (this->getOutputStream().getSelectResult() > 0) {
			this->getOutputStream().clearBuffer();
			this->getOutputStream().startReading();
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::optionReadSend> " "Output Read Result:",
							helper.toString(
									this->getOutputStream().getReadResult())));
		}

		if (this->getErrorStream().getSelectResult() == 0) {
			//dummy timeout
			//TIMEOUT for 50ms with no data
		} else if (this->getErrorStream().getSelectResult() > 0) {
			this->getErrorStream().clearBuffer();
			this->getErrorStream().startReading();
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::optionReadSend> " "Error Read Result:",
							helper.toString(
									this->getErrorStream().getReadResult())));
		}

		if (this->getOutputStream().getSelectResult() > 0
				|| this->getErrorStream().getSelectResult() > 0) {
			this->setACTFLAG(true);	//there is an activity on pipes so ACT flag should be set to true to reset heartbeat timeout.
		}
		if (this->getOutputStream().getReadResult() > 0
				|| this->getErrorStream().getReadResult() > 0) {
			//getting buffers
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::optionReadSend> " "Output Read Result:",
							helper.toString(
									this->getOutputStream().getReadResult())));
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::optionReadSend> " "Output Buffer:",
							this->getOutputStream().getBuffer()));
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::optionReadSend> " "Error  Read Result:",
							helper.toString(
									this->getErrorStream().getReadResult())));
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::optionReadSend> " "Error Buffer:",
							this->getErrorStream().getBuffer()));

			this->checkAndWrite(messageQueue, command);
		} else {
			/*
			 * End of Reading pipes.
			 */
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::optionReadSend> " "Output Read Result:",
							helper.toString(
									this->getOutputStream().getReadResult())));
			this->getLogger().writeLog(7,
					this->getLogger().setLogData(
							"<SubutaiThread::optionReadSend> " "Error  Read Result:",
							helper.toString(
									this->getErrorStream().getReadResult())));
			this->getLogger().writeLog(6,
					this->getLogger().setLogData(
							"<SubutaiThread::optionReadSend> " "Breaking!!"));
			break;
		}
	}

	//Check Execution is done by Timeout or another Reason(Normally finished)
	if (EXECTIMEOUT == true) {
		/*
		 * Timeout Response is sending..
		 */
		this->lastCheckAndSend(messageQueue, command);
		this->getLogger().writeLog(7,
				this->getLogger().setLogData(
						"<SubutaiThread::optionReadSend> " "Timeout Done message is sending.."));

		string message;
		if (!command->getIsDaemon()) {
			message = this->getResponse().createTimeoutMessage(
					command->getUuid(), this->getPpid(),
					command->getRequestSequenceNumber(),
					this->getResponsecount(), "", "", command->getCommandId());
		} else {
			// EXECUTE_RESPONSE
			if (command->getType() == "PS_REQUEST") {
				message = this->getResponse().createPsResponse(
						command->getUuid(), command->getCommandId());
			} else {
				message = this->getResponse().createExitMessage(
						command->getUuid(), this->getPpid(),
						command->getRequestSequenceNumber(),
						this->getResponsecount(), command->getCommandId(), 0);
			}
		}
		while (!messageQueue->try_send(message.data(), message.size(), 0))
			;
		this->getLogger().writeLog(7,
				this->getLogger().setLogData(
						"<SubutaiThread::optionReadSend> " "Process Last Message",
						message));

		if (_container) {
			if (!command->getIsDaemon() && (0 == kill(*sub_pid, 0))) {
				this->getLogger().writeLog(7,
						logger.setLogData("<SubutaiThread::optionReadSend> ",
								"--------- *** Process will be killed on container .",
								"pid:", helper.toString(*sub_pid)));
				if (*sub_pid > 1000) {
					this->getLogger().writeLog(7,
							logger.setLogData(
									"<SubutaiThread::optionReadSend> ",
									"Killing process with id: ",
									helper.toString(*sub_pid),
									" running in container."));
					kill(*sub_pid, SIGKILL);
				} else
					this->getLogger().writeLog(7,
							logger.setLogData(
									"<SubutaiThread::optionReadSend> ",
									"Agent will not kill process with id: ",
									helper.toString(*sub_pid),
									" which seems to belong to a command running in container. But it may belong to system since the pid < 1000. "));

			}
		} else if (this->getPpid()) {
			this->getLogger().writeLog(7,
					logger.setLogData(
							"<SubutaiThread::optionReadSend> " "Process will be killed.",
							"pid:", helper.toString(this->getPpid())));
			kill(this->getPpid(), SIGKILL); //killing the process after timeout
		} else {
			this->getLogger().writeLog(6,
					this->getLogger().setLogData(
							"<SubutaiThread::optionReadSend> " "Process pid is not valid.",
							"pid:", helper.toString(this->getPpid())));
		}
	}

	if (this->getErrorStream().getReadResult() == 0
			&& this->getOutputStream().getReadResult() == 0) {
		/*
		 * Execute Done Response is sending..
		 */
		int val;
		close(ret[1]);
		size_t ret_val = read(ret[0], &val, sizeof(val));
		close(ret[0]);

		if (static_cast<int>(ret_val) == -1)
			this->getLogger().writeLog(6,
					this->getLogger().setLogData(
							"<SubutaiThread::optionReadSend> " "Cannot read output.."));

		if (this->getCWDERR() == true || this->getUIDERR() == true)
			val = 1;

		this->lastCheckAndSend(messageQueue, command);

		this->getLogger().writeLog(6,
				this->getLogger().setLogData(
						"<SubutaiThread::optionReadSend> " "Done message is sending.."));
		string message;
		if (command->getType() == "PS_REQUEST") {
			message = this->getResponse().createPsResponse(command->getUuid(),
					command->getCommandId());
		} else {
			message = this->getResponse().createExitMessage(command->getUuid(),
					this->getPpid(), command->getRequestSequenceNumber(),
					this->getResponsecount(), command->getCommandId(), val);
		}
		while (!messageQueue->try_send(message.data(), message.size(), 0))
			;
		this->getLogger().writeLog(6,
				this->getLogger().setLogData(
						"<SubutaiThread::optionReadSend> " "Process Last Message",
						message));
	}

	this->getLogger().writeLog(6,
			this->getLogger().setLogData(
					"<SubutaiThread::optionReadSend> " "Capturing is Done!!"));
	return true;
}

/**
 *  \details   This method is the main method that forking a new process.
 *  		   It execute the command.
 *  		   It also uses Output and Error Streams for capturing the execution responses.
 *  		   if the execution successfully done, it returns true.
 *  		   Otherwise it returns false.
 */
int SubutaiThread::threadFunction(message_queue* messageQueue,
		SubutaiCommand *command, char* argv[], SubutaiContainer* container) {
	if (container) {
		_container = container; // Keep container for future use
	} else {
		_container = NULL;
	}
	pid = fork();		// creating a child process
	if (pid == 0)		// child process is starting
			{
		int argv0size = strlen(argv[0]);
		strncpy(argv[0], "subutai-agent-child", argv0size);
		logger.openLogFile(getpid(), command->getRequestSequenceNumber());
		string pidparnumstr = helper.toString(getpid()); //geting pid number of the process
		string processpid = "";	                      //processpid for execution
		logger.writeLog(6,
				logger.setLogData(
						"<SubutaiThread::threadFunction> " "New Main Fork is Starting!!",
						helper.toString(getpid())));
		this->getOutputStream().setMode(command->getStandardOutput());
		this->getOutputStream().setIdentity("output");
		this->getErrorStream().setMode(command->getStandardError());
		this->getErrorStream().setIdentity("error");

		if (this->getOutputStream().openPipe() == false
				|| this->getErrorStream().openPipe() == false) {
			/* an error occurred pipe of pipeerror or output */
			logger.writeLog(6,
					logger.setLogData(
							"<SubutaiThread::threadFunction> " "Error opening pipes!!"));
		}

		if (command->getIsDaemon()) {
			logger.writeLog(6,
					logger.setLogData(
							"<SubutaiThread::threadFunction> " "Process will be executed as a Daemon process!!"));
		} else {
			logger.writeLog(6,
					logger.setLogData(
							"<SubutaiThread::threadFunction> " "Process will not be executed as a Daemon process!!"));
		}
		int ret[2];
		int val = 0; //for system return value
		pipe(ret);
		signal(SIGCHLD, SIG_DFL);
		int *sub_pid = (int*) mmap(NULL, sizeof(int), PROT_READ | PROT_WRITE,
				MAP_ANON | MAP_SHARED, -1, 0);
		int newpid = fork();
		if (newpid == 0) {	// Child execute the command
			string pidchldnumstr = helper.toString(getpid());
			logger.writeLog(6,
					logger.setLogData(
							"<SubutaiThread::threadFunction> " "New Child Process is starting for pipes",
							"Parentpid", pidparnumstr, "pid", pidchldnumstr));
			this->getOutputStream().preparePipe();
			this->getErrorStream().preparePipe();
			this->getErrorStream().closePipe(0);
			this->getOutputStream().closePipe(0);
			if (!checkCWD(command, container)) {
				logger.writeLog(7,
						logger.setLogData(
								"<SubutaiThread::threadFunction> " "CWD id not found on system..",
								"CWD:", command->getWorkingDirectory()));
				kill(getpid(), SIGKILL);		//killing child
				exit(1);
			}
			if (!checkUID(command, container)) {
				logger.writeLog(6,
						logger.setLogData(
								"<SubutaiThread::threadFunction> " "User id not found on system..",
								"RunAs:", command->getRunAs()));
				kill(getpid(), SIGKILL);		//killing child
				exit(1);
			}

			if (!container) {
				// Execute command for host
				logger.writeLog(6,
						logger.setLogData(
								"<SubutaiThread::threadFunction> " "Execution is starting!!",
								"pid", pidchldnumstr));
				string execCmd = createExecString(command);
				if (command->getIsDaemon()) {
					execCmd.append(" &");
				}
				val = system(execCmd.c_str());
			} else {
				// Execute command for container
				logger.writeLog(6,
						logger.setLogData(
								"<SubutaiThread::threadFunction> " "Execution is starting!! on a container",
								"pid", pidchldnumstr));
				try {
					if (command->getIsDaemon()) {
						container->RunDaemon(command);
					} else {
						container->RunCommandAsDaemon(command, sub_pid);
						this->getLogger().writeLog(6,
								this->getLogger().setLogData(
										"<SubutaiThread::optionReadSend> "
												"********* Pid after command start",
										"current pid:",
										helper.toString(*sub_pid)));

					}
				} catch (SubutaiException e) {
					logger.writeLog(6,
							logger.setLogData(
									"<SubutaiThread::threadFunction> " "Exception",
									e.displayText()));
				} catch (std::exception e) {
					logger.writeLog(6,
							logger.setLogData(
									"<SubutaiThread::threadFunction> " "Exception",
									string(e.what())));
				}
			}
			close(ret[0]);
			write(ret[1], &val, sizeof(val));
			close(ret[1]);
			logger.writeLog(6,
					logger.setLogData(
							"<SubutaiThread::threadFunction> " "Execution is done!!",
							"pid", pidchldnumstr));
			exit(EXIT_SUCCESS);
		} else if (newpid == -1) {
			cout << "ERROR!!" << endl;
			return false;
		} else {
			//Parent read the result and send back
			try {
				signal(SIGCHLD, SIG_IGN);
				this->getErrorStream().closePipe(1);
				this->getOutputStream().closePipe(1);
				logger.writeLog(6,
						logger.setLogData(
								"<SubutaiThread::threadFunction> " "optionReadSend is starting!!",
								"pid", helper.toString(getpid())));
				optionReadSend(messageQueue, command, newpid, ret, sub_pid);
				logger.writeLog(6,
						logger.setLogData(
								"<SubutaiThread::threadFunction> " "optionReadSend has finished!!",
								"pid", helper.toString(getpid())));
				this->getErrorStream().closePipe(0);
				this->getOutputStream().closePipe(0);
				logger.writeLog(6,
						logger.setLogData(
								"<SubutaiThread::threadFunction> " "New Main Thread is Stopping!!",
								"pid", helper.toString(getpid())));
				logger.closeLogFile();
				kill(getpid(), SIGKILL);		//killing child
				return true;                //thread successfully done its work.
			} catch (const std::exception& error) {
				logger.writeLog(3,
						logger.setLogData(
								"<SubutaiThread::threadFunction> " "Problem TF:",
								error.what()));
				cout << error.what() << endl;
			}
		}
	} else if (pid == -1) {
		return pid;
	} else if (pid > 0)	//parent continue its process and return back
			{
		return pid;	//parent successfully done
	}
	return pid;
}

/**
 *  \details   getting "logger" private variable of SubutaiThread instance.
 */
SubutaiLogger& SubutaiThread::getLogger() {
	return this->logger;
}

/**
 *  \details   setting "logger" private variable of SubutaiThread instance.
 */
void SubutaiThread::setLogger(SubutaiLogger mylogger) {
	this->logger = mylogger;
}

/**
 *  \details   getting "uid" private variable of SubutaiThread instance.
 */
SubutaiUserID& SubutaiThread::getUserID() {
	return this->uid;
}

/**
 *  \details   getting "response" SubutaiResponsepack private variable of SubutaiThread instance.
 */
SubutaiResponsePack& SubutaiThread::getResponse() {
	return this->response;
}

/**
 *  \details   getting "errorStream" SubutaiStreamReader private variable of SubutaiThread instance.
 */
SubutaiStreamReader& SubutaiThread::getErrorStream() {
	return this->errorStream;
}

/**
 *  \details   getting "outputStream" SubutaiStreamReader private variable of SubutaiThread instance.
 */
SubutaiStreamReader& SubutaiThread::getOutputStream() {
	return this->outputStream;
}

/**
 *  \details   getting "CWDERR" private variable of SubutaiThread instance.
 *  		   it shows the current working directory existence. false:CWD does not exist. true:it exist.
 */
bool& SubutaiThread::getCWDERR() {
	return this->CWDERR;
}

/**
 *  \details   setting "CWDERR" private variable of SubutaiThread instance.
 */
void SubutaiThread::setCWDERR(bool cwderr) {
	this->CWDERR = cwderr;
}

/**
 *  \details   getting "UIDERR" private variable of SubutaiThread instance.
 *  		   it shows the current user existence. false:UID does not exist. true:it exist.
 */
bool& SubutaiThread::getUIDERR() {
	return this->UIDERR;
}

/**
 *  \details   setting "UIDERR" private variable of SubutaiThread instance.
 */
void SubutaiThread::setUIDERR(bool uiderr) {
	this->UIDERR = uiderr;
}

/**
 *  \details   getting "EXITSTATUS" private variable of SubutaiThread instance.
 */
int& SubutaiThread::getEXITSTATUS() {
	return this->EXITSTATUS;
}

/**
 *  \details   setting "EXITSTATUS" private variable of SubutaiThread instance.
 *  		   it shows the error state of the execution if it is bigger than "0" there is some error message in the execution if it is "0" false.
 *  		   it means successfull execution is done.
 */
void SubutaiThread::setEXITSTATUS(int exitstatus) {
	this->EXITSTATUS = exitstatus;
}

/**
 *  \details   getting "ACTFLAG" private variable of SubutaiThread instance.
 *  		   it shows the activity on the process within default 30 seconds. false:No activity. true:There is at least one activity.
 */
bool& SubutaiThread::getACTFLAG() {
	return this->ACTFLAG;
}

/**
 *  \details   setting "ACTFLAG" private variable of SubutaiThread instance.
 */
void SubutaiThread::setACTFLAG(bool actflag) {
	this->ACTFLAG = actflag;
}

/**
 *  \details   getting "responsecount" private variable of SubutaiThread instance.
 *  		   it indicates the number of sending resposne.
 */
int& SubutaiThread::getResponsecount() {
	return this->responsecount;
}

/**
 *  \details   setting "responsecount" private variable of SubutaiThread instance.
 */
void SubutaiThread::setResponsecount(int rspcount) {
	this->responsecount = rspcount;
}

/**
 *  \details   getting "processpid" private variable of SubutaiThread instance.
 *  		   it indicates the process id of the execution.
 */
int& SubutaiThread::getPpid() {
	return this->processpid;
}

/**
 *  \details   setting "processpid" private variable of SubutaiThread instance.
 */
void SubutaiThread::setPpid(int Ppid) {
	this->processpid = Ppid;
}

/**
 *  \details   getting "outBuff" private variable of SubutaiThread instance.
 *  		   it stores output value to be send.
 */
string& SubutaiThread::getoutBuff() {
	return this->outBuff;
}

/**
 *  \details   setting "outBuff" private variable of SubutaiThread instance.
 */
void SubutaiThread::setoutBuff(string outbuff) {
	this->outBuff = outbuff;
}

/**
 *  \details   getting "errBuff" private variable of SubutaiThread instance.
 *  		   it stores error value to be send.
 */
string& SubutaiThread::geterrBuff() {
	return this->errBuff;
}

/**
 *  \details   setting "errBuff" private variable of SubutaiThread instance.
 */
void SubutaiThread::seterrBuff(string errbuff) {
	this->errBuff = errbuff;
}

/**
 *  \details   This method executes the given command and returns its answer.
 *  		   This is used for getting pid of the execution.
 */
string SubutaiThread::getProcessPid(const char* cmd) {
	FILE* pipe = popen(cmd, "r");
	if (!pipe) {
		return "ERROR";
	}
	char buffer[128];
	string result = "";
	while (!feof(pipe)) {
		if (fgets(buffer, 128, pipe) != NULL) {
			buffer[strlen(buffer) - 1] = '\0';
			result = buffer;
		}
	}
	pclose(pipe);
	return result;
}

