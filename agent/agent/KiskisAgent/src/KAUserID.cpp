#include "KAUserID.h"
/**
 *  \details   Default constructor of the KAUserID class.
 */
KAUserID::KAUserID()
{
	// TODO Auto-generated constructor stub
}
/**
 *  \details   Default destructor of the KAUserID class.
 */
KAUserID::~KAUserID()
{
	// TODO Auto-generated destructor stub
}
/**
 *  \details   This method set the user rights to the given user.
 */
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
/**
 *  \details   This method can undo user rights to the root.
 */
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
/**
 *  \details   This method checks given user on the system.
 *  		   If the user found on system it returns true. Otherwise it returns false.
 */
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
/**
 *  \details   This method checks given user is root or not.
 *  		   If the user is root, it returns true. Otherwise it returns false.
 */
bool KAUserID::checkRootUser()
{
	uid_t ruid = getuid();	//if the user is root = 0
	if(ruid==0)
		return true;		//the user is root
	else
		return false;		//user is not root
}
