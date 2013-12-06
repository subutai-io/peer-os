/**   @copyright 2013 Safehaus.org
 *
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
 */
/**
 *  @brief     KALogger.h
 *  @class     KALogger.h
 *  @details   KALogger class is designed for log facilities.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.0.0
 *  @date      Sep 4, 2013
 */

#ifndef KALOGGER_H_
#define KALOGGER_H_
#include<stdio.h>
#include<string>
#include<boost/thread.hpp>
using namespace std;
class KALogger {
public:
	KALogger();
	virtual ~KALogger();
	string getLocaltime();
	string toString(int);
	void writeLog(int,string);
	bool openLogFile(int,int);
	bool openLogFileWithName(string);
	void closeLogFile();
	string setLogData(string,string="",string="",string="",string="");
	int getLogLevel();
	void setLogLevel(int loglevel);
private:
	FILE* logFile;
	int loglevel;
};

#endif /* KALOGGER_H_ */
