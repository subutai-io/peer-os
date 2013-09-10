/*
 * responsePack.h
 *
 *  Created on: Sep 4, 2013
 *      Author: qt-test
 */

#ifndef RESPONSEPACK_H_
#define RESPONSEPACK_H_
#include <iostream>
#include <jsoncpp/json.h>
#include "Response.h"
using namespace std;
class responsePack : public Response
{
public:
	responsePack();
	virtual ~responsePack();
	string createResponseMessage(string,int,int,string,string);
	string createExitMessage(string, int, int);
	string createRegMessage(string,string);

private:
	string sendout;
};

#endif /* RESPONSEPACK_H_ */
