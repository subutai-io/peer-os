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
        if (names[i].strcmp(container_name.c_str()) == 0) {
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
        if (names[i].strcmp(container_name.c_str()) == 0) {
            _current_container = cont[i];
            return true;
        }
    }
    return false;
}

bool SubutaiContainerManager::RunProgram(string program, char* params) {
    
}
