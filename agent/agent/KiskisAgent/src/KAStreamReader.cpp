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
#include "KAStreamReader.h"
/**
 *  \details   Default constructor of the KAStreamReader class.
 */
KAStreamReader::KAStreamReader()
{
	setIdentity("");
	setMode("");
	setPath("");
	setSelectResult(1);
	setReadResult(1);
	clearBuffer();
}
/**
 *  \details   Overloaded constructor of the KAStreamReader class.
 */
KAStreamReader::KAStreamReader(string mode,string path,string identity)
{
	setMode(mode);
	setPath(path);
	setIdentity(identity);
	setSelectResult(1);
	setReadResult(1);
	clearBuffer();
}
/**
 *  \details   Default destructor of the KAStreamReader class.
 */
KAStreamReader::~KAStreamReader()
{

}
/**
 *  \details   setting "identity" private variable of KAStreamReader instance.
 *  		   identity should be "error" or "output".
 */
void KAStreamReader::setIdentity(string identity)
{
	this->identity = identity;
}
/**
 *  \details   getting "identity" private variable of KAStreamReader instance.
 */
string& KAStreamReader::getIdentity()
{
	return this->identity;
}
/**
 *  \details   setting "mode" private variable of KAStreamReader instance.
 *  		   mode has the value of: "CAPTURE" or "CAPTURE_AND_RETURN" or "RETURN" or "NO".
 */
void KAStreamReader::setMode(string mode)
{
	this->mode = mode;
}
/**
 *  \details   getting "mode" private variable of KAStreamReader instance
 */
string& KAStreamReader::getMode()
{
	return this->mode;
}
/**
 *  \details   getting "path" private variable of KAStreamReader instance.
 *  		   this path is used for capturing intermediate response's location.
 */
void KAStreamReader::setPath(string path)
{
	this->path = path;
}
/**
 *  \details   getting "path" private variable of KAStreamReader instance.
 */
string& KAStreamReader::getPath()
{
	return this->path;
}
/**
 *  \details   setting "fileDec" private variable of KAStreamReader instance.
 *  		   Each StreamReader instance has a file descriptor to open a pipe.
 */
void KAStreamReader::setFileDec(fd_set fileDec)
{
	this->fileDec = fileDec;
}
/**
 *  \details   This method prepares its file descriptor to pipe operation.
 */
void KAStreamReader::prepareFileDec()
{
	FD_ZERO(&fileDec);
	FD_SET(mypipe[0],&fileDec);
}
/**
 *  \details   getting "fileDec" private variable of KAStreamReader instance.
 */
fd_set& KAStreamReader::getFileDec()
{
	return this->fileDec;
}
/**
 *  \details   This method prepares pipelines using its file descriptor.
 *  		   It duplicates the stderr and stdout pipelines to its private pipes.
 */
void KAStreamReader::PreparePipe()
{
	dup2(mypipe[0], STDIN_FILENO);
	if(identity == "output")
	{
		dup2(mypipe[1], STDOUT_FILENO);
	}
	else if (identity == "error")
	{
		dup2(mypipe[1], STDERR_FILENO);
	}
}
/**
 *  \details   getting "mypipe" private variable of KAStreamReader instance.
 */
int* KAStreamReader::getPipe()
{
	return mypipe;
}
/**
 *  \details   This method opens the pipe.
 *  			Return true if open operation is successfull otherwise it returns false.
 */
bool KAStreamReader::openPipe()
{
	if(!pipe(mypipe))
	{
		return true;
	}
	else
	{
		return false;
	}
}
/**
 *  \details   	This method closes the pipe.
 */
void KAStreamReader::closePipe(int i)
{
	close(mypipe[i]);
}
/**
 *  \details   	 setting "selectResult" private variable of KAStreamReader instance.
 *  			 selectResult indicates that the timeout is occured or not.
 */
void KAStreamReader::setSelectResult(int selectresult)
{
	this->selectResult = selectresult;
}
/**
 *  \details   	 getting "selectResult" private variable of KAStreamReader instance.
 */
int KAStreamReader::getSelectResult()
{
	return this->selectResult;
}
/**
 *  \details   	 setting "readResult" private variable of KAStreamReader instance.
 *  			 readResult indicates that EOF is occured in the pipe or not.
 */
void KAStreamReader::setReadResult(int readresult)
{
	this->readResult = readresult;
}
/**
 *  \details   	 getting "readResult" private variable of KAStreamReader instance.
 */
int KAStreamReader::getReadResult()
{
	return this->readResult;
}
/**
 *  \details   	 This method clears the Stream buffer.
 */
void KAStreamReader::clearBuffer()
{
	memset(buffer,0,1000);
}
/**
 *  \details   	 getting "buffer" private variable of KAStreamReader instance.
 */
char* KAStreamReader::getBuffer()
{
	return buffer;
}
/**
 *  \details   	 This method set the timeout value of the Stream. it is used by default 20000 for now. (20000 usec = 20ms)
 */
void KAStreamReader::setTimeout(unsigned int usecond)
{
	this->timeout.tv_sec = 0;
	this->timeout.tv_usec = usecond;
}
/**
 *  \details   	 This method starts selection and timeout if it is set.
 */
void KAStreamReader::startSelection()
{
	this->selectResult = select(mypipe[0]+1,&fileDec,NULL,NULL,&timeout);
}
/**
 *  \details   	 This method starts reading the buffer contents.
 */
void KAStreamReader::startReading()
{
	this->readResult = read(mypipe[0] , buffer,sizeof(buffer));
}
/**
 *  \details   	 This method opens the file that is filling with intermediate responses.
 */
bool KAStreamReader::openFile()
{
	this->file = fopen(this->path.c_str(),"a+");

	if(file)
	{
		return true;
	}
	else
	{
		return false;
	}
}
/**
 *  \details   	 This method closes the file that was filled with intermediate responses.
 */
void KAStreamReader::closeFile()
{
	fclose(this->file);
}
/**
 *  \details   	 This method append a given contents to the file.
 */
void KAStreamReader::appendFile(string value)
{
	fputs(value.c_str(),this->file);
}
