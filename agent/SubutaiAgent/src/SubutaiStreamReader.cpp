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
#include "SubutaiStreamReader.h"

/**
 *  \details   Default constructor of the SubutaiStreamReader class.
 */
SubutaiStreamReader::SubutaiStreamReader() {
	setIdentity("");
	setMode("");
	setPath("");
	setSelectResult(1);
	setReadResult(1);
	clearBuffer();
}

/**
 *  \details   Overloaded constructor of the SubutaiStreamReader class.
 */
SubutaiStreamReader::SubutaiStreamReader(string mode, string path,
		string identity) {
	setMode(mode);
	setPath(path);
	setIdentity(identity);
	setSelectResult(1);
	setReadResult(1);
	clearBuffer();
}

/**
 *  \details   Default destructor of the SubutaiStreamReader class.
 */
SubutaiStreamReader::~SubutaiStreamReader() {

}

/**
 *  \details   setting "identity" private variable of SubutaiStreamReader instance.
 *  		   identity should be "error" or "output".
 */
void SubutaiStreamReader::setIdentity(string identity) {
	this->identity = identity;
}

/**
 *  \details   getting "identity" private variable of SubutaiStreamReader instance.
 */
string& SubutaiStreamReader::getIdentity() {
	return this->identity;
}

/**
 *  \details   setting "mode" private variable of SubutaiStreamReader instance.
 *  		   mode has the value of: "CAPTURE" or "CAPTURE_AND_RETURN" or "RETURN" or "NO".
 */
void SubutaiStreamReader::setMode(string mode) {
	this->mode = mode;
}

/**
 *  \details   getting "mode" private variable of SubutaiStreamReader instance
 */
string& SubutaiStreamReader::getMode() {
	return this->mode;
}

/**
 *  \details   getting "path" private variable of SubutaiStreamReader instance.
 *  		   this path is used for capturing intermediate response's location.
 */
void SubutaiStreamReader::setPath(string path) {
	this->path = path;
}

/**
 *  \details   getting "path" private variable of SubutaiStreamReader instance.
 */
string& SubutaiStreamReader::getPath() {
	return this->path;
}

/**
 *  \details   setting "fileDec" private variable of SubutaiStreamReader instance.
 *  		   Each StreamReader instance has a file descriptor to open a pipe.
 */
void SubutaiStreamReader::setFileDec(fd_set fileDec) {
	this->fileDec = fileDec;
}

/**
 *  \details   This method prepares its file descriptor to pipe operation.
 */
void SubutaiStreamReader::prepareFileDec() {
	FD_ZERO(&fileDec);
	FD_SET(mypipe[0], &fileDec);
}

/**
 *  \details   getting "fileDec" private variable of SubutaiStreamReader instance.
 */
fd_set& SubutaiStreamReader::getFileDec() {
	return this->fileDec;
}

/**
 *  \details   This method prepares pipelines using its file descriptor.
 *  		   It duplicates the stderr and stdout pipelines to its private pipes.
 */
void SubutaiStreamReader::preparePipe() {
	dup2(mypipe[0], STDIN_FILENO);
	if (identity == "output") {
		dup2(mypipe[1], STDOUT_FILENO);
	} else if (identity == "error") {
		dup2(mypipe[1], STDERR_FILENO);
	}
}
/**
 *  \details   getting "mypipe" private variable of SubutaiStreamReader instance.
 */
int* SubutaiStreamReader::getPipe() {
	return mypipe;
}

/**
 *  \details   This method opens the pipe.
 *  			Return true if open operation is successfull otherwise it returns false.
 */
bool SubutaiStreamReader::openPipe() {
	if (!pipe(mypipe)) {
		return true;
	} else {
		return false;
	}
}

/**
 *  \details   	This method closes the pipe.
 */
void SubutaiStreamReader::closePipe(int i) {
	close(mypipe[i]);
}

/**
 *  \details   	 setting "selectResult" private variable of SubutaiStreamReader instance.
 *  			 selectResult indicates that the timeout is occured or not.
 */
void SubutaiStreamReader::setSelectResult(int selectresult) {
	this->selectResult = selectresult;
}

/**
 *  \details   	 getting "selectResult" private variable of SubutaiStreamReader instance.
 */
int SubutaiStreamReader::getSelectResult() {
	return this->selectResult;
}

/**
 *  \details   	 setting "readResult" private variable of SubutaiStreamReader instance.
 *  			 readResult indicates that EOF is occured in the pipe or not.
 */
void SubutaiStreamReader::setReadResult(int readresult) {
	this->readResult = readresult;
}

/**
 *  \details   	 getting "readResult" private variable of SubutaiStreamReader instance.
 */
int SubutaiStreamReader::getReadResult() {
	return this->readResult;
}

/**
 *  \details   	 This method clears the Stream buffer.
 */
void SubutaiStreamReader::clearBuffer() {
	memset(buffer, 0, 1000);
}

/**
 *  \details   	 getting "buffer" private variable of SubutaiStreamReader instance.
 */
char* SubutaiStreamReader::getBuffer() {
	return buffer;
}

/**
 *  \details   	 This method set the timeout value of the Stream. it is used by default 20000 for now. (20000 usec = 20ms)
 */
void SubutaiStreamReader::setTimeout(unsigned int usecond) {
	this->timeout.tv_sec = 0;
	this->timeout.tv_usec = usecond;
}

/**
 *  \details   	 This method starts selection and timeout if it is set.
 */
void SubutaiStreamReader::startSelection() {
	this->selectResult = select(mypipe[0] + 1, &fileDec, NULL, NULL, &timeout);
}

/**
 *  \details   	 This method starts reading the buffer contents.
 */
void SubutaiStreamReader::startReading() {
	this->readResult = read(mypipe[0], buffer, sizeof(buffer));
}

/**
 *  \details   	 This method opens the file that is filling with intermediate responses.
 */
bool SubutaiStreamReader::openFile() {
	this->file = fopen(this->path.c_str(), "a+");
	if (file) {
		return true;
	} else {
		return false;
	}
}

/**
 *  \details   	 This method closes the file that was filled with intermediate responses.
 */
void SubutaiStreamReader::closeFile() {
	fclose(this->file);
}

/**
 *  \details   	 This method append a given contents to the file.
 */
void SubutaiStreamReader::appendFile(string value) {
	fputs(value.c_str(), this->file);
}
