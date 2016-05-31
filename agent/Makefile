APP=subutai
CC=go
VERSION=4.0.0-RC14-SNAPSHOT
LDFLAGS=-ldflags "-r /apps/subutai/current/lib -w -s -X main.Version=${VERSION}"

all:
	$(CC) build ${LDFLAGS} -o $(APP)

