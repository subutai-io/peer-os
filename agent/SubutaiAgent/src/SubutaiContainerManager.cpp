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
    vector<SubutaiContainer> tmp_container = findAllContainers();
    getContainerStates(tmp_container);

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
    for (vector<SubutaiContainer>::iterator it = _runningContainers.begin(); it != _runningContainers.end(); it++) {
        if ((*it).getContainerHostnameValue().compare(container_name) == 0) {
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
    for (vector<SubutaiContainer>::iterator it = _stoppedContainers.begin(); it != _stoppedContainers.end(); it++) {
        if ((*it).getContainerHostnameValue().compare(container_name) == 0) {
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
    for (vector<SubutaiContainer>::iterator it = _frozenContainers.begin(); it != _frozenContainers.end(); it++) {
        if ((*it).getContainerHostnameValue().compare(container_name) == 0) {
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
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Get all containers.."));
    vector<SubutaiContainer> containers;
    char** names;
    lxc_container** cont;
    int num = list_all_containers(_lxc_path.c_str(), &names, &cont);
    for (int i = 0; i < num; i++) {
        SubutaiContainer* c = new SubutaiContainer(_logger, cont[i]);
        c->setContainerHostname(names[i]);
        _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", c->getContainerHostnameValue() + " added.."));
        containers.push_back(*c);
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

    for (vector<SubutaiContainer>::iterator it = _runningContainers.begin(); it != _runningContainers.end(); it++) {
        if ((*it).getContainerHostnameValue().compare(container_name) == 0) {
            return &(*it);
        }
    }
    for (vector<SubutaiContainer>::iterator it = _stoppedContainers.begin(); it != _stoppedContainers.end(); it++) {
            if ((*it).getContainerHostnameValue().compare(container_name) == 0) {
                return &(*it);
            }
        }
    for (vector<SubutaiContainer>::iterator it = _frozenContainers.begin(); it != _frozenContainers.end(); it++) {
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
    for (ContainerIterator it = _runningContainers.begin(); it != _runningContainers.end(); it++) {
        if ((*it).getContainerIdValue() == container_id) {
            return &(*it);
        }
    }
    for (ContainerIterator it = _stoppedContainers.begin(); it != _stoppedContainers.end(); it++) {
            if ((*it).getContainerIdValue() == container_id) {
                return &(*it);
            }
    }
    for (ContainerIterator it = _frozenContainers.begin(); it != _frozenContainers.end(); it++) {
            if ((*it).getContainerIdValue() == container_id) {
                return &(*it);
            }
    }
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Container not found: " + container_id));
    return NULL;
}


/*
 * \details     get the states of all lxcs by using lxc-ls terminal command..
 * 				Frozen containers are returned by both --active and --frozen commands !
 *
 */
void SubutaiContainerManager::getContainerStates(vector<SubutaiContainer> _allContainers)
{
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Get container states.." ));
    _frozenContainers.clear();
    _runningContainers.clear();
    _stoppedContainers.clear();

    vector<string> running_containers = _helper.runAndSplit("lxc-ls --running", "r", "\n");
    vector<string> stopped_containers = _helper.runAndSplit("lxc-ls --stopped", "r", "\n");
    vector<string> frozen_containers  = _helper.runAndSplit("lxc-ls --frozen" , "r", "\n");

 	for (vector<SubutaiContainer>::iterator it = _allContainers.begin(); it != _allContainers.end(); it++) {
 		bool containerRegisteredToList = false;

		for (vector<string>::iterator it_status = frozen_containers.begin(); it_status != frozen_containers.end(); it_status++) {
			if((*it).getContainerHostnameValue() == (*it_status))
			{
			    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", (*it).getContainerHostnameValue() + "is added to frozen.."));
				(*it).setContainerStatus(FROZEN);
				_frozenContainers.push_back((*it));
				containerRegisteredToList = true; break;
			}
		}
		if(containerRegisteredToList) continue;
		for (vector<string>::iterator it_status = running_containers.begin(); it_status != running_containers.end(); it_status++) {
			if((*it).getContainerHostnameValue() == (*it_status))
			{
			    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", (*it).getContainerHostnameValue() + "is added to running.."));
				(*it).setContainerStatus(RUNNING);
				_runningContainers.push_back((*it));
				containerRegisteredToList = true; break;
			}
		}
		if(containerRegisteredToList) continue;
		for (vector<string>::iterator it_status = stopped_containers.begin(); it_status != stopped_containers.end(); it_status++) {
			if((*it).getContainerHostnameValue() == (*it_status))
			{
			    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", (*it).getContainerHostnameValue() + "is added to stopped.."));
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
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Update container list and their fields.."));
	  vector<SubutaiContainer> tmp_container = findAllContainers();
	  getContainerStates(tmp_container);

	  for (vector<SubutaiContainer>::iterator it = _runningContainers.begin(); it != _runningContainers.end(); it++)
	  {
		  (*it).getContainerAllFields();
	  }
	  for (vector<SubutaiContainer>::iterator it = _stoppedContainers.begin(); it != _stoppedContainers.end(); it++)
	  {
		  (*it).getContainerAllFields();
	  }
	  for (vector<SubutaiContainer>::iterator it = _frozenContainers.begin(); it != _frozenContainers.end(); it++)
	  {
		  (*it).getContainerAllFields();
	  }
	  //free(&tmp_container);
}


/**
 * \details 	get running containers of resource host
 */
vector<SubutaiContainer> SubutaiContainerManager::getRunningContainers()
{
	return _runningContainers;
}

/**
 * \details 	get stopped containers of resource host
 */
vector<SubutaiContainer> SubutaiContainerManager::getStoppedContainers()
{
	return _stoppedContainers;
}

/**
 * \details 	get frozen containers of resource host
 */
vector<SubutaiContainer> SubutaiContainerManager::getFrozenContainers()
{
	return _frozenContainers;
}

/**
 * \details 	get all containers of resource host
 */
vector<SubutaiContainer> SubutaiContainerManager::getAllContainers()
{
	vector<SubutaiContainer> concat;
	concat.reserve(_runningContainers.size() + _stoppedContainers.size() + _frozenContainers.size()); // preallocate memory
	concat.insert(concat.end(), _runningContainers.begin(), _runningContainers.end());
	concat.insert(concat.end(), _stoppedContainers.begin(), _stoppedContainers.end());
	concat.insert(concat.end(), _frozenContainers.begin(),  _frozenContainers.end());
	return concat;
}


/**
 * \details 	for testing
 */
void SubutaiContainerManager::write()
{
	cout << "active: \n";
	for (vector<SubutaiContainer>::iterator it = _runningContainers.begin(); it != _runningContainers.end(); it++)
		(*it).write();
	cout << "stopped: \n";
	for (vector<SubutaiContainer>::iterator it = _stoppedContainers.begin(); it != _stoppedContainers.end(); it++)
			(*it).write();
	cout << "frozen: \n";
	for (vector<SubutaiContainer>::iterator it = _frozenContainers.begin(); it != _frozenContainers.end(); it++)
			(*it).write();
}
