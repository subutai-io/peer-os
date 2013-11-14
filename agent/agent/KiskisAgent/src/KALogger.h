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
	void writeLog(int*,int,string);
	void openLogFile(int,int);
	void closeLogFile();
	string setLogData(string,string="",string="",string="",string="");
private:
	FILE* logFile;
};

#endif /* KALOGGER_H_ */
