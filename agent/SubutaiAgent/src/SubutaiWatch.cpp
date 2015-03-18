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
#include "SubutaiWatch.h"
/**
 *  \details   Default constructor of SubutaiWatch class.
 */
SubutaiWatch::SubutaiWatch(SubutaiConnection* connection,
		SubutaiResponsePack* response, SubutaiLogger* logger) {
#ifdef IN_NONBLOCK
	fd = inotify_init1( IN_NONBLOCK);
#else
	fd = inotify_init();
#endif

	if (fd < 0) {
		cout << "inotify_init" << endl;
	}
	this->watchConnection = connection;
	this->watchRepsonse = response;
	this->watchLogger = logger;
}

/**
 *  \details   Default destructor of SubutaiWatch class.
 */
SubutaiWatch::~SubutaiWatch() {
}

/**
 *  \details   This method Insert event information, used to create new watch, into Watch object.
 */
bool SubutaiWatch::addWatcher(const string &name) {
	if (folderExistenceChecker(name) && !checkDuplicateName(name)) {
		cout << name << " found on system" << endl;

		wd = inotify_add_watch(fd, name.c_str(), WATCH_FLAGS);
		wd_elem elem = { -1, name };
		watch[wd] = elem;
		rwatch[elem] = wd;
		return true;
	} else {
		if (!folderExistenceChecker(name))
			cout << name << " does not exist on system" << endl;
		else
			cout << name << " already on the list.." << endl;

		return false;
	}
}

/**
 *  \details   This method checks given pattern is a valid file or folder
 */
bool SubutaiWatch::folderExistenceChecker(const string &name) {
	struct stat sb;

	if (stat(name.c_str(), &sb) == 0 && S_ISDIR(sb.st_mode)) {
		return true;
	} else {
		return false;
	}
}

/**
 *  \details   This method checks the given folder is already added to list or not
 */
bool SubutaiWatch::checkDuplicateName(const string &name) {
	typedef map<int, wd_elem>::const_iterator MapIterator;
	for (MapIterator iter = watch.begin(); iter != watch.end(); iter++) {
		if ((iter->second.name) == name)
			return true;
	}
	return false;
}

/**
 *  \details   This methods Initialize watchset with given timeout value.
 */
void SubutaiWatch::initialize(unsigned int timevalue) {
	FD_ZERO(&this->watch_set);
	FD_SET(this->fd, &this->watch_set);

	timeout.tv_sec = 0;
	timeout.tv_usec = timevalue;
}

/**
 *  \details    This method is a Erase wrapper.
 */
bool SubutaiWatch::eraseWatcher(const string &name) {
	bool foundOnWatchlist = false;

	typedef map<int, wd_elem>::const_iterator MapIterator;
	for (MapIterator iter = watch.begin(); iter != watch.end(); iter++) {
		if ((iter->second.name) == name) {
			foundOnWatchlist = true;
			break;
		}
	}

	if (folderExistenceChecker(name) && foundOnWatchlist) {
		for (map<int, wd_elem>::iterator wi = watch.begin(); wi != watch.end();
				wi++) {
			if (wi->second.name == name) {
				inotify_rm_watch(fd, wi->first);
				watch.erase(wi);
				wd_elem pelem = { -1, name };
				rwatch.erase(pelem);
			}
		}
		return true;
	} else
		return false;
}

/**
 *  \details 	This method Given a watch descriptor, return the full directory name as string.
 */
string SubutaiWatch::get(int wd) {
	const wd_elem &elem = watch[wd];
	return elem.pd == -1 ? elem.name : this->get(elem.pd) + "/" + elem.name;
}

/**
 *  \details   	Given a parent wd and name (provided in IN_DELETE events), return the watch descriptor.
 *			    Main purpose is to help remove directories from watch list.
 */
int SubutaiWatch::get(int pd, string name) {
	wd_elem elem = { pd, name };
	return rwatch[elem];
}

/**
 *  \details   This method cleans up the all descriptors.
 *
 */
void SubutaiWatch::cleanup() {
	for (map<int, wd_elem>::iterator wi = watch.begin(); wi != watch.end();
			wi++) {
		inotify_rm_watch(this->fd, wi->first);
		watch.erase(wi);
	}
	rwatch.clear();
}

/**
 *  \details   This method shows the status of the watchers.
 */
void SubutaiWatch::stats() {
	watchLogger->writeLog(7,
			watchLogger->setLogData("<SubutaiWatch::stats>",
					"Watcher Stats logging.."));
	vector<string> myvector;
	cout << "number of watches=" << watch.size() << " & reverse watches="
			<< rwatch.size() << endl;
	cout << "*************" << endl;
	cout << "WatchList" << endl;
	typedef map<int, wd_elem>::const_iterator MapIterator;
	for (MapIterator iter = watch.begin(); iter != watch.end(); iter++) {
		cout << "First Item: " << iter->first << "    Second Item pd: "
				<< iter->second.pd << " Second Item name: " << iter->second.name
				<< endl;
		myvector.push_back(iter->second.name);
		watchLogger->writeLog(7,
				watchLogger->setLogData("<SubutaiWatch::stats>", "Watcher: ",
						iter->second.name));
	}
	watchRepsonse->setConfPoints(myvector);

	cout << "*************" << endl;
	cout << "ReverseWatchList" << endl;
	typedef map<wd_elem, int, wd_elem>::const_iterator MapIterator1;
	for (MapIterator1 iter1 = rwatch.begin(); iter1 != rwatch.end(); iter1++) {
		cout << "First Item pd: " << iter1->first.pd << " First Item name: "
				<< iter1->first.name << "   Second Item: " << iter1->second
				<< endl;
	}

}

/**
 *  \details   	 This method starts selection and timeout if it is set.
 */
void SubutaiWatch::startSelection() {
	this->selectResult = select(fd + 1, &watch_set, NULL, NULL, &timeout);
}

/**
 *  \details   	 This method starts reading the buffer contents.
 */
void SubutaiWatch::startReading() {
	this->readResult = read(fd, buffer, EVENT_BUF_LEN);
}

/**
 *  \details   	 setting "selectResult" private variable of SubutaiWatch instance.
 *  			 selectResult indicates that the timeout is occured or not.
 */
void SubutaiWatch::setSelectResult(int selectresult) {
	this->selectResult = selectresult;
}

/**
 *  \details   	 getting "selectResult" private variable of SubutaiWatch instance.
 */
int SubutaiWatch::getSelectResult() {
	return this->selectResult;
}

/**
 *  \details   	 setting "readResult" private variable of SubutaiWatch instance.
 *  			 readResult indicates that EOF is occured in the pipe or not.
 */
void SubutaiWatch::setReadResult(int readresult) {
	this->readResult = readresult;
}

/**
 *  \details   	 getting "readResult" private variable of SubutaiWatch instance.
 */
int SubutaiWatch::getReadResult() {
	return this->readResult;
}

/**
 *  \details   	 getting "currentDirectory" private variable of SubutaiWatch instance.
 */
string SubutaiWatch::getCurrentDirectory() {
	return this->currentDirectory;
}

/**
 *  \details   	 setting "currentDirectory" private variable of SubutaiWatch instance.
 */
void SubutaiWatch::setCurrentDirectory(string currDir) {
	this->currentDirectory = currDir;
}

/**
 *  \details   	 getting "newDirectory" private variable of SubutaiWatch instance.
 */
string SubutaiWatch::getNewDirectory() {
	return this->newDirectory;
}

/**
 *  \details   	 setting "currentDirectory" private variable of SubutaiWatch instance.
 */
void SubutaiWatch::setNewDirectory(string newDir) {
	this->newDirectory = newDir;
}

/**
 *  \details   	 This method clears the Watchers Event buffer.
 */
void SubutaiWatch::clearBuffer() {
	memset(buffer, 0, EVENT_BUF_LEN);
}

/**
 *  \details   	 getting "buffer" private variable of SubutaiWatch instance.
 */
char* SubutaiWatch::getBuffer() {
	return buffer;
}

/**
 *  \details   	 This method checks the notification for file system watchers
 *  			 This method also understands the type of events and changes(Create/Delete/Modify) of files
 */
bool SubutaiWatch::checkNotification(SubutaiContainerManager* cman) {
	bool status = false;
	int length = 0;
	initialize(20000);
	startSelection();

	try {
		if (getSelectResult()) {
			setSelectResult(0);
			// Read event(s) from non-blocking inotify fd (non-blocking specified in inotify_init1 above).
			startReading();
			length = getReadResult();
		}
		if (length < 0) {
			watchLogger->writeLog(3,
					watchLogger->setLogData("<SubutaiWatch::checkNotification>",
							"Length is under zero"));
			status = false;
			return status;
		}
		for (int i = 0; i < length; i++) {
			struct inotify_event *event =
					(struct inotify_event *) &getBuffer()[i];

			if (event->wd == -1) {
				watchLogger->writeLog(3,
						watchLogger->setLogData(
								"<SubutaiWatch::checkNotification>",
								"Overflow!!"));
				status = false;
				return status;
			}

			if (event->mask & IN_Q_OVERFLOW) {
				watchLogger->writeLog(3,
						watchLogger->setLogData(
								"<SubutaiWatch::checkNotification>",
								"Overflow!!"));
				status = false;
				return status;
			}

			if (event->len) {
				watchRepsonse->setContainerSet(cman->getAllContainers());
				if (event->mask & IN_IGNORED) {
					watchLogger->writeLog(3,
							watchLogger->setLogData(
									"<SubutaiWatch::checkNotification>",
									"IN_IGNORED!!"));
				}
				if (event->mask & IN_CREATE) {
					setCurrentDirectory(get(event->wd));
					watchLogger->writeLog(7,
							watchLogger->setLogData(
									"<SubutaiWatch::checkNotification>",
									"Event Directory: ",
									getCurrentDirectory()));
					if (event->mask & IN_ISDIR) //folder events
					{
						status = true;
						setNewDirectory(
								getCurrentDirectory() + "/" + event->name);
						sendout = watchRepsonse->createInotifyMessage(
								watchConnection->getID(), getNewDirectory(),
								getModificationTime(getNewDirectory(), false),
								"Create_Folder");
						watchConnection->sendMessage(sendout, "INOTIFY_TOPIC");
						watchLogger->writeLog(7,
								watchLogger->setLogData(
										"<SubutaiWatch::checkNotification>",
										"Sending Event Response: ", sendout));
					} else //file events
					{
						status = true;
						string newFile = getCurrentDirectory() + "/"
								+ event->name;
						sendout = watchRepsonse->createInotifyMessage(
								watchConnection->getID(), newFile,
								getModificationTime(newFile, false),
								"Create_File");
						watchConnection->sendMessage(sendout, "INOTIFY_TOPIC");
						watchLogger->writeLog(7,
								watchLogger->setLogData(
										"<SubutaiWatch::checkNotification>",
										"Sending Event Response: ", sendout));
					}
				} else if (event->mask & IN_DELETE) {
					setCurrentDirectory(get(event->wd));
					watchLogger->writeLog(7,
							watchLogger->setLogData(
									"<SubutaiWatch::checkNotification>",
									"Event Directory: ",
									getCurrentDirectory()));
					if (event->mask & IN_ISDIR) {
						status = true;
						setNewDirectory(
								getCurrentDirectory() + "/" + event->name);
						sendout = watchRepsonse->createInotifyMessage(
								watchConnection->getID(), getNewDirectory(),
								getModificationTime(getNewDirectory(), true),
								"Delete_Folder");
						watchConnection->sendMessage(sendout, "INOTIFY_TOPIC");
						watchLogger->writeLog(7,
								watchLogger->setLogData(
										"<SubutaiWatch::checkNotification>",
										"Sending Event Response: ", sendout));
					} else {
						status = true;
						string newFile = getCurrentDirectory() + "/"
								+ event->name;
						sendout = watchRepsonse->createInotifyMessage(
								watchConnection->getID(), newFile,
								getModificationTime(newFile, true),
								"Delete_File");
						watchConnection->sendMessage(sendout, "INOTIFY_TOPIC");
						watchLogger->writeLog(7,
								watchLogger->setLogData(
										"<SubutaiWatch::checkNotification>",
										"Sending Event Response: ", sendout));
					}
				} else if (event->mask & IN_MODIFY) {
					setCurrentDirectory(get(event->wd));
					watchLogger->writeLog(7,
							watchLogger->setLogData(
									"<SubutaiWatch::checkNotification>",
									"Event Directory: ",
									getCurrentDirectory()));
					if (event->mask & IN_ISDIR) {
						status = true;
						//NOT IMPLEMENTED on INOTIFY
					} else {
						status = true;
						string modFile = getCurrentDirectory() + "/"
								+ event->name;
						sendout = watchRepsonse->createInotifyMessage(
								watchConnection->getID(), modFile,
								getModificationTime(modFile, false),
								"Modify_file");
						watchConnection->sendMessage(sendout, "INOTIFY_TOPIC");
						watchLogger->writeLog(7,
								watchLogger->setLogData(
										"<SubutaiWatch::checkNotification>",
										"Sending Event Response: ", sendout));
					}
				} else if (event->mask & IN_ATTRIB) {
					setCurrentDirectory(get(event->wd));
					watchLogger->writeLog(7,
							watchLogger->setLogData(
									"<SubutaiWatch::checkNotification>",
									"Event Directory: ",
									getCurrentDirectory()));
					if (event->mask & IN_ISDIR) {
						status = true;
						string modFile = getCurrentDirectory() + "/"
								+ event->name;
						sendout = watchRepsonse->createInotifyMessage(
								watchConnection->getID(), modFile,
								getModificationTime(modFile, false),
								"Modify_Permission_Folder");
						watchConnection->sendMessage(sendout, "INOTIFY_TOPIC");
						watchLogger->writeLog(7,
								watchLogger->setLogData(
										"<SubutaiWatch::checkNotification>",
										"Sending Event Response: ", sendout));
					} else {
						status = true;
						string modFile = getCurrentDirectory() + "/"
								+ event->name;
						sendout = watchRepsonse->createInotifyMessage(
								watchConnection->getID(), modFile,
								getModificationTime(modFile, false),
								"Modify_Permission_File");
						watchConnection->sendMessage(sendout, "INOTIFY_TOPIC");
						watchLogger->writeLog(7,
								watchLogger->setLogData(
										"<SubutaiWatch::checkNotification>",
										"Sending Event Response: ", sendout));
					}
				}
			}
			i += EVENT_SIZE + event->len;
		}
	} catch (exception e) {
		cout << e.what() << endl;
	}
	return status;
}

string SubutaiWatch::getModificationTime(string folderpath, bool generate) {
	string unixResult;
	string deleteResult;
	try {
		if (generate == false) //unix folder creation and modification time
		{
			char buffer[100];
			struct stat t_stat;
			stat(folderpath.c_str(), &t_stat);
			struct tm * timeinfo = localtime(&t_stat.st_ctime);
			strftime(buffer, 80, "%d.%m.%Y %X", timeinfo);
			unixResult = (string) buffer;
		} else {
			boost::posix_time::ptime now =
					boost::posix_time::second_clock::local_time();
			deleteResult = helper.toString(now.date().day().as_number()) + "-"
					+ helper.toString(now.date().month().as_number()) + "-"
					+ helper.toString(now.date().year()) + " "
					+ helper.toString(now.time_of_day().hours()) + ":"
					+ helper.toString(now.time_of_day().minutes()) + ":"
					+ helper.toString(now.time_of_day().seconds());
		}
	} catch (exception e) {
		cout << e.what() << endl;
	}
	if (generate) {
		return deleteResult;
	} else {
		return unixResult;
	}
}

