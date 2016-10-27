APP=subutai
CC=go
VERSION=4.0.4-SNAPSHOT
COMMIT=$(shell git rev-parse HEAD)
LDFLAGS=-ldflags "-r /apps/subutai/current/lib -w -s -X main.version=${VERSION} -X main.commit=${COMMIT}"

all:	
	$(CC) build ${LDFLAGS} -o $(APP)
