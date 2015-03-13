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
 *  @brief     SubutaiHelper.cpp
 *  @class     SubutaiHelper.cpp
 *  @details   SubutaiHelper Class defines the helper methods..
 *  @author    Mikhail Savochkin
 *  @author    Ozlem Ceren Sahin
 *  @version   1.1.0
 *  @date      Oct 31, 2014
 */
#include "SubutaiHelper.h"

using namespace std;

/**
 *  \details   This method designed for Typically conversion from integer to string.
 */
string SubutaiHelper::toString(int intcont) {	//integer to string conversion
	ostringstream dummy;
	dummy << intcont;
	return dummy.str();
}

/*
 * \details split string by delimeter
 *
 */
vector<string> SubutaiHelper::runAndSplit(char* cmd, char* type,
		char* delimeter) {
	return splitResult(execCommand(cmd, type), delimeter);
}

/*
 * \details execute a terminal command. type = r, w, rw
 *
 */
string SubutaiHelper::execCommand(char* cmd, char* type) {
	FILE* pipe = popen(cmd, type);
	if (!pipe)
		return "ERROR";
	char buffer[128];
	string result = "";
	while (!feof(pipe)) {
		if (fgets(buffer, 128, pipe) != NULL)
			result += buffer;
	}
	pclose(pipe);
	return result;
}

/*
 * \details split string by delimeter
 *
 */
vector<string> SubutaiHelper::splitResult(string list, char* delimeter) {
	vector<string> tokens;
	size_t pos = 0;
	std::string token;
	while ((pos = list.find(delimeter)) != std::string::npos) {
		token = list.substr(0, pos);
		if (pos != 0)
			tokens.push_back(token);
		list.erase(0, pos + 1);
	}
	if (list.size() > 0)
		tokens.push_back(list);
	return tokens;
}

void SubutaiHelper::writeToFile(string& path, string& input) {
	fstream file;	//opening commandQueue.txt
	file.open(path.c_str(), fstream::in | fstream::out | fstream::app);
	file << input;
	file.close();
}

string& SubutaiHelper::readFromFile(string& path) {
	string input, str;
	ifstream file(path.c_str());
	if (file.peek() != ifstream::traits_type::eof()) {
		ofstream file_tmp("/etc/subutai-agent/tmp.txt");
		input = "";
		getline(file, str);
		input = str;
		while (getline(file, str)) {
			file_tmp << str << endl;
		}
		file_tmp.close();
		rename("/etc/subutai-agent/tmp.txt", path.c_str());
	}
	return input;
}

void SubutaiHelper::removeFromFile(string& path, string& sub_string) {
	string input, str;
	ifstream file(path.c_str());
	if (file.peek() != ifstream::traits_type::eof()) {
		ofstream file_tmp("/etc/subutai-agent/tmp.txt");
		while (getline(file, str)) {
			size_t found = str.find(sub_string);
			if (found == std::string::npos) {
				file_tmp << str << endl;
			}
		}
		file_tmp.close();
		rename("/etc/subutai-agent/tmp.txt", path.c_str());
	}
}
