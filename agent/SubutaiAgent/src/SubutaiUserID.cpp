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
#include "SubutaiUserID.h"

/**
 *  \details   Default constructor of the SubutaiUserID class.
 */
SubutaiUserID::SubutaiUserID()
{
	// TODO Auto-generated constructor stub
}

/**
 *  \details   Default destructor of the SubutaiUserID class.
 */
SubutaiUserID::~SubutaiUserID()
{
	// TODO Auto-generated destructor stub
}

/**
 *  \details   This method set the user rights to the given user.
 */
void SubutaiUserID::doSetuid (uid_t &euid)
{
	int status;

#ifdef _POSIX_SAVED_IDS
	status = seteuid (euid);
#else
	status = setreuid (ruid, euid);		//setting UID
#endif
	if (status < 0)
	{		//checking it
		fprintf (stderr, "NOT ACTIVATED.\n");
		exit (status);
	}
}

/**
 *  \details   This method can undo user rights to the root.
 */
void SubutaiUserID::undoSetuid(uid_t &ruid)
{
	int status;

#ifdef _POSIX_SAVED_IDS
	status = seteuid (ruid);
#else
	status = setreuid (euid, ruid);		//undo_UID
#endif
	if (status < 0)
	{
		fprintf (stderr, "NOT ACTIVATED.\n");
		exit (status);
	}
}

/**
 *  \details   This method checks given user on the system.
 *  		   If the user found on system it returns true. Otherwise it returns false.
 */
bool SubutaiUserID::getIDs(uid_t& ruid,uid_t&  euid,string runAs)	//getting UID on system
{
	struct passwd *pw;
	ruid = getuid();

	if (NULL == (pw = getpwnam(runAs.c_str())))
	{
		return false;
	}
	else
	{
		euid = pw->pw_uid;
		return true;
	}
}

/**
 *  \details   This method checks given user is root or not.
 *  		   If the user is root, it returns true. Otherwise it returns false.
 */
bool SubutaiUserID::checkRootUser()
{
	uid_t ruid = getuid();	//if the user is root = 0
	if (ruid==0)
		return true;		//the user is root
	else
		return false;		//user is not root
}
