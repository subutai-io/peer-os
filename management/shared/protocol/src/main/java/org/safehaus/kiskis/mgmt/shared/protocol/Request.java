/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Dilshat
 */
public class Request implements Serializable {

    private String source = null;
    private RequestType type = null;
    private UUID uuid = null;
    private UUID taskUuid = null;
    private Integer requestSequenceNumber = null;
    private String workingDirectory = null;
    private String program = null;
    private OutputRedirection stdOut = null;
    private OutputRedirection stdErr = null;
    private String stdOutPath = null;
    private String stdErrPath = null;
    private String runAs = null;
    private List<String> args = null;
    private Map<String, String> environment = null;
    private Integer pid = null;
    private Integer timeout = 30;

    public UUID getTaskUuid() {
        return taskUuid;
    }

    public void setTaskUuid(UUID taskUuid) {
        this.taskUuid = taskUuid;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public RequestType getType() {
        return type;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Integer getRequestSequenceNumber() {
        return requestSequenceNumber;
    }

    public void setRequestSequenceNumber(Integer requestSequenceNumber) {
        this.requestSequenceNumber = requestSequenceNumber;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public OutputRedirection getStdOut() {
        return stdOut;
    }

    public void setStdOut(OutputRedirection stdOut) {
        this.stdOut = stdOut;
    }

    public OutputRedirection getStdErr() {
        return stdErr;
    }

    public void setStdErr(OutputRedirection stdErr) {
        this.stdErr = stdErr;
    }

    public String getStdOutPath() {
        return stdOutPath;
    }

    public void setStdOutPath(String stdOutPath) {
        this.stdOutPath = stdOutPath;
    }

    public String getStdErrPath() {
        return stdErrPath;
    }

    public void setStdErrPath(String stdErrPath) {
        this.stdErrPath = stdErrPath;
    }

    public String getRunAs() {
        return runAs;
    }

    public void setRunAs(String runAs) {
        this.runAs = runAs;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        if (timeout != null && timeout > 0) {
            this.timeout = timeout;
        }
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    @Override
    public String toString() {
        return "Request{" + "source=" + source + ", type=" + type + ", uuid=" + uuid + ", taskUuid=" + taskUuid + ", requestSequenceNumber=" + requestSequenceNumber + ", workingDirectory=" + workingDirectory + ", program=" + program + ", stdOut=" + stdOut + ", stdErr=" + stdErr + ", stdOutPath=" + stdOutPath + ", stdErrPath=" + stdErrPath + ", runAs=" + runAs + ", args=" + args + ", environment=" + environment + ", pid=" + pid + ", timeout=" + timeout + '}';
    }
}
