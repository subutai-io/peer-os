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
 *  @brief     KAUserID.h
 *  @class     KAUserID.h
 *  @details   KAUserID class is designed for checking UserID on system it also uses set and undo operations of user rights.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.0.1
 *  @date      Dec 17, 2013
 */
#ifndef KAUSERID_H_
#define KAUSERID_H_
#include <sys/types.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <pwd.h>
#include <syslog.h>
using namespace std;
class KAUserID
{
public:
	KAUserID();
	virtual ~KAUserID();
	void doSetuid( uid_t &);
	void undoSetuid( uid_t &);
	bool getIDs(uid_t&,uid_t&,string);
	bool checkRootUser();
};
#endif /* KAUSERID_H_ */
