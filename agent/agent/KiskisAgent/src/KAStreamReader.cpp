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
#include "KAStreamReader.h"
KAStreamReader::KAStreamReader()
{
	setIdentity("");
	setMode("");
	setPath("");
	//		setFileDec();
	//		setPipe();
	setSelectResult(1);
	setReadResult(1);
	clearBuffer();
}
KAStreamReader::KAStreamReader(string mode,string path,string identity)
{
	setMode(mode);
	setPath(path);
	setIdentity(identity);
	setSelectResult(1);
	setReadResult(1);
	clearBuffer();
}
KAStreamReader::~KAStreamReader()
{
}
void KAStreamReader::setIdentity(string identity)
{
	this->identity = identity;
}
string& KAStreamReader::getIdentity()
{
	return this->identity;
}
void KAStreamReader::setMode(string mode)
{
	this->mode = mode;
}
string& KAStreamReader::getMode()
{
	return this->mode;
}
void KAStreamReader::setPath(string path)
{
	this->path = path;
}
string& KAStreamReader::getPath()
{
	return this->path;
}
void KAStreamReader::setFileDec(fd_set fileDec)
{
	this->fileDec = fileDec;
}
void KAStreamReader::prepareFileDec()
{
	FD_ZERO(&fileDec);
	FD_SET(mypipe[0],&fileDec);
}
fd_set& KAStreamReader::getFileDec()
{
	return this->fileDec;
}
void KAStreamReader::PreparePipe()
{
	dup2(mypipe[0], STDIN_FILENO);
	if(identity=="output")
		dup2(mypipe[1], STDOUT_FILENO);
	else if (identity=="error")
		dup2(mypipe[1], STDERR_FILENO);
}
int* KAStreamReader::getPipe()
{
	return mypipe;
}
bool KAStreamReader::openPipe()
{
	if(!pipe(mypipe))
		return true;
	else
		return false;
}
void KAStreamReader::closePipe(int i)
{
	close(mypipe[i]);
}
void KAStreamReader::setSelectResult(int selectresult)
{
	this->selectResult=selectresult;
}
int KAStreamReader::getSelectResult()
{
	return this->selectResult;
}
void KAStreamReader::setReadResult(int readresult)
{
	this->readResult=readresult;
}
int KAStreamReader::getReadResult()
{
	return this->readResult;
}
void KAStreamReader::clearBuffer()
{
	memset(buffer,0,1000);
}
char* KAStreamReader::getBuffer()
{
	return buffer;
}
void KAStreamReader::setTimeout(unsigned int second)
{
	this->timeout.tv_sec = second;
	this->timeout.tv_usec = 0;
}
void KAStreamReader::startSelection()
{
	if(timeout.tv_sec==0)
	{
		this->selectResult = select(mypipe[0]+1,&fileDec,NULL,NULL,NULL);
	}
	else if(timeout.tv_sec==1)
	{
		this->timeout.tv_sec=2;
		this->selectResult = select(mypipe[0]+1,&fileDec,NULL,NULL,&timeout);
	}
	else
		this->selectResult = select(mypipe[0]+1,&fileDec,NULL,NULL,&timeout);
}
void KAStreamReader::startReading()
{
	this->readResult=read(mypipe[0] , buffer,sizeof(buffer));
}
bool KAStreamReader::openFile()
{
	this->file=fopen(this->path.c_str(),"a+");

	if(file)
		return true;
	else
		return false;
}
void KAStreamReader::closeFile()
{
	fclose(this->file);
}
void KAStreamReader::appendFile(string value)
{
	fputs(value.c_str(),this->file);
}
