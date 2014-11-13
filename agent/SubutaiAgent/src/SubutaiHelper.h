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
/**
 *  @brief     SubutaiHelper.h
 *  @class     SubutaiHelper.h
 *  @details   SubutaiHelper Class defines the helper methods.
 *  @author    Mikhail Savochkin
 *  @author    Ozlem Ceren Sahin
 *  @version   1.1.0
 *  @date      Nov 6, 2014
 */
#ifndef SUBUTAIHELPER_H_
#define SUBUTAIHELPER_H_
#include <syslog.h>
#include <stdio.h>
#include <unistd.h>
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <cstdlib>
#include <sstream>
#include <list>
using namespace std;
using std::stringstream;
using std::string;



//interface struct
struct Interface {
    string name;
    string mac;
    string ip;
};


class SubutaiHelper
{
    public:
		string execCommand(char*, char*);
		vector<string> splitResult(string, char*);
		vector<string> runAndSplit(char*, char*, char* );
		string toString(int);
};

#endif /* SUBUTAIHELPER_H_ */



