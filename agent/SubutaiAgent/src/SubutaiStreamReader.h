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
 *  @brief     SubutaiStreamReader.h
 *  @class     SubutaiStreamReader.h
 *  @details   SubutaiStreamReader Class is used for capturing intermediate responses.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.1.0
 *  @date      Sep 13, 2014
 */
#ifndef SUBUTAISTREAMREADER_H_
#define SUBUTAISTREAMREADER_H_
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/file.h>
#include <fcntl.h>
#include <errno.h>
#include <syslog.h>
#include <stdarg.h>
#include <stdlib.h>
#include <signal.h>
#include <pwd.h>
#include <iostream>
#include <cstdio>
#include <stdio.h>
#include <unistd.h>
#include <cstdlib>
#include <string.h>
#include <time.h>

using namespace std;
class SubutaiStreamReader {
public:
	SubutaiStreamReader();
	SubutaiStreamReader(string, string, string);
	~SubutaiStreamReader();
	void setIdentity(string);
	string& getIdentity();
	void setMode(string);
	string& getMode();
	void setPath(string);
	string& getPath();
	void setFileDec(fd_set);
	void prepareFileDec();
	fd_set& getFileDec();
	void setExecutionState(bool);
	bool& getExecutionState();
	void preparePipe();
	int* getPipe();
	bool openPipe();
	void closePipe(int);
	void setSelectResult(int);
	int getSelectResult();
	void setReadResult(int);
	int getReadResult();
	void clearBuffer();
	char* getBuffer();
	void startSelection();
	void setTimeout(unsigned int);
	void startReading();
	bool openFile();
	void appendFile(string);
	void closeFile();
private:
	string identity;		//output or error decision
	string mode;			//stdout and stderr mode
	string path;			//stdout and stderr paths
	fd_set fileDec;		//filedec
	int mypipe[2];				//pipe for reading
	int selectResult;		//select result
	int readResult;			//read result
	char buffer[1000];		//Reading buffer
	struct timeval timeout;
	FILE* file;
};
#endif /* SUBUTAISTREAMREADER_H_ */
