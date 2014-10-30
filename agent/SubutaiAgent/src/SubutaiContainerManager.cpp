#include "SubutaiContainerManager.h"

SubutaiContainerManager::SubutaiContainerManager(string lxc_path) : _lxc_path(lxc_path) 
{
    // Check for running containers in case we just started an app
    // after crash
    char** names;
    lxc_container** cont;
    int num = list_active_containers(_lxc_path.c_str(), &names, &cont);
    for (int i = 0; i < num; i++) {
        SubutaiContainer c;
        c.uuid = "";
        c.id = 0;
        c.container = cont[i];
        _containers.push_back(c);
    }
}

SubutaiContainerManager::~SubutaiContainerManager() 
{

}

bool SubutaiContainerManager::isContainerRunning(string container_name) 
{
    for (vector<SubutaiContainer>::iterator it = _containers.begin(); it != _containers.end(); it++) {
        if ((*it).uuid.compare(container_name) == 0) {
            return true;
        }
    }
    return false;
}

SubutaiContainer SubutaiContainerManager::findContainer(string container_name) {
    for (vector<SubutaiContainer>::iterator it = _containers.begin(); it != _containers.end(); it++) {
        if ((*it).uuid.compare(container_name) == 0) {
            return (*it);
        }
    }
}

bool SubutaiContainerManager::RunProgram(string program, vector<string> params) {
    char* _params[params.size() + 2];
    _params[0] = const_cast<char*>(program.c_str());
    vector<string>::iterator it;
    int i = 1;
    for (it = params.begin(); it != params.end(); it++, i++) {
        _params[i] = const_cast<char*>(it->c_str());
    }    
    _params[i + 1] = NULL;
    lxc_attach_options_t opts = LXC_ATTACH_OPTIONS_DEFAULT;
    _current_container->attach_run_wait(_current_container, &opts, program.c_str(), _params);
}
