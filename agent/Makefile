APP=subutai
CC=go
VERSION=4.0.3
LDFLAGS=-ldflags "-r /apps/subutai/current/lib -w -s -X main.Version=${VERSION}"

all:
	$(CC) build ${LDFLAGS} -o $(APP)

