#ifndef __SUBUTAI_CONTAINER_MANAGER_H__
#define __SUBUTAI_CONTAINER_MANAGER_H__

#include <string>
#include <vector>
#include <lxc/lxccontainer.h>

using namespace std;

struct SubutaiContainer {
    string uuid;
    int id;
    lxc_container* container;
};

class SubutaiContainerManager {
    public:
        SubutaiContainerManager(string lxc_path);
        ~SubutaiContainerManager();
        SubutaiContainer findContainer(string container_name);
        bool isContainerRunning(string container_name);
        bool RunProgram(string program, vector<string> params);
    private:
        string          _lxc_path;
        lxc_container*  _current_container;
        vector<SubutaiContainer> _containers;
};

#endif
