/**
 *  @brief     KAUserID.h
 *  @class     KAUserID.h
 *  @details   KAUserID class is designed for checking UserID on system it also uses set and undo operations of user rights.
 *  @author    Emin INAL
 *  @author    Bilal BAL
 *  @version   1.0
 *  @date      Sep 4, 2013
 *  @copyright GNU Public License.
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
