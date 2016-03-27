package executer

import (
	"bufio"
	"bytes"
	"github.com/subutai-io/base/agent/agent/container"
	"github.com/subutai-io/base/agent/config"
	"github.com/subutai-io/base/agent/log"
	"gopkg.in/lxc/go-lxc.v2"
	"io"
	"os"
	"os/exec"
	"os/user"
	"strconv"
	"strings"
	"sync"
	"syscall"
	"time"
	"unsafe"
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
	//read/write pipes stdout
	rop, wop, _ := os.Pipe()
	//read/out pipes stderr
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

	start := time.Now().Unix()
	end := time.Now().Add(time.Duration(req.Timeout) * time.Second).Unix()
	var response = genericResponse(&req)
	response.ResponseNumber = 1
	var wg sync.WaitGroup
	wg.Add(1)
	go func() {
		defer wg.Done()
		r := bufio.NewReader(rop)
		for line, isPrefix, err := r.ReadLine(); err == nil; line, isPrefix, err = r.ReadLine() {
			response.StdOut = response.StdOut + string(line)
			if !isPrefix {
				response.StdOut = response.StdOut + "\n"
			}
			if len(response.StdOut) > 50000 {
				out_c <- response
				start = now()
				response.StdErr, response.StdOut = "", ""
				response.ResponseNumber++
			}
			if end-now() < 0 {
				break
			}
		}
	}()

	wg.Add(1)
	go func() {
		defer wg.Done()
		scanErr := bufio.NewScanner(rep)
		for scanErr.Scan() {
			response.StdErr = response.StdErr + scanErr.Text() + "\n"
			if len(response.StdErr) > 50000 {
				out_c <- response
				start = now()
				response.StdErr, response.StdOut = "", ""
				response.ResponseNumber++
			}
			if end-now() < 0 {
				break
			}
		}
	}()

	wg.Add(1)
	go func() {
		defer wg.Done()
		for end-now() > 0 {
			if now()-start > 10 {
				out_c <- response
				start = now()
				response.StdErr, response.StdOut = "", ""
				response.ResponseNumber++
			}
			time.Sleep(time.Second)
		}
	}()

	done := make(chan error)
	go func() { done <- cmd.Wait() }()
	select {
	case <-done:
		response.ExitCode = "0"
		if req.IsDaemon != 1 {
			response.ExitCode = strconv.Itoa(cmd.ProcessState.Sys().(syscall.WaitStatus).ExitStatus())
		}
		out_c <- response
		end = -1
	case <-time.After(time.Duration(req.Timeout) * time.Second):
		if req.IsDaemon == 1 {
			response.ExitCode = "0"
			out_c <- response
			<-done
		} else {
			cmd.Process.Kill()
			response.Type = config.Broker.ExecuteTimeout
			cmd.Process.Wait()
			if cmd.ProcessState != nil {
				response.ExitCode = strconv.Itoa(cmd.ProcessState.Sys().(syscall.WaitStatus).ExitStatus())
			} else {
				response.ExitCode = "-1"
			}
			out_c <- response
		}
	}
	wg.Wait()
	close(out_c)
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
func genericResponse(req *RequestOptions) ResponseOptions {
	return ResponseOptions{
		Type:      config.Broker.ExecuteResponce,
		CommandId: req.CommandId,
		Id:        req.Id,
	}
}

func now() int64 {
	return time.Now().Unix()
}

func AttachContainer(name string, r RequestOptions, out_c chan<- ResponseOptions) {
	lxc_c, _ := lxc.NewContainer(name, config.Agent.LxcPrefix)
	opts := lxc.DefaultAttachOptions

	var res ResponseOptions = genericResponse(&r)

	o_read, o_write, _ := os.Pipe()
	e_read, e_write, _ := os.Pipe()

	var chunk bytes.Buffer
	defer o_read.Close()
	defer e_read.Close()

	opts.StdoutFd = o_write.Fd()
	opts.StderrFd = e_write.Fd()

	opts.UID, opts.GID = container.GetCredentials(r.RunAs, name)

	opts.Cwd = r.WorkingDir

	var exitCode int
	var cmd bytes.Buffer

	cmd.WriteString(r.Command)
	for _, a := range r.Args {
		cmd.WriteString(a + " ")
	}

	log.Debug("Executing command in container " + name + ":" + cmd.String())
	go func() {
		exitCode, _ = lxc_c.RunCommandStatus([]string{"timeout", strconv.Itoa(r.Timeout), "/bin/bash", "-c", cmd.String()}, opts)

		o_write.Close()
		e_write.Close()
	}()

	out := bufio.NewScanner(o_read)
	var e bytes.Buffer
	start_time := time.Now().Unix()

	res.ResponseNumber = 1
	for out.Scan() {
		//we have more stdout coming in
		//collect 1000 bytes and send the chunk
		chunk.WriteString(out.Text() + "\n")
		//send chunk every 1000 bytes or 10 seconds
		if chunk.Len() >= 1000 || now()-start_time >= 10 {
			res.StdOut = chunk.String()
			out_c <- res
			chunk.Truncate(0)
			res.ResponseNumber++
			start_time = now()
		}
	}

	io.Copy(&e, e_read)
	if exitCode == 0 {
		res.Type = config.Broker.ExecuteResponce
		res.ExitCode = strconv.Itoa(exitCode)
		if chunk.Len() > 0 {
			res.StdOut = chunk.String()
		}
		res.StdErr = e.String()
		out_c <- res
	} else {
		if exitCode/256 == 124 {
			res.Type = config.Broker.ExecuteTimeout
		}
		res.ExitCode = strconv.Itoa(exitCode / 256)
		res.StdOut = chunk.String()
		res.StdErr = e.String()
		out_c <- res
	}
	lxc.Release(lxc_c)
	close(out_c)
}
