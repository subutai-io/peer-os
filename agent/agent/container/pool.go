package container

import (
	"errors"
	fs "gopkg.in/fsnotify.v1"
	"strings"
	"subutai/config"
	cont "subutai/lib/container"
	"subutai/lib/gpg"
	"subutai/log"
	"time"
)

var instance *pool

type pool struct {
	uuid_name map[string]string
	name_uuid map[string]string
}

func init() {
	pl := PoolInstance()
	pl.Populate()
	pl.Watch(config.Agent.LxcPrefix)
}

var ErrHostNotFound = errors.New("Target host not found")

func PoolInstance() *pool {
	if instance == nil {
		instance = &pool{
			uuid_name: make(map[string]string),
			name_uuid: make(map[string]string),
		}
	}
	return instance
}

func (p *pool) GetTargetHostName(uuid string) (string, error) {
	name, ok := p.uuid_name[uuid]
	if ok {
		return name, nil
	}
	return "", ErrHostNotFound
}

func (p *pool) AddHost(uuid, name string) {
	p.uuid_name[uuid] = name
	p.name_uuid[name] = uuid
}

func (p *pool) RemoveHost(target string) {
	delete(p.uuid_name, target)
	delete(p.name_uuid, target)
}

func (p *pool) Watch(path string) {
	watcher, err := fs.NewWatcher()
	watcher.Add(path)
	log.Check(log.WarnLevel, "Creating watcher", err)
	go func() {
		for {
			select {
			case event := <-watcher.Events:
				//new container added
				if event.Op&fs.Create == fs.Create {
					contName := event.Name[strings.LastIndex(event.Name, "/")+1 : len(event.Name)]
					time.Sleep(time.Second * 15)
					uuid := gpg.GetFingerprint(contName)
					PoolInstance().AddHost(uuid, contName)
				}
				//delete from pool
				if event.Op&fs.Remove == fs.Remove {
					contName := event.Name[strings.LastIndex(event.Name, "/")+1 : len(event.Name)]
					PoolInstance().RemoveHost(contName)
				}
			case err := <-watcher.Errors:
				log.Error("Error on retrieval, " + err.Error())
			}
		}
	}()
}

func (p *pool) Populate() {
	conts := cont.Containers()
	for _, cont := range conts {
		uuid := gpg.GetFingerprint(cont)
		PoolInstance().AddHost(uuid, cont)
	}
}
