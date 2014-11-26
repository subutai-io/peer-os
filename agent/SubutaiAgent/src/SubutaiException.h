#ifndef __SUBUTAI_EXCEPTION_H__
#define __SUBUTAI_EXCEPTION_H__

#include <iostream>
#include <exception>
#include <string>

class SubutaiException : public std::exception {
    public:
        SubutaiException(const std::string msg);
        const char* what();
    private:
        std::string _msg;
};

#endif
