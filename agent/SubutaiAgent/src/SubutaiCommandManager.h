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
 *  @brief     SubutaiCommandManager.h
 *  @class     SubutaiCommandManager
 *  @details   SubutaiCommandManager keeps commands and message responses
 *  @author    Mikhail Savochkin
 *  @version   1.1.0
 *  @date      Nov 18, 2014
 */
#ifndef __SUBUTAI_COMMAND_MANAGER_H__
#define __SUBUTAI_COMMAND_MANAGER_H__

#include <iostream>
#include <cstdio>
#include <deque>
#include <string>
#include "SubutaiCommand.h"

using namespace std;

class SubutaiCommandManager {
    public:
        ~SubutaiCommandManager();
        static SubutaiCommandManager*   getInstance();
        
        void addCommand(SubutaiCommand* command);
        void addMessage(string message);
        SubutaiCommand* currentCommand();
        bool nextCommand();
        SubutaiCommand* firstCommand();
        void finishCommand(string commandId);
        
    protected:
        SubutaiCommandManager();
        static SubutaiCommandManager*   _instance;
    private:
        deque<SubutaiCommand*>         _commands;
        deque<SubutaiCommand*>::iterator _itCommands;
        deque<string>                  _messages;
};

#endif
