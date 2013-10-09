package org.safehaus.kiskis.mgmt.shared.protocol.elements;

import java.io.Serializable;

/**
 * Defines a reply which is produced from executing a {@link Response}
 */
public class Response implements Serializable, Command {
    private ResponseType type;
    private Integer exitCode; //might be null if not final response chunk
    private String uuid;
    private Long requestSequenceNumber;
    private Long responseSequenceNumber;
    private String stdOut;
    private String stdErr;

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

    @Override
    public String toString() {
        return "Response{" + "type=" + type + ", exitCode=" + exitCode + ", uuid=" + uuid + ", requestSequenceNumber=" + requestSequenceNumber + ", responseSequenceNumber=" + responseSequenceNumber + ", stdOut=" + stdOut + ", stdErr=" + stdErr + '}';
    }
}
