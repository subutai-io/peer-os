APP=subutai
CC=go
TIMESTAMP=-$(shell date +%s)

$(APP): main.go
	$(CC) build -ldflags="-r /apps/subutai/current/lib -w -s -X main.TIMESTAMP=$(TIMESTAMP)" -o $@ $^

clean:
	-rm -f $(APP)
