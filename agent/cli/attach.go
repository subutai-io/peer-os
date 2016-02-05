package lib

import (
	"github.com/subutai-io/Subutai/agent/config"
	"github.com/subutai-io/Subutai/agent/log"
	"gopkg.in/lxc/go-lxc.v2"
	"io"
	"os"
	"sync"
)

func LxcAttach(name string) {
	var wg sync.WaitGroup
	clear, x86, regular := false, false, false

	c, err := lxc.NewContainer(name, config.Agent.LxcPrefix)
	log.Check(log.ErrorLevel, "Creating container object", err)

	stdoutReader, stdoutWriter, err := os.Pipe()
	log.Check(log.ErrorLevel, "Connecting stdout pipe", err)

	stderrReader, stderrWriter, err := os.Pipe()
	log.Check(log.ErrorLevel, "Connecting stderr pipe", err)

	wg.Add(1)
	go func() {
		defer wg.Done()
		_, err = io.Copy(os.Stdout, stdoutReader)
		log.Check(log.ErrorLevel, "Writing stdout to channel", err)
	}()
	wg.Add(1)
	go func() {
		defer wg.Done()
		_, err = io.Copy(os.Stderr, stderrReader)
		log.Check(log.ErrorLevel, "Writing stderr to channel", err)
	}()

	options := lxc.DefaultAttachOptions

	options.StdinFd = os.Stdin.Fd()
	options.StdoutFd = stdoutWriter.Fd()
	options.StderrFd = stderrWriter.Fd()

	options.ClearEnv = false
	if clear {
		options.ClearEnv = true
	}

	if x86 {
		options.Arch = lxc.X86
	}
	if regular {
		options.UID = 1000
		options.GID = 1000
	}

	log.Check(log.ErrorLevel, "Attaching shell", c.AttachShell(options))

	log.Check(log.ErrorLevel, "Closing stdout", stdoutWriter.Close())
	log.Check(log.ErrorLevel, "Closing stderr", stderrWriter.Close())

	wg.Wait()
}
