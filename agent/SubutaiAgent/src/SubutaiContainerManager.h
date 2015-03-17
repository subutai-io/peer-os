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
 *    @copyright 2013 Safehaus.org
 */
/**
 *  @brief     SubutaiContainerManager.h
 *  @class     SubutaiContainerManager.h
 *  @details   Manages containers on current host, uses LXC API.
 *  @author    Mikhail Savochkin
 *  @author    Ozlem Ceren Sahin
 *  @version   1.1.0
 *  @date      Oct 31, 2014
 */
#ifndef __SUBUTAI_CONTAINER_MANAGER_H__
#define __SUBUTAI_CONTAINER_MANAGER_H__

#include <string>
#include <vector>
#include <lxc/lxccontainer.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>
#include <map>

#include "SubutaiLogger.h"
#include "SubutaiHelper.h"
#include "SubutaiContainer.h"
#include "SubutaiException.h"

using namespace std;

typedef std::vector<SubutaiContainer*>::iterator ContainerIterator;

class SubutaiContainerManager {
public:
	SubutaiContainerManager(string, SubutaiLogger*);
	~SubutaiContainerManager();
	vector<SubutaiContainer*> findAllContainers();
	SubutaiContainer* findContainerById(string);
	vector<SubutaiContainer*> getAllContainers();
	vector<string> getContainers();bool checkIfTemplate(string, vector<string>,
			bool);
	void updateContainerIdListOnStart();
	void deleteContainerInfo(string);
	void updateContainerLists();
protected:

private:
	string _lxc_path;
	SubutaiLogger* _logger;
	vector<SubutaiContainer*> _containers;
	SubutaiHelper _helper;
};

#endif
