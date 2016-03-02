APP=subutai
CC=go
PACK=upx
TIMESTAMP=-$(shell date +%s)

all: pack

$(APP): main.go
	$(CC) build -ldflags="-r /apps/subutai/current/lib -w -s -X main.TIMESTAMP=$(TIMESTAMP)" -o $@ -v $^

pack: clean $(APP)
	$(PACK) $(APP)

clean:
	-rm -f $(APP)
