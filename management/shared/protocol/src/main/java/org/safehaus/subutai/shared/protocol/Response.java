/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.shared.protocol;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.safehaus.subutai.shared.protocol.enums.ResponseType;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * @author Dilshat
 */
public class Response implements Serializable {

    private String source;
    private ResponseType type;
    private Integer exitCode; //might be null if not final response chunk
    private UUID uuid;
    private UUID taskUuid;
    private Integer requestSequenceNumber;
    private Integer responseSequenceNumber;
    private String stdOut;
    private String stdErr;
    private Integer pid;
    private String macAddress;
    private String hostname;
    private String parentHostName;
    private List<String> ips;
    private Boolean isLxc;
    private String transportId;

    public String getParentHostName() {
        return parentHostName;
    }

    public String getTransportId() {
        return transportId;
    }

    public void setTransportId(String transportId) {

        Preconditions.checkArgument(!Strings.isNullOrEmpty(transportId), "Transport id is null or empty");

        this.transportId = transportId;
    }

    public Boolean isIsLxc() {
        return isLxc;
    }

    public List<String> getIps() {
        return ips;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public String getSource() {
        return source;
    }

    public ResponseType getType() {
        return type;
    }

    public void setType(ResponseType type) {

        Preconditions.checkNotNull(type, "Response type is null");

        this.type = type;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Integer getRequestSequenceNumber() {
        return requestSequenceNumber;
    }

    public Integer getResponseSequenceNumber() {
        return responseSequenceNumber;
    }

    public String getStdOut() {
        return stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    public Integer getPid() {
        return pid;
    }

    public UUID getTaskUuid() {
        return taskUuid;
    }

    public boolean isFinal() {
        return ResponseType.EXECUTE_RESPONSE_DONE.equals(type)
                || ResponseType.EXECUTE_TIMEOUT.equals(type)
                || ResponseType.TERMINATE_RESPONSE_DONE.equals(type)
                || ResponseType.TERMINATE_RESPONSE_FAILED.equals(type);
    }

    public boolean hasSucceeded() {
        return (ResponseType.EXECUTE_RESPONSE_DONE.equals(type) || ResponseType.TERMINATE_RESPONSE_DONE.equals(type)) && exitCode != null && exitCode == 0;
    }

    @Override
    public String toString() {
        return "Response{" + "source=" + source + ", type=" + type + ", exitCode=" + exitCode + ", uuid=" + uuid + ", taskUuid=" + taskUuid + ", requestSequenceNumber=" + requestSequenceNumber + ", responseSequenceNumber=" + responseSequenceNumber + ", stdOut=" + stdOut + ", stdErr=" + stdErr + ", pid=" + pid + ", macAddress=" + macAddress + ", hostname=" + hostname + ", ips=" + ips + ", isLxc=" + isLxc + ", transportId=" + transportId + '}';
    }

}
