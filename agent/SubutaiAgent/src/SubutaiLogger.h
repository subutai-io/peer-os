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
 *  @brief     SubutaiLogger.h
 *  @class     SubutaiLogger.h
 *  @details   SubutaiLogger class is designed for log facilities.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.1.0
 *  @date      Sep 13, 2014
 */

#ifndef SUBUTAILOGGER_H_
#define SUBUTAILOGGER_H_
#include <stdio.h>
#include <string>
#include "SubutaiHelper.h"
#include <boost/date_time/posix_time/posix_time_types.hpp>
using namespace std;
class SubutaiLogger {
public:
	SubutaiLogger();
	virtual ~SubutaiLogger();
	string getLocaltime();
	void writeLog(int, string);
	bool openLogFile(int, int);
	bool openLogFileWithName(string);
	void closeLogFile();
	string setLogData(string, string = "", string = "", string = "",
			string = "");
	int getLogLevel();
	void setLogLevel(int loglevel);
private:
	SubutaiHelper _helper;
	FILE* logFile;
	int loglevel;
};

#endif /* SUBUTAILOGGER_H_ */
