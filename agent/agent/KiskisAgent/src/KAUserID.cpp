/*
 *============================================================================
 Name        : KAUserID.cpp
 Author      : Bilal Bal
 Date		 : Sep 4, 2013
 Version     : 1.0
 Copyright   : Your copyright notice
 Description : KAUserID class is designed for checking UID on system it also used setting, undo operations of user rights.
==============================================================================
 */
#include "KAUserID.h"
KAUserID::KAUserID()
{
	// TODO Auto-generated constructor stub
}
KAUserID::~KAUserID()
{
	// TODO Auto-generated destructor stub
}
void KAUserID::doSetuid (uid_t &euid)
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
void KAUserID::undoSetuid(uid_t &ruid)
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
bool KAUserID::getIDs(uid_t& ruid,uid_t&  euid,string runAs)	//getting UID on system
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
bool KAUserID::checkRootUser()
{
	uid_t ruid = getuid();	//if the user is root = 0
	if(ruid==0)
		return true;		//the user is root
	else
		return false;		//user is not root
}
