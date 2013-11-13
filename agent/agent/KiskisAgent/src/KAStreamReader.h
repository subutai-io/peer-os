/*
 *============================================================================
 Name        : KAStreamReader.h
 Author      : Emin INAL
 Date		 : Oct 30, 2013
 Version     : 1.0
 Copyright   : Your copyright notice
 Description : KAStreamReader Class will be used for capturing intermediate responses
==============================================================================
 */
#ifndef KASTREAMREADER_H_
#define KASTREAMREADER_H_
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

using namespace std;
class KAStreamReader
{
public:
	KAStreamReader();
	KAStreamReader(string,string,string);
	~KAStreamReader();
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
	void PreparePipe();
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
	fd_set	fileDec;		//filedec
	int mypipe[2];				//pipe for reading
	int selectResult;		//select result
	int readResult;			//read result
	char buffer[1000];		//Reading buffer
	struct timeval timeout;
	FILE* file;
};
#endif /* KASTREAMREADER_H_ */
