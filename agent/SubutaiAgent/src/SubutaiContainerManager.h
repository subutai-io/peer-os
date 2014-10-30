#ifndef __SUBUTAI_CONTAINER_MANAGER_H__
#define __SUBUTAI_CONTAINER_MANAGER_H__

#include <string>
#include <vector>
#include <lxc/lxccontainer.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>

using namespace std;

struct SubutaiContainer {
    string uuid;
    string hostname;
    lxc_container* container;
};

class SubutaiContainerManager {
    public:
        SubutaiContainerManager(string lxc_path);
        ~SubutaiContainerManager();
        SubutaiContainer findContainer(string container_name);
        vector<SubutaiContainer> findAllContainers(string lxc_path);
        vector<SubutaiContainer> findActiveContainers(string lxc_path);
        vector<SubutaiContainer> findDefinedContainers(string lxc_path);
        bool isContainerRunning(string container_name);
        bool RunProgram(string program, vector<string> params);
    private:
        string          _lxc_path;
        lxc_container*  _current_container;
        vector<SubutaiContainer> _allContainers;
        vector<SubutaiContainer> _definedContainers;
        vector<SubutaiContainer> _activeContainers;
};

#endif
