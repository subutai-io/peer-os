/**
 *  @brief     KALogger.h
 *  @class     KALogger.h
 *  @details   KALogger class is designed for log facilities.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.0
 *  @date      Sep 4, 2013
 *  @copyright GNU Public License.
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
	void openLogFile(int,int);
	void openLogFileWithName(string);
	void closeLogFile();
	string setLogData(string,string="",string="",string="",string="");
	int getLogLevel();
	void setLogLevel(int loglevel);
private:
	FILE* logFile;
	int loglevel;
};

#endif /* KALOGGER_H_ */
