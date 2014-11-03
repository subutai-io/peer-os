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
 *  @brief     SubutaiContainerManager.cpp
 *  @class     SubutaiContainerManager.cpp
 *  @details   Manages containers on current host, uses LXC API.
 *  @author    Mikhail Savochkin
 *  @author    Ozlem Ceren Sahin
 *  @version   1.1.0
 *  @date      Oct 31, 2014
 */
#include "SubutaiContainerManager.h"
#include "SubutaiContainer.h"


SubutaiContainerManager::SubutaiContainerManager(string lxc_path, SubutaiLogger* logger) : _lxc_path(lxc_path), _logger(logger)
{
    // Check for running containers in case we just started an app
    // after crash
    vector<SubutaiContainer> tmp_container = findAllContainers();
    getContainerStates(tmp_container);
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Initializing"));
}

SubutaiContainerManager::~SubutaiContainerManager() 
{
}

void SubutaiContainerManager::init() {
}

bool SubutaiContainerManager::isContainerRunning(string container_name) 
{
    for (vector<SubutaiContainer>::iterator it = _running_containers.begin(); it != _running_containers.end(); it++) {
        if ((*it).getContainerHostnameValue().compare(container_name) == 0) {
            return (*it).isContainerRunning();
        }
    }
    return false;
}

bool SubutaiContainerManager::isContainerStopped(string container_name)
{
    for (vector<SubutaiContainer>::iterator it = _running_containers.begin(); it != _running_containers.end(); it++) {
        if ((*it).getContainerHostnameValue().compare(container_name) == 0) {
            return (*it).isContainerStopped();
        }
    }
    return false;
}

bool SubutaiContainerManager::isContainerFrozen(string container_name)
{
    for (vector<SubutaiContainer>::iterator it = _running_containers.begin(); it != _running_containers.end(); it++) {
        if ((*it).getContainerHostnameValue().compare(container_name) == 0) {
            return (*it).isContainerFrozen();
        }
    }
    return false;
}
/*
 * \details find defined containers - returns all defined containers -active stopped running
 *
 */
vector<SubutaiContainer> SubutaiContainerManager::findDefinedContainers()
{
    vector<SubutaiContainer> containers;
    char** names;
    lxc_container** cont;
    int num = list_defined_containers(_lxc_path.c_str(), &names, &cont);
    for (int i = 0; i < num; i++) {
        SubutaiContainer* c = new SubutaiContainer(_logger, cont[i]);
        c->setContainerHostname(names[i]);
        containers.push_back(*c);
    }
    return containers;

}

/*
 * \details find only active containers
 *			It returns frozen and running containers
 */
vector<SubutaiContainer> SubutaiContainerManager::findActiveContainers()
{
    vector<SubutaiContainer> containers;
    char** names;
    lxc_container** cont;
    int num = list_active_containers(_lxc_path.c_str(), &names, &cont);
    for (int i = 0; i < num; i++) {
        SubutaiContainer* c = new SubutaiContainer(_logger, cont[i]);
        c->setContainerHostname(names[i]);
        containers.push_back(*c);
    }
    return containers;

}

/*
 * \details find all containers - returns all defined containers -active stopped running
 *
 */
vector<SubutaiContainer> SubutaiContainerManager::findAllContainers()
{
    vector<SubutaiContainer> containers;
    char** names;
    lxc_container** cont;
    int num = list_all_containers(_lxc_path.c_str(), &names, &cont);
    for (int i = 0; i < num; i++) {
        SubutaiContainer* c = new SubutaiContainer(_logger, cont[i]);
        c->setContainerHostname(names[i]);
        containers.push_back(*c);
    }
    return containers;

}


/*
 * \details find container using hostname/name
 *
 */
SubutaiContainer* SubutaiContainerManager::findContainerByName(string container_name) {

    for (vector<SubutaiContainer>::iterator it = _running_containers.begin(); it != _running_containers.end(); it++) {
        if ((*it).getContainerHostnameValue().compare(container_name) == 0) {
            return &(*it);
        }
    }
    return NULL;
}

/*
 * \details find container using id
 *
 */
SubutaiContainer* SubutaiContainerManager::findContainerById(string container_id) {
    for (ContainerIterator it = _running_containers.begin(); it != _running_containers.end(); it++) {
        if ((*it).getContainerIdValue() == container_id) {
            return &(*it);
        }
    }
    return NULL;
}

/*
 * \details send registration message from all lxcs
 *
 */
void SubutaiContainerManager::registerAllContainers(SubutaiConnection* connection)
{

    // TODO: Do we need to register all containers? Maybe active only?
    for (ContainerIterator it = _running_containers.begin(); it != _running_containers.end(); it++) {
        (*it).registerContainer(connection);
    }
}

/*
 * \details execute a terminal command. type = r, w, rw
 *
 */
string exec(char* cmd, char* type) {
    FILE* pipe = popen(cmd, type);
    if (!pipe) return "ERROR";
    char buffer[128];
    string result = "";
    while(!feof(pipe)) {
        if(fgets(buffer, 128, pipe) != NULL)
            result += buffer;
    }
    pclose(pipe);
    return result;
}

/*
 * \details split string by delimeter
 *
 */
vector<string> splitContainers(string list, char* delimeter) {
    vector<string> tokens;
    size_t pos = 0;
    std::string token;
    while ((pos = list.find(delimeter)) != std::string::npos) {
        token = list.substr(0, pos);
        tokens.push_back(token);
        list.erase(0, pos + 1);
    }
    return tokens;
}

/*
 * \details     get the states of all lxcs by using lxc-ls terminal command..
 * 				Frozen containers are returned by both --active and --frozen commands !
 *
 */
void SubutaiContainerManager::getContainerStates(vector<SubutaiContainer> _allContainers)
{
    vector<string> running_containers = splitContainers(exec("lxc-ls --running", "r"), "\n");
    vector<string> stopped_containers = splitContainers(exec("lxc-ls --stopped", "r"), "\n");
    vector<string> frozen_containers = splitContainers(exec("lxc-ls --frozen", "r"), "\n");


 	for (vector<SubutaiContainer>::iterator it = _allContainers.begin(); it != _allContainers.end(); it++) {
 		bool containerRegisteredToList = false;
 		(*it).setContainerHostname("");
		for (vector<string>::iterator it_status = frozen_containers.begin(); it_status != frozen_containers.end(); it_status++) {
			if((*it).getContainerHostnameValue() == (*it_status))
			{
				(*it).setContainerStatus(FROZEN);
				_frozenContainers.push_back((*it));
				containerRegisteredToList = true; break;
			}
		}
		if(containerRegisteredToList) continue;
		for (vector<string>::iterator it_status = running_containers.begin(); it_status != running_containers.end(); it_status++) {
			if((*it).getContainerHostnameValue() == (*it_status))
			{
				(*it).setContainerStatus(RUNNING);
				_running_containers.push_back((*it));
				containerRegisteredToList = true; break;
			}
		}
		if(containerRegisteredToList) continue;
		for (vector<string>::iterator it_status = stopped_containers.begin(); it_status != stopped_containers.end(); it_status++) {
			if((*it).getContainerHostnameValue() == (*it_status))
			{
				(*it).setContainerStatus(STOPPED);
				_stoppedContainers.push_back((*it));
				break;
			}
		}
	}







}


/*
 * \details     Update active stopped and frozen lists
 */
void SubutaiContainerManager::updateContainerLists()
{
	  vector<SubutaiContainer> tmp_container = findAllContainers();
	  getContainerStates(tmp_container);

	  for (vector<SubutaiContainer>::iterator it = _running_containers.begin(); it != _running_containers.end(); it++)
	  {
		  (*it).getContainerAllFields();
	  }
}


void SubutaiContainerManager::write()
{

	cout << "active: \n";
	for (vector<SubutaiContainer>::iterator it = _running_containers.begin(); it != _running_containers.end(); it++)
		(*it).write();
	cout << "stopped: \n";
	for (vector<SubutaiContainer>::iterator it = _stoppedContainers.begin(); it != _stoppedContainers.end(); it++)
			(*it).write();
	cout << "frozen: \n";
	for (vector<SubutaiContainer>::iterator it = _frozenContainers.begin(); it != _frozenContainers.end(); it++)
			(*it).write();


}


/*
 * \details     Runs lxc's attach_run_wait function to specified container
 */
/*
   string SubutaiContainerManager::RunProgram(SubutaiContainer* cont, string program, vector<string> params) {
   char* _params[params.size() + 2];
   _params[0] = const_cast<char*>(program.c_str());
   vector<string>::iterator it;
   int i = 1;
   for (it = params.begin(); it != params.end(); it++, i++) {
   _params[i] = const_cast<char*>(it->c_str());
   }    
   _params[i + 1] = NULL;
   lxc_attach_options_t opts = LXC_ATTACH_OPTIONS_DEFAULT;
   int fd[2];
   pipe(fd);
   int _stdout = dup(1);
   dup2(fd[1], 1);
   char buffer[1000];
   int exit_code = cont->getLxcContainerValue()->attach_run_wait(_current_container, &opts, program.c_str(), _params);
   fflush(stdout);
   string command_output = "";
// TODO: Implement checking of buffer size here
while (1) {
ssize_t size = read(fd[0], buffer, 1000);
command_output += buffer;
if (size < 1000) {
buffer[size] = '\0';
command_output += buffer;
}
}
dup2(_stdout, 1);

return command_output;
}
*/
/*
 * \details     Collect info from running containers for heartbeat packets
 * 
 *//*
      void SubutaiContainerManager::CollectInfo() {
      vector<string> params;
      params.push_back("-a");
      for (ContainerIterator it = _activeContainers.begin(); it != _activeContainers.end(); it++) {
      UpdateNetworkingInfo(&(*it), RunProgram(&(*it), "/bin/ifconfig", params));
      }
      }*/

/*
 * \details     Parses output of ifconfig and updates Container
 *              We can move this to some another class where we will collect all usefull common methods
 */
/*
   void SubutaiContainerManager::UpdateNetworkingInfo(SubutaiContainer* cont, string data) {
// Clear previously stored data
cont->ip.clear();
cont->mac.clear();
size_t n = 0;
size_t p = 0;
vector<string> res;
bool nextIsMac = false;
bool nextIsIp = false;
// Tokenize the data by spaces and extract mac and ip
while ((n = data.find_first_of(" ", p)) != string::npos) {
if (n - p != 0) {
if (nextIsMac) {
cont->mac.push_back(data.substr(p, n - p));
nextIsMac = false;
} else if (nextIsIp) {
// On a some systems ifconfig may differ from others by adding
// a space after "inet addr:"
string bad_part = "addr:";
string ip = data.substr(p, n - p);
if (ip.substr(0, bad_part.length()).compare(bad_part) == 0) {
ip = data.substr(bad_part.length(), ip.length());
} 
cont->ip.push_back(ip);
nextIsIp = false;
}
if (data.substr(p, n - p).compare("HWaddr") == 0) {
nextIsMac = true;
} else if (data.substr(p, n - p).compare("inet") == 0) {
nextIsIp = true;
}
}
p = n + 1;
}
}

void SubutaiContainerManager::UpdateUsersList(SubutaiContainer* cont) {
cont->users.clear();
vector<string> params;
params.push_back("/etc/passwd");
string passwd = RunProgram(cont, "/bin/cat", params);
size_t n = 0;
size_t p = 0;
stringstream ss(passwd);
string line;
while (getline(ss, line, '\n')) {
int c = 0;
int uid;
string uname;
while ((n = line.find_first_of(":", p)) != string::npos) {
c++;
if (n - p != 0) {
if (c == 1) {
// This is a username
uname = line.substr(p, n - p);
} else if (c == 3) {
// This is a uid
stringstream conv(line.substr(p, n - p));
if (!(conv >> uid)) {
uid = -1; // We failed to convert string to int
}
}
}
cont->users.insert(make_pair(uid, uname));
}
}
}
*/
