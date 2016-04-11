package executer

import (
	"bufio"
	"bytes"
	"os"
	"os/exec"
	"os/user"
	"strconv"
	"strings"
	"sync"
	"syscall"
	"time"
	"unsafe"

	"gopkg.in/lxc/go-lxc.v2"

	"github.com/subutai-io/base/agent/agent/container"
	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/log"
)

type EncRequest struct {
	HostId  string `json:"hostid"`
	Request string `json:"request"`
}
type Request struct {
	Id      string         `json:"id"`
	Request RequestOptions `json:"request"`
}

type RequestOptions struct {
	Type        string            `json:"type"`
	Id          string            `json:"id"`
	CommandId   string            `json:"commandId"`
	WorkingDir  string            `json:"workingDirectory"`
	Command     string            `json:"command"`
	Args        []string          `json:"args"`
	Environment map[string]string `json:"environment"`
	StdOut      string            `json:"stdOut"`
	StdErr      string            `json:"stdErr"`
	RunAs       string            `json:"runAs"`
	Timeout     int               `json:"timeout"`
	IsDaemon    int               `json:"isDaemon"`
}

type Response struct {
	ResponseOpts ResponseOptions `json:"response"`
	Id           string          `json:"id"`
}

type ResponseOptions struct {
	Type           string `json:"type"`
	Id             string `json:"id"`
	CommandId      string `json:"commandId"`
	Pid            int    `json:"pid"`
	ResponseNumber int    `json:"responseNumber,omitempty"`
	StdOut         string `json:"stdOut,omitempty"`
	StdErr         string `json:"stdErr,omitempty"`
	ExitCode       string `json:"exitCode,omitempty"`
}

func ExecHost(req RequestOptions, out_c chan<- ResponseOptions) {
	cmd := buildCmd(&req)
	if cmd == nil {
		close(out_c)
		return
	}
	rop, wop, _ := os.Pipe()
	rep, wep, _ := os.Pipe()
	defer rop.Close()
	defer rep.Close()

	cmd.Stdout = wop
	cmd.Stderr = wep
	if req.IsDaemon == 1 {
		cmd.SysProcAttr = &syscall.SysProcAttr{}
		cmd.SysProcAttr.Setpgid = true
		cmd.SysProcAttr.Credential = &syscall.Credential{Uid: 0, Gid: 0}
	}
	err := cmd.Start()
	log.Check(log.WarnLevel, "Executing command: "+req.CommandId+" "+req.Command+" "+strings.Join(req.Args, " "), err)

	wop.Close()
	wep.Close()

	stdout := make(chan string)
	stderr := make(chan string)
	go outputReader(rop, stdout)
	go outputReader(rep, stderr)

	var response = genericResponse(req)
	var wg sync.WaitGroup
	wg.Add(1)
	go func() {
		defer wg.Done()
		outputSender(stdout, stderr, out_c, &response)
	}()

	done := make(chan error)
	go func() { done <- cmd.Wait() }()
	select {
	case <-done:
		wg.Wait()
		response.ExitCode = "0"
		if req.IsDaemon != 1 {
			response.ExitCode = strconv.Itoa(cmd.ProcessState.Sys().(syscall.WaitStatus).ExitStatus())
		}
		out_c <- response
	case <-time.After(time.Duration(req.Timeout) * time.Second):
		if req.IsDaemon == 1 {
			response.ExitCode = "0"
			out_c <- response
			<-done
		} else {
			cmd.Process.Kill()
			response.Type = "EXECUTE_TIMEOUT"
			cmd.Process.Wait()
			if cmd.ProcessState != nil {
				response.ExitCode = strconv.Itoa(cmd.ProcessState.Sys().(syscall.WaitStatus).ExitStatus())
			} else {
				response.ExitCode = "-1"
			}
			out_c <- response
		}
	}
	close(out_c)
}

func outputReader(read *os.File, ch chan<- string) {
	r := bufio.NewReader(read)
	for line, isPrefix, err := r.ReadLine(); err == nil; line, isPrefix, err = r.ReadLine() {
		if isPrefix {
			ch <- string(line)
		} else {
			ch <- string(line) + "\n"
		}
	}
	close(ch)
}

func outputSender(stdout, stderr chan string, ch chan<- ResponseOptions, response *ResponseOptions) {
	for stdout != nil || stderr != nil {
		alive := false
		select {
		case buf, ok := <-stdout:
			response.StdOut = response.StdOut + buf
			if !ok {
				stdout = nil
			}
		case buf, ok := <-stderr:
			response.StdErr = response.StdErr + buf
			if !ok {
				stderr = nil
			}
		case <-time.After(time.Second * 10):
			alive = true
		}
		if len(response.StdOut) > 50000 || len(response.StdErr) > 50000 || alive {
			ch <- *response
			response.StdErr, response.StdOut = "", ""
			response.ResponseNumber++
		}
	}
}

func buildCmd(r *RequestOptions) *exec.Cmd {
	user, err := user.Lookup(r.RunAs)
	if log.Check(log.WarnLevel, "User lookup: "+r.RunAs, err) {
		return nil
	}
	uid, err := strconv.Atoi(user.Uid)
	if log.Check(log.WarnLevel, "UID lookup: "+user.Uid, err) {
		return nil
	}
	gid, err := strconv.Atoi(user.Gid)
	if log.Check(log.WarnLevel, "GID lookup: "+user.Gid, err) {
		return nil
	}
	gid32 := *(*uint32)(unsafe.Pointer(&gid))
	uid32 := *(*uint32)(unsafe.Pointer(&uid))

	var buff bytes.Buffer
	buff.WriteString(r.Command + " ")
	for _, arg := range r.Args {
		buff.WriteString(arg + " ")
	}
	cmd := exec.Command("/bin/bash", "-c", buff.String())
	cmd.Dir = r.WorkingDir
	cmd.SysProcAttr = &syscall.SysProcAttr{}
	cmd.SysProcAttr.Credential = &syscall.Credential{Uid: uid32, Gid: gid32}

	return cmd
}

//prepare basic response
func genericResponse(req RequestOptions) ResponseOptions {
	return ResponseOptions{
		Type:           "EXECUTE_RESPONSE",
		CommandId:      req.CommandId,
		Id:             req.Id,
		ResponseNumber: 1,
	}
}

func AttachContainer(name string, req RequestOptions, out_c chan<- ResponseOptions) {
	lxc_c, _ := lxc.NewContainer(name, config.Agent.LxcPrefix)

	rop, wop, _ := os.Pipe()
	rep, wep, _ := os.Pipe()
	defer rop.Close()
	defer rep.Close()

	opts := lxc.DefaultAttachOptions
	opts.UID, opts.GID = container.GetCredentials(req.RunAs, name)
	opts.StdoutFd = wop.Fd()
	opts.StderrFd = wep.Fd()
	opts.Cwd = req.WorkingDir

	var exitCode int
	var cmd bytes.Buffer

	cmd.WriteString(req.Command)
	for _, a := range req.Args {
		cmd.WriteString(a + " ")
	}

	log.Debug("Executing command in container " + name + ":" + cmd.String())
	go func() {
		exitCode, _ = lxc_c.RunCommandStatus([]string{"timeout", strconv.Itoa(req.Timeout), "/bin/bash", "-c", cmd.String()}, opts)
		wop.Close()
		wep.Close()
	}()

	stdout := make(chan string)
	stderr := make(chan string)
	go outputReader(rop, stdout)
	go outputReader(rep, stderr)

	var response = genericResponse(req)
	outputSender(stdout, stderr, out_c, &response)
	if exitCode == 0 {
		response.Type = "EXECUTE_RESPONSE"
		response.ExitCode = strconv.Itoa(exitCode)
	} else {
		if exitCode/256 == 124 {
			response.Type = "EXECUTE_TIMEOUT"
		}
		response.ExitCode = strconv.Itoa(exitCode / 256)
	}
	out_c <- response
	lxc.Release(lxc_c)
	close(out_c)
}
