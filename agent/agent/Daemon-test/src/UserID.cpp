/*
 * UserID.cpp
 *
 *  Created on: Sep 4, 2013
 *      Author: qt-test
 */

#include "UserID.h"



UserID::UserID() {
	// TODO Auto-generated constructor stub

}

UserID::~UserID() {
	// TODO Auto-generated destructor stub
}

void UserID::do_setuid (uid_t &euid)
{
	int status;

#ifdef _POSIX_SAVED_IDS
	status = seteuid (euid);
#else
	status = setreuid (ruid, euid);
#endif
	if (status < 0) {
		fprintf (stderr, "NOT ACTIVATED.\n");
		exit (status);
	}
}
void UserID::undo_setuid(uid_t &ruid)
{
	int status;

#ifdef _POSIX_SAVED_IDS
	status = seteuid (ruid);
#else
	status = setreuid (euid, ruid);
#endif
	if (status < 0) {
		fprintf (stderr, "NOT ACTIVATED.\n");
		exit (status);
	}
}

bool UserID::getIDs(uid_t& ruid,uid_t&  euid,string runAs)
{
	struct passwd *pw;
	ruid = getuid();

	  if (NULL == (pw = getpwnam(runAs.c_str())))
	  {
	     perror("getpwnam() error.");
	     cout << perror <<endl;
	     return false;
	  }
	  else
	  {
			euid = pw->pw_uid;
			return true;
	  }

}
