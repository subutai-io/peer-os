/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol;

import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

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
    public String macAddress;
    public String hostname;
    public List<String> ips;
    public Boolean isLxc;
    private String transportId;

    public String getTransportId() {
        return transportId;
    }

    public void setTransportId(String transportId) {
        this.transportId = transportId;
    }

    public Boolean isIsLxc() {
        return isLxc;
    }

    public void setIsLxc(Boolean isLxc) {
        this.isLxc = isLxc;
    }

    public List<String> getIps() {
        return ips;
    }

    public void setIps(List<String> ips) {
        this.ips = ips;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

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

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setTaskUuid(UUID taskUuid) {
        this.taskUuid = taskUuid;
    }

    public Integer getRequestSequenceNumber() {
        return requestSequenceNumber;
    }

    public void setRequestSequenceNumber(Integer requestSequenceNumber) {
        this.requestSequenceNumber = requestSequenceNumber;
    }

    public Integer getResponseSequenceNumber() {
        return responseSequenceNumber;
    }

    public void setResponseSequenceNumber(Integer responseSequenceNumber) {
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

    public UUID getTaskUuid() {
        return taskUuid;
    }

    @Override
    public String toString() {
        return "Response{" + "source=" + source + ", type=" + type + ", exitCode=" + exitCode + ", uuid=" + uuid + ", taskUuid=" + taskUuid + ", requestSequenceNumber=" + requestSequenceNumber + ", responseSequenceNumber=" + responseSequenceNumber + ", stdOut=" + stdOut + ", stdErr=" + stdErr + ", pid=" + pid + ", macAddress=" + macAddress + ", hostname=" + hostname + ", ips=" + ips + ", isLxc=" + isLxc + ", transportId=" + transportId + '}';
    }

}
