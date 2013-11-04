/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskismgmt.protocol;

/**
 *
 * @author Dilshat
 */
public class Response {

    private String source;
    private ResponseType type;
    private Integer exitCode; //might be null if not final response chunk
    private String uuid;
    private Long requestSequenceNumber;
    private Long responseSequenceNumber;
    private String stdOut;
    private String stdErr;
    private Integer pid;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ResponseType getType() {
        return type;
    }

    public void setType(ResponseType type) {
        this.type = type;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
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

    public Long getResponseSequenceNumber() {
        return responseSequenceNumber;
    }

    public void setResponseSequenceNumber(Long responseSequenceNumber) {
        this.responseSequenceNumber = responseSequenceNumber;
    }

    public String getStdOut() {
        return stdOut;
    }

    public void setStdOut(String stdOut) {
        this.stdOut = stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    public void setStdErr(String stdErr) {
        this.stdErr = stdErr;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    @Override
    public String toString() {
        return "Response{" + "source=" + source + ", type=" + type + ", exitCode=" + exitCode + ", uuid=" + uuid + ", requestSequenceNumber=" + requestSequenceNumber + ", responseSequenceNumber=" + responseSequenceNumber + ", stdOut=" + stdOut + ", stdErr=" + stdErr + ", pid=" + pid + '}';
    }
}
