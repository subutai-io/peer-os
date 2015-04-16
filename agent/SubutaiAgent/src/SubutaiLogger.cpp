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
#include "SubutaiLogger.h"

/**
 *  \details   Default constructor of the SubutaiLogger class.
 */
SubutaiLogger::SubutaiLogger() {
	// TODO Auto-generated constructor stub
}

/**
 *  \details   Default destructor of the SubutaiLogger class.
 */
SubutaiLogger::~SubutaiLogger() {
	// TODO Auto-generated destructor stub
}

/**
 *  \details   getting "loglevel" private variable of the SubutaiLogger instance.
 */
int SubutaiLogger::getLogLevel() {
	return this->loglevel;
}

/**
 *  \details   setting "loglevel" private variable of the SubutaiLogger instance.
 *  		   This level indicates that the loglevel status.
 *  		   it should be between (0-7) -> (Emergency-Debug)
 */
void SubutaiLogger::setLogLevel(int loglevel) {
	this->loglevel = loglevel;
}

/**
 *  \details   This method creates local time values as a string.
 *  		   The return value as dd-mm-yy hh:mm::ss
 */
string SubutaiLogger::getLocaltime() {
	boost::posix_time::ptime now =
			boost::posix_time::second_clock::local_time();

	return _helper.toString(now.date().day().as_number()) + "-"
			+ _helper.toString(now.date().month().as_number()) + "-"
			+ _helper.toString(now.date().year()) + " "
			+ _helper.toString(now.time_of_day().hours()) + ":"
			+ _helper.toString(now.time_of_day().minutes()) + ":"
			+ _helper.toString(now.time_of_day().seconds());
}

/**
 *  \details   This method opens a log file.
 *  		   For name production, local time,process ID and Sequence Number are used.
 *  		   return true if file pointer successfully created and assigned.
 *  		   otherwise it returns false.
 */
bool SubutaiLogger::openLogFile(int pid, int requestSequenceNumber) {
	boost::posix_time::ptime now =
			boost::posix_time::second_clock::local_time();
	string logFileName = "/var/log/subutai-agent/"
			+ _helper.toString(now.date().year())
			+ _helper.toString(now.date().month().as_number())
			+ _helper.toString(now.date().day().as_number()) + "-"
			+ _helper.toString(now.time_of_day().total_milliseconds()) + "-"
			+ _helper.toString(pid) + "-"
			+ _helper.toString(requestSequenceNumber);
	this->logFile = fopen(logFileName.c_str(), "a + ");
	if (this->logFile) {
		return true;	//file pointer successfully assigned.
	} else
		return false;
}

/**
 *  \details   This method opens a log file with given name.
 *   		   return true if file pointer successfully created and assigned.
 *  		   otherwise it returns false.
 */
bool SubutaiLogger::openLogFileWithName(string logfilename) {
	logfilename = "/var/log/subutai-agent/" + logfilename;
	this->logFile = fopen(logfilename.c_str(), "a + ");
	if (this->logFile) {
		return true; 	//file pointer successfully assigned.
	} else
		return false;
}

/**
 *  \details   This method closed the log file.
 */
void SubutaiLogger::closeLogFile() {
	fclose(logFile);
}

/**
 *  \details   This method sets the log data.
 */
string SubutaiLogger::setLogData(string text, string param1, string value1,
		string param2, string value2) {
	return text + " " + param1 + " " + value1 + " " + param2 + " " + value2;
}

/**
 *  \details   This method writes the logs to log files according to 8 log level.
 */
void SubutaiLogger::writeLog(int level, string log) {
	switch (this->loglevel) {
	case 7:
		switch (level) {
		case 7:
			log = getLocaltime() + " <DEBUG>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 6:
			log = getLocaltime() + " <INFO>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 5:
			log = getLocaltime() + " <NOTICE>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 4:
			log = getLocaltime() + " <WARNING>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 3:
			log = getLocaltime() + " <ERROR>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 2:
			log = getLocaltime() + " <CRITICAL>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 1:
			log = getLocaltime() + " <ALERT>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 0:
			log = getLocaltime() + " <EMERGENCY>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		}
		break;
	case 6:
		switch (level) {
		case 6:
			log = getLocaltime() + " <INFO>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 5:
			log = getLocaltime() + " <NOTICE>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 4:
			log = getLocaltime() + " <WARNING>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 3:
			log = getLocaltime() + " <ERROR>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 2:
			log = getLocaltime() + " <CRITICAL>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 1:
			log = getLocaltime() + " <ALERT>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 0:
			log = getLocaltime() + " <EMERGENCY>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		}
		break;
	case 5:
		switch (level) {
		case 5:
			log = getLocaltime() + " <NOTICE>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 4:
			log = getLocaltime() + " <WARNING>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 3:
			log = getLocaltime() + " <ERROR>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 2:
			log = getLocaltime() + " <CRITICAL>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 1:
			log = getLocaltime() + " <ALERT>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 0:
			log = getLocaltime() + " <EMERGENCY>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		}
		break;
	case 4:
		switch (level) {
		case 4:
			log = getLocaltime() + " <WARNING>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 3:
			log = getLocaltime() + " <ERROR>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 2:
			log = getLocaltime() + " <CRITICAL>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 1:
			log = getLocaltime() + " <ALERT>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 0:
			log = getLocaltime() + " <EMERGENCY>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		}
		break;
	case 3:
		switch (level) {
		case 3:
			log = getLocaltime() + " <ERROR>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 2:
			log = getLocaltime() + " <CRITICAL>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 1:
			log = getLocaltime() + " <ALERT>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 0:
			log = getLocaltime() + " <EMERGENCY>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		}
		break;
	case 2:
		switch (level) {
		case 2:
			log = getLocaltime() + " <CRITICAL>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 1:
			log = getLocaltime() + " <ALERT>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 0:
			log = getLocaltime() + " <EMERGENCY>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		}
		break;
	case 1:
		switch (level) {
		case 1:
			log = getLocaltime() + " <ALERT>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		case 0:
			log = getLocaltime() + " <EMERGENCY>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		}
		break;
	case 0:
		switch (level) {
		case 0:
			log = getLocaltime() + " <EMERGENCY>" + log + "\n";
			fputs(log.c_str(), this->logFile);
#if _DEBUG
			printf("%s", log.c_str());
#endif
			break;
		}
		break;
	}
	fflush(logFile);
}
