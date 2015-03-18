#include "SubutaiException.h"

SubutaiException::SubutaiException(const std::string msg) :
		_msg(msg), _code(-1) {
}

SubutaiException::SubutaiException(const std::string msg, int code) :
		_msg(msg), _code(code) {
}

SubutaiException::~SubutaiException() throw () {
}

const char* SubutaiException::what() const throw () {
	return _msg.c_str();
}

const std::string SubutaiException::displayText() {
	std::stringstream output;
	output << "SubutaiException: ";
	if (_code != 0) {
		output << _code << " ";
	}
	output << _msg;
	return output.str();
}

int SubutaiException::code() {
	return _code;
}
