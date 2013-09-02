AUTOMAKE_OPTIONS = foreign
CPPFLAGS=-Wall -I/usr/local/include -DVERSION=\"$(VERSION)\" -DPROG="\"$(PACKAGE)\""
LDFLAGS=-L/usr/local/lib -lqpidmessaging -lpthread -lboost_thread -lqpidtypes
bin_PROGRAMS = DAEMON
DAEMON_SOURCES = DAEMON.cpp
LDADD = -L/usr/local/lib -lqpidmessaging -lpthread -lboost_thread -lqpidtypes
