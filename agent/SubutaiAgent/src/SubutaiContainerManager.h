#ifndef __SUBUTAI_CONTAINER_MANAGER_H__
#define __SUBUTAI_CONTAINER_MANAGER_H__

#include <string>
#include <lxc/lxccontainer.h>

using namespace std;

class SubutaiContainerManager {
    public:
        SubutaiContainerManager(string lxc_path);
        ~SubutaiContainerManager();
        bool findContainer(string container_name);
        bool isContainerRunning(string container_name);
        bool RunProgram(string program, char* params[]);
    private:
        string          _lxc_path;
        lxc_container*  _current_container;
};

#endif
