/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.commandrunner;

import com.google.common.base.Strings;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.commandrunner.AgentResult;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

/**
 *
 * @author dilshat
 */
class AgentResultImpl implements AgentResult {

    private final UUID agentUUID;
    private final StringBuilder stdOut = new StringBuilder();
    private final StringBuilder stdErr = new StringBuilder();
    private Integer exitCode;

    public AgentResultImpl(UUID agentUUID) {
        this.agentUUID = agentUUID;
    }

    void appendResults(Response response) {
        if (response != null && exitCode == null) {
            if (!Strings.isNullOrEmpty(response.getStdOut())) {
                stdOut.append(response.getStdOut());
            }
            if (!Strings.isNullOrEmpty(response.getStdErr())) {
                stdOut.append(response.getStdErr());
            }
            if (response.isFinal() && response.getExitCode() != null) {
                exitCode = response.getExitCode();
            }
        }
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public String getStdOut() {
        return stdOut.toString();
    }

    public String getStdErr() {
        return stdErr.toString();
    }

    public UUID getAgentUUID() {
        return agentUUID;
    }

}
