/*
 *============================================================================
 Name        : KAUserID.h
 Author      : Bilal Bal
 Date		 : Sep 4, 2013
 Version     : 1.0
 Copyright   : Your copyright notice
 Description : KAUserID class is designed for checking UID on system it also used setting, undo operations of user rights.
==============================================================================
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
