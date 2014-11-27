#ifndef __SUBUTAI_EXCEPTION_H__
#define __SUBUTAI_EXCEPTION_H__

#include <iostream>
#include <exception>
#include <string>
#include <sstream>

class SubutaiException : public std::exception {
    public:
        SubutaiException(const std::string msg);
        SubutaiException(const std::string msg, int code = 0);
        virtual                 ~SubutaiException() throw();
        virtual const char*     what() const throw();
        const std::string       displayText();
        int                     code();
    private:
        std::string             _msg;
        int                     _code;
};

#endif
