/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskismgmt.protocol;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Dilshat
 */
public class Request {

    private String source;
    private RequestType type;
    private String uuid;
    private Long requestSequenceNumber;
    private String workingDirectory;
    private String program;
    private OutputRedirection stdOut;
    private OutputRedirection stdErr;
    private String stdOutPath;
    private String stdErrPath;
    private String runAs;
    private List<String> args;
    private Map<String, String> environment;

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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getRequestSequenceNumber() {
        return requestSequenceNumber;
    }

    public void setRequestSequenceNumber(Long requestSequenceNumber) {
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

    @Override
    public String toString() {
        return "Request{" + "source=" + source + ", type=" + type + ", uuid=" + uuid + ", requestSequenceNumber=" + requestSequenceNumber + ", workingDirectory=" + workingDirectory + ", program=" + program + ", stdOut=" + stdOut + ", stdErr=" + stdErr + ", stdOutPath=" + stdOutPath + ", stdErrPath=" + stdErrPath + ", runAs=" + runAs + ", args=" + args + ", environment=" + environment + '}';
    }
}
