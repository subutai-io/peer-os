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

#include "SubutaiContainerManager.h"

SubutaiContainerManager::SubutaiContainerManager(string lxc_path, SubutaiLogger* logger) : _lxc_path(lxc_path), _logger(logger)
{
    // Check for running containers in case we just started an app
    // after crash
    findAllContainers();
}

SubutaiContainerManager::~SubutaiContainerManager() 
{
}

void SubutaiContainerManager::init() {
}

bool SubutaiContainerManager::isContainerRunning(string container_name) 
{
    for (vector<SubutaiContainer>::iterator it = _activeContainers.begin(); it != _activeContainers.end(); it++) {
        if ((*it).hostname.compare(container_name) == 0) {
            return true;
        }
    }
    return false;
}

void SubutaiContainerManager::findDefinedContainers()
{
    char** names;
    lxc_container** cont;
    int num = list_defined_containers(_lxc_path.c_str(), &names, &cont);
    for (int i = 0; i < num; i++) {
        SubutaiContainer c;
        c.uuid = "";
        c.hostname = names[i];
        c.container = cont[i];
        _definedContainers.push_back(c);
    }
}
void SubutaiContainerManager::findActiveContainers()
{
    char** names;
    lxc_container** cont;
    int num = list_active_containers(_lxc_path.c_str(), &names, &cont);
    for (int i = 0; i < num; i++) {
        SubutaiContainer c;
        c.uuid = "";
        c.hostname = names[i];
        c.container = cont[i];
        _activeContainers.push_back(c);
    }
}
void SubutaiContainerManager::findAllContainers()
{
    char** names;
    lxc_container** cont;
    int num = list_all_containers(_lxc_path.c_str(), &names, &cont);
    for (int i = 0; i < num; i++) {
        SubutaiContainer c;
        c.uuid = "";
        c.hostname = names[i];
        c.container = cont[i];
        _allContainers.push_back(c);
    }
}

SubutaiContainer SubutaiContainerManager::findContainer(string container_name) {
    for (vector<SubutaiContainer>::iterator it = _activeContainers.begin(); it != _activeContainers.end(); it++) {
        if ((*it).hostname.compare(container_name) == 0) {
            return (*it);
        }
    }
}

/*
 * \details     Runs lxc's attach_run_wait function to specified container
 */
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
    cont->container->attach_run_wait(_current_container, &opts, program.c_str(), _params);
    fflush(stdout);
    string command_output;
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

/*
 * \details     Collect info from running containers for heartbeat packets
 * 
 */
void SubutaiContainerManager::CollectInfo() {
    vector<string> params;
    params.push_back("-a");
    for (ContainerIterator it = _activeContainers.begin(); it != _activeContainers.end(); it++) {
        UpdateNetworkingInfo(&(*it), RunProgram(&(*it), "ipconfig", params));
    }
}

/*
 * \details     Parses output of ifconfig and updates Container
 *              We can move this to some another class where we will collect all usefull common methods
 */
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
    string passwd = RunProgram(cont, "cat", params);
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
