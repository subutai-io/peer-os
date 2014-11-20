#include "SubutaiCommandManager.h"

SubutaiCommandManager* SubutaiCommandManager::_instance = NULL;

SubutaiCommandManager::SubutaiCommandManager() {

}

SubutaiCommandManager::~SubutaiCommandManager() {

}

SubutaiCommandManager* SubutaiCommandManager::getInstance() {
    if (!_instance) new SubutaiCommandManager();
    return _instance;
}

void SubutaiCommandManager::addCommand(SubutaiCommand* command) {
    _commands.push_back(command);
}

void SubutaiCommandManager::addMessage(string message) {
    _messages.push_back(message);
}

SubutaiCommand* SubutaiCommandManager::currentCommand() {
    return new SubutaiCommand();
}

bool SubutaiCommandManager::nextCommand() {
    if (_itCommands == _commands.end()) {
        return false;
    }
    _itCommands++;
    return true;
}

SubutaiCommand* SubutaiCommandManager::firstCommand() {
    _itCommands = _commands.begin();
    return _commands.front();
}

void SubutaiCommandManager::finishCommand(string commandId) {
    for (deque<SubutaiCommand*>::iterator it = _commands.begin(); it != _commands.end(); it++) {
        if (command)
    }
}
