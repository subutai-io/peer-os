package org.safehaus.kiskis.mgmt.shared.protocol.elements;

import java.io.Serializable;
import java.util.Map;

/**
 * It describes an action to execute against certain {@link Request} received
 */
public class Request implements Serializable {
    private RequestType type;
    private String uuid;
    private String macAddress;
    private Long requestSequenceNumber;
    private String workingDirectory;
    private String program;
    private OutputRedirection stdOut;
    private OutputRedirection stdErr;
    private String runAs;
    private Map<String, String> environment;

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

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
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

    public String getRunAs() {
        return runAs;
    }

    public void setRunAs(String runAs) {
        this.runAs = runAs;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    @Override
    public String toString() {
        return "Transportable{" + "type=" + type + ", uuid=" + uuid + ", macAddress=" + macAddress + ", requestSequenceNumber=" + requestSequenceNumber + ", workingDirectory=" + workingDirectory + ", program=" + program + ", stdOut=" + stdOut + ", stdErr=" + stdErr + ", runAs=" + runAs + ", environment=" + environment + '}';
    }
}
