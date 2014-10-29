#include "SubutaiContainerManager.h"

SubutaiContainerManager::SubutaiContainerManager(string lxc_path) : _lxc_path(lxc_path) 
{

}

SubutaiContainerManager::~SubutaiContainerManager() 
{

}

bool SubutaiContainerManager::isContainerRunning(string container_name) 
{
    char** names;
    lxc_container** cont;
    int num = list_active_containers(_lxc_path.c_str(), &names, &cont);
    for (int i = 0; i < num; i++) {
        if (names[i] == container_name.c_str()) {
            return true;
        }
    }
    return false;
}

bool SubutaiContainerManager::findContainer(string container_name) {
    char** names;
    lxc_container** cont;
    int num = list_active_containers(_lxc_path.c_str(), &names, &cont);
    for (int i = 0; i < num; i++) {
        if (names[i] == container_name.c_str()) {
            _current_container = cont[i];
            return true;
        }
    }
    return false;
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
