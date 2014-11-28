ACLOCAL_AMFLAGS = ${ACLOCAL_FLAGS}
AUTOMAKE_OPTIONS = foreign
CPPFLAGS=-Wall -DVERSION=\"$(VERSION)\" -DPROG="\"$(PACKAGE)\"" -I/var/lib/jenkins/AgentInstallation/mosquitto-1.3.1/lib
LFLAGS = --gc-sections
sbin_PROGRAMS = subutai-agent
subutai_agent_SOURCES = SubutaiAgent.cpp SubutaiHelper.h SubutaiHelper.cpp SubutaiThread.h SubutaiThread.cpp SubutaiStreamReader.h SubutaiStreamReader.cpp SubutaiResponsePack.h SubutaiResponsePack.cpp SubutaiConnection.h SubutaiConnection.cpp SubutaiCommand.h SubutaiCommand.cpp SubutaiContainer.h SubutaiContainer.cpp SubutaiContainerManager.h SubutaiContainerManager.cpp SubutaiTimer.h SubutaiTimer.cpp SubutaiUserID.h SubutaiUserID.cpp SubutaiResponse.h SubutaiResponse.cpp pugiconfig.hpp pugixml.hpp pugixml.cpp SubutaiLogger.h SubutaiLogger.cpp mosquittopp.h mosquittopp.cpp SubutaiWatch.h SubutaiWatch.cpp SubutaiEnvironment.h SubutaiEnvironment.cpp Message.pb.cpp Message.pb.h SubutaiException.h SubutaiException.cpp
subutai_agent_LDADD = /usr/lib/libboost_thread.a /usr/lib/libjson_linux-gcc-4.6_libmt.a /usr/lib/x86_64-linux-gnu/librt.a /usr/lib/libmosquittopp.a /usr/lib/libmosquitto.a /usr/lib/libcares.a /usr/lib/libboost_system.a /usr/lib/libprotobuf.a /usr/lib/libprotobuf-lite.a /usr/lib/libprotoc.a
