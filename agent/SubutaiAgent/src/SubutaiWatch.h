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
/**
 *  @brief     SubutaiWatch.h
 *  @class     SubutaiWatch.h
 *  @details   SubutaiWatch Class is designed for monitoring file system folders and files.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.1.0
 *  @date      Sep 13, 2014
 */
#ifndef KAWATCH_H_
#define KAWATCH_H_
#include <stdio.h>
#include <signal.h>
#include <limits.h>
#include <sys/inotify.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <iostream>
#include <string>
#include <map>
#include <string.h>
#include <cstring>
#include <list>
#include <unistd.h>
#include <time.h>
#include <boost/date_time/posix_time/posix_time_types.hpp>
#include <string>
#include "SubutaiConnection.h"
#include "SubutaiContainerManager.h"
#include "SubutaiHelper.h"
#include "SubutaiResponsePack.h"
#include "SubutaiLogger.h"

using namespace std;
using std::map;
using std::string;
using std::cout;
using std::endl;

#define EVENT_SIZE          ( sizeof (struct inotify_event) )
#define EVENT_BUF_LEN       ( 1024 * ( EVENT_SIZE + NAME_MAX + 1) )
#define WATCH_FLAGS         ( IN_CREATE | IN_DELETE | IN_MODIFY | IN_ATTRIB)

class SubutaiWatch {
public:
	SubutaiWatch(SubutaiConnection*, SubutaiResponsePack*, SubutaiLogger*);
	virtual ~SubutaiWatch(void);
	void initialize(unsigned int);bool addWatcher(const string &);bool eraseWatcher(
			const string &);
	string get(int);
	int get(int, string);
	void cleanup();
	void stats();
	void startSelection();
	void startReading();
	void setSelectResult(int);
	int getSelectResult();
	void setReadResult(int);
	int getReadResult();
	string getCurrentDirectory();
	string getNewDirectory();
	void setNewDirectory(string);
	void setCurrentDirectory(string);
	void clearBuffer();
	char* getBuffer();bool checkNotification(SubutaiContainerManager*);
	string getModificationTime(string, bool);
private:
	bool folderExistenceChecker(const string &);bool checkDuplicateName(
			const string &);
	struct wd_elem {
		int pd;
		string name;bool operator()(const wd_elem &l, const wd_elem &r) const {
			return l.pd < r.pd ? true :
					l.pd == r.pd && l.name < r.name ? true : false;
		}
	};
	map<int, wd_elem> watch;
	map<wd_elem, int, wd_elem> rwatch;
	fd_set watch_set;
	int fd;
	int wd;
	char buffer[ EVENT_BUF_LEN];
	string currentDirectory;
	string newDirectory;
	timeval timeout;
	int selectResult;
	int readResult;
	string sendout;
	SubutaiResponsePack* watchRepsonse;
	SubutaiConnection* watchConnection;
	SubutaiLogger* watchLogger;
	SubutaiHelper helper;

};
#endif /* SUBUTAIWATCH_H_ */
