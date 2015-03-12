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

void logStep( string  msg)
{
	//cout << msg << endl;
}
/**
 * \details 	Default constructor of SubutaiContainerManager
 */
SubutaiContainerManager::SubutaiContainerManager(string lxc_path, SubutaiLogger* logger) : _lxc_path(lxc_path), _logger(logger)
{
    // Check for running containers in case we just started an app
    // after crash
    try {
    	logStep("Container manager is started construction");
    	_containers = findAllContainers();
    	logStep("updating container Id List in constructor");
    	updateContainerIdListOnStart(); // remove the id-hostname matchings for removed containers from containerIdList
    	for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++)
    	{
        	logStep("Work on container ids on start");
    		(*it)->getContainerId();
    	}

    } catch (SubutaiException e) {
        _logger->writeLog(3, _logger->setLogData("<SubutaiContainerManager>", e.displayText()));         
    } catch (std::exception e) {
        _logger->writeLog(3, _logger->setLogData("<SubutaiContainerManager>", e.what()));         
    }

    _logger->writeLog(6, _logger->setLogData("<SubutaiContainerManager>", "Initializing"));
}


/**
 * \details 	Default destructor of SubutaiContainerManager
 */
SubutaiContainerManager::~SubutaiContainerManager() 
{
	logStep("Container Manager Destruction");
}

/**
 * \details 	check if a container is deleted or added and update container hostname-id matching list on start of agent
 */
void SubutaiContainerManager::updateContainerIdListOnStart()
{
	 	string hostname_file, id;

	 	string path = "/etc/subutai-agent/";
	    string uuidFile = path + "containerIdList.txt";

	    ifstream file(uuidFile.c_str());
	    ofstream temp("temp.txt"); // put id-hostname matchings which wont be deleted in temp


	    while(file >> hostname_file >> id)
	    {
	    	for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++) {
	    	   	/*If new container is added, containerIdList is updated in getContainerId() method*/
	    	    if ( !strcmp(hostname_file.c_str(), (*it)->getContainerHostnameValue().c_str()))
	    	    {
	    	    	temp << hostname_file << " " << id << endl;
	    	    	break;
	    	    }
	    	}
	    }
	    file.clear(); // clear eof and fail bits
	 	file.seekg(0, ios::beg);
	 	file.close();
	 	temp.close();
	 	remove(uuidFile.c_str());
	 	rename("temp.txt",uuidFile.c_str());
}


/**
 * Delete related container info from containerIdList file when a container is destroyed.
 */
void SubutaiContainerManager::deleteContainerInfo(string hostname)
{
    string hostname_file, id;

    string path = "/etc/subutai-agent/";
    string uuidFile = path + "containerIdList.txt";

	logStep("Deleting container info from containerIdList.txt");
    ifstream file(uuidFile.c_str());
    ofstream temp("temp.txt"); // put id-hostname matchings which wont be deleted in temp


    while(file >> hostname_file >> id)
    {
        if(strcmp(hostname_file.c_str(),hostname.c_str())){ // if the entry is different from hostname which will be deleted, put it in temp file.
            temp << hostname_file << " " << id << endl;
        }
    }
    file.clear(); // clear eof and fail bits
    file.seekg(0, ios::beg);
    file.close();
    temp.close();
	logStep("Deletion of container id is finished");
    remove(uuidFile.c_str());
    rename("temp.txt",uuidFile.c_str());
	logStep("Change file name from tmp to containerIdList");
}

/*
 * \details get the word from a line when the end of the word is specified with \n - new line.
 *
 */
void get_word_from_line(char * line, int size)
{
	for(int i=0; i<size; i++)
	{
		if(line[i] == '\n')
		{
			line[i] = '\0';
			break;
		}
	}
}

/*
 * \details This method gets the list of containers using subutai methodology.
 */
vector<string> SubutaiContainerManager::getContainers()
{
	logStep("get container list via subutai list -c");
	FILE * fp = popen("subutai list -c", "r");
	vector<string> containerList;
	if (fp)
	{
		char *line=NULL; size_t n;
		if ((getline(&line, &n, fp) > 0) )
		{
			get_word_from_line(line, static_cast<int>(n) );
			if(strcmp(line, "CONTAINER") != 0 )
			{
				_logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Return value of 'subutai list -c' is changed.. The first line is not 'CONTAINER'. It may cause problems, please check."));
			}
		}
		if ((getline(&line, &n, fp) > 0) )
		{
			get_word_from_line(line, static_cast<int>(n) );
			if(strcmp(line, "---------") != 0 )
			{
				_logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Return value of 'subutai list -c' is changed.. The second line is not '---------'. It may cause problems, please check."));
			}
		}
	    while ((getline(&line, &n, fp) > 0) && line)
	    {
	    	get_word_from_line(line, static_cast<int>(n) );
	    	logStep("line is found");
	     	containerList.push_back(line);
	    }
	    pclose(fp);
	}
	logStep("get container list via subutai finished");
	return containerList;
}

/*
 * \details lxc api returns both containers and templates by list_all_containers.
 * 			This method returns true if element is not template.
 *
 */
bool SubutaiContainerManager::checkIfTemplate(string container_name, vector<string> containerList, bool isSubutaiAvailable)
{
	logStep("Clean container list by comparing with subutai list -c.");
	// Template - container difference is only available for Subutai systems, otherwise always return true.
	if(!isSubutaiAvailable) return false;
	for(vector<string>::const_iterator it = containerList.begin(); it != containerList.end(); ++it)
	{
		if ( strcmp( (*it).c_str(), container_name.c_str()) == 0)
		{
	    	logStep(container_name + " is a container.");
			return false;
		}
	}
	logStep(container_name + " is a template.");
	return true;
}


/*
 * \details find all containers - returns all defined containers -active stopped running
 *
 */
vector<SubutaiContainer*> SubutaiContainerManager::findAllContainers()
{
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Get all containers."));
    vector<SubutaiContainer*> containers;
    vector<string> subutai_containers;
    char** names;
    lxc_container** cont;
    int num;
    bool isSubutaiAvailable = system("which subutai")==0;
    try {
    	logStep("Prepare the list of containers");
    	if(isSubutaiAvailable) subutai_containers = getContainers();
    	logStep("Get list of container via lxc api");
        num = list_all_containers(_lxc_path.c_str(), &names, &cont);
        for (int i = 0; i < num; i++) {
        	if( !checkIfTemplate(names[i], subutai_containers, isSubutaiAvailable) )
        	{
				SubutaiContainer* c = new SubutaiContainer(_logger, cont[i]);
				c->setContainerHostname(names[i]);
				_logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", c->getContainerHostnameValue() + " added.."));
				containers.push_back(c);
		    	logStep(c->getContainerHostnameValue() + " is added to list");
        	}
        }
    } catch (SubutaiException e) {
        _logger->writeLog(3, _logger->setLogData("<SubutaiContainerManager>", e.displayText()));
    } catch (std::exception e) {
        _logger->writeLog(3, _logger->setLogData("<SubutaiContainerManager>", string(e.what())));
    }
    logStep("size of container list : " + _helper.toString(containers.size()));
    return containers;
}

/*
 * \details find container using id
 *
 */
SubutaiContainer* SubutaiContainerManager::findContainerById(string container_id)
{
	logStep("Find container having id : " + container_id);
    _logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Get container by id: " + container_id));
    for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++) {
        if ((*it)->getContainerIdValue() == container_id) {
        	logStep("Container found: " + (*it)->getContainerHostnameValue() );
            return (*it);
        }
    }
    _logger->writeLog(1, _logger->setLogData("<SubutaiContainerManager>", "Container not found: " + container_id));
	logStep("No container found with id " + container_id);
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
    vector<string> subutai_containers;
    int num, index = 0, size_of_containers = _containers.size();
    bool isSubutaiAvailable = system("which subutai")==0;
    try {
    	logStep("update container list is started.");
    	if(isSubutaiAvailable)
    	{
    		logStep("Find container list via subutai list -c");
    		subutai_containers = getContainers();
    	}
    	logStep("Find containers via lxc api.");
        num = list_all_containers(_lxc_path.c_str(), &names, &cont);
    	logStep("Container is listed i: " + _helper.toString(size_of_containers));
        bool destroy_container_check[size_of_containers];
    	logStep("array initialized");
        /* hold destroy container check array to control which container is deleted. */
        for (int i = 0; i < size_of_containers; i++)
        {
        	logStep("False is set.");
        	destroy_container_check[i] = false;
        }
        logStep("Container deletion map is initialized.");
        for (int i = 0; i < num; i++) {
        	if( !checkIfTemplate(names[i], subutai_containers, isSubutaiAvailable) )
        	{
				// Check is there is any new container appears
				bool containerFound = false;
				index = 0;
				for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++) {
					if ((*it)->getContainerHostnameValue() == string(names[i])) {
				        logStep( (*it)->getContainerHostnameValue() + "is found in the list. No need to add it again");
						containerFound = true;
						destroy_container_check[index] = true;
						break;
					}
					index++;
				}
				if (!containerFound) {
					SubutaiContainer* c = new SubutaiContainer(_logger, cont[i]);
					c->setContainerHostname(names[i]);
			        logStep("Adding " + c->getContainerHostnameValue());
					_logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", c->getContainerHostnameValue() + " added.."));
					_containers.push_back(c);
				}
			}
        }


        for (int i = size_of_containers-1; i >= 0; i--) {
        	/*if this container is destroyed, clean both containerIdList and containers array on containerManager*/
        	if(!destroy_container_check[i])
        	{
		        logStep(_containers.at(i) -> getContainerHostnameValue() + " will be deleted from lists.");
        		_logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Starting removal operation of container."));
        		_logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Deleting container info from containerIdList."));
        		deleteContainerInfo((_containers.at(i))->getContainerHostnameValue());
        		_logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Erasing container from container vector."));
		        logStep("Trying to erase container: " + _containers.at(i) -> getContainerHostnameValue());
        		_containers.erase (_containers.begin()+i);
		        logStep(_containers.at(i) -> getContainerHostnameValue() + " is erased from vector.");
        		_logger->writeLog(7, _logger->setLogData("<SubutaiContainerManager>", "Finished removal operation of container."));
        	}
        }
    } catch (SubutaiException e) {
        _logger->writeLog(3, _logger->setLogData("<SubutaiContainerManager>", e.displayText()));
    } catch (std::exception e) {
        _logger->writeLog(3, _logger->setLogData("<SubutaiContainerManager>", string(e.what())));
    }
    logStep("Update container informations for all");
    for (ContainerIterator it = _containers.begin(); it != _containers.end(); it++) {
    	/*If new container is added, containerIdList is updated in getContainerId() method*/
        logStep("Updating " + (*it)->getContainerHostnameValue());
        (*it)->getContainerAllFields();
    }
}

/**
 * \details 	get all containers of resource host
 */
vector<SubutaiContainer*> SubutaiContainerManager::getAllContainers()
{
    return _containers;
}


