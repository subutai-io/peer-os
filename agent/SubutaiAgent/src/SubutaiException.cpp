#include "SubutaiException.h"

SubutaiException::SubutaiException(const std::string msg) : _msg(msg) {

}

const char* SubutaiException::what() {
    return _msg.c_str();
}
