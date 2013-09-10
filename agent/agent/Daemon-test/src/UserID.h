/*
 * UserID.h
 *
 *  Created on: Sep 4, 2013
 *      Author: qt-test
 */

#ifndef USERID_H_
#define USERID_H_
#include <sys/types.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <pwd.h>
using namespace std;
class UserID {
public:
	UserID();
	virtual ~UserID();
	void do_setuid( uid_t &);
	void undo_setuid( uid_t &);
	bool getIDs(uid_t&,uid_t&,string);

};

#endif /* USERID_H_ */
