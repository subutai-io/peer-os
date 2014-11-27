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

/**
 * \details 	Default constructor of SubutaiContainerManager
 */
SubutaiContainerManager::SubutaiContainerManager(string lxc_path, SubutaiLogger* logger) : _lxc_path(lxc_path), _logger(logger)
{
    // Check for running containers in case we just started an app
    // after crash
    try {
        _containers = findAllContainers();
    } catch (SubutaiException e) {
        _logger->writeLog(3, _logger->setLogData("<SubutaiContainerManager>", e.displayText()));         
    } catch (std::exception e) {
        _logger->writeLog(3, _logger->setLogData("<SubutaiContainerManager>", e.what()));         
    }

    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Initializing"));
}
/**
 * \details 	Default destructor of SubutaiContainerManager
 */
SubutaiContainerManager::~SubutaiContainerManager() 
{
}


/**
 * \details 	Check if container with the name given is running
 */
bool SubutaiContainerManager::isContainerRunning(string container_name) 
{
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Check if container running"));
    for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++) {
        if ((*it).getContainerHostnameValue().compare(container_name) == 0 && (*it).getState() == "RUNNING") {
            return true;
        }
    }
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Container not found: " + container_name));
    return false;
}

/**
 * \details 	Check if container with the name given is stopped
 */
bool SubutaiContainerManager::isContainerStopped(string container_name)
{
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Check if container stopped"));
    for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++) {
        if ((*it).getContainerHostnameValue().compare(container_name) == 0 && (*it).getState() == "STOPPED") {
            return true;
        }
    }
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Container not found: " + container_name));
    return false;
}

/**
 * \details 	Check if container with the name given is frozen
 */
bool SubutaiContainerManager::isContainerFrozen(string container_name)
{
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Check if container frozen"));
    for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++) {
        if ((*it).getContainerHostnameValue().compare(container_name) == 0 && (*it).getState() == "FROZEN") {
            return true;
        }
    }
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Container not found: " + container_name));
    return false;
}


/*
 * \details find all containers - returns all defined containers -active stopped running
 *
 */
vector<SubutaiContainer> SubutaiContainerManager::findAllContainers()
{
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Get all containers."));
    vector<SubutaiContainer> containers;
    char** names;
    lxc_container** cont;
    int num;
    try {
        num = list_all_containers(_lxc_path.c_str(), &names, &cont);
        for (int i = 0; i < num; i++) {
            SubutaiContainer* c = new SubutaiContainer(_logger, cont[i]);
            c->setContainerHostname(names[i]);
            _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", c->getContainerHostnameValue() + " added.."));
            containers.push_back(*c);
        }
    } catch (SubutaiException e) {

    } catch (std::exception e) {

    }
    return containers;
}

/*
 * \details find container using hostname
 *
 */
SubutaiContainer* SubutaiContainerManager::findContainerByName(string container_name)
{
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Get container by name: " + container_name));
    for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++) {
        if ((*it).getContainerHostnameValue().compare(container_name) == 0) {
            return &(*it);
        }
    }
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Container not found: " + container_name));
    return NULL;
}

/*
 * \details find container using id
 *
 */
SubutaiContainer* SubutaiContainerManager::findContainerById(string container_id)
{
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Get container by id: " + container_id));
    for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++) {
        if ((*it).getContainerIdValue() == container_id) {
            return &(*it);
        }
    }
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Container not found: " + container_id));
    return NULL;
}

/*
 * \details     Update active stopped and frozen lists
 */
void SubutaiContainerManager::updateContainerLists()
{
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Update container list and their fields.."));
    char** names;
    lxc_container** cont;
    int num;
    try {
        num = list_all_containers(_lxc_path.c_str(), &names, &cont);
        for (int i = 0; i < num; i++) {
            bool containerFound = false;
            for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++) {
                if ((*it).getContainerHostnameValue() == string(names[i])) {
                    containerFound = true;
                }
            }
            if (!containerFound) {
                SubutaiContainer* c = new SubutaiContainer(_logger, cont[i]);
                c->setContainerHostname(names[i]);
                _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", c->getContainerHostnameValue() + " added.."));
                _containers.push_back(*c);
            }
        }
    } catch (SubutaiException e) {

    } catch (std::exception e) {

    }
    for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++) {
        (*it).getContainerAllFields();
    }
}

/**
 * \details 	get running containers of resource host
 */
vector<SubutaiContainer> SubutaiContainerManager::getRunningContainers()
{
    vector<SubutaiContainer> cont;
    for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++) {
        if ((*it).getState() == "RUNNING") {
            cont.push_back((*it));
        }
    }
    return cont;
}

/**
 * \details 	get stopped containers of resource host
 */
vector<SubutaiContainer> SubutaiContainerManager::getStoppedContainers()
{
    vector<SubutaiContainer> cont;
    for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++) {
        if ((*it).getState() == "STOPPED") {
            cont.push_back((*it));
        }
    }
    return cont;
}

/**
 * \details 	get frozen containers of resource host
 */
vector<SubutaiContainer> SubutaiContainerManager::getFrozenContainers()
{
    vector<SubutaiContainer> cont;
    for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++) {
        if ((*it).getState() == "FROZEN") {
            cont.push_back((*it));
        }
    }
    return cont;
}

/**
 * \details 	get all containers of resource host
 */
vector<SubutaiContainer> SubutaiContainerManager::getAllContainers()
{
    return _containers;
}


/**
 * \details 	for testing
 */
void SubutaiContainerManager::write()
{
    cout << "active: \n";
    for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++) {
        if ((*it).getState() == "RUNNING") {
            (*it).write();
        }
    }
    cout << "stopped: \n";
    for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++) {
        if ((*it).getState() == "STOPPED") {
            (*it).write();
        }
    }
    cout << "frozen: \n";
    for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++) {
        if ((*it).getState() == "FROZEN") {
            (*it).write();
        }
    }
}
