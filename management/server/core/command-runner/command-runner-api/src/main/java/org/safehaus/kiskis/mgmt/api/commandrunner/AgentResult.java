/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.commandrunner;

import java.util.UUID;

/**
 *
 * @author dilshat
 */
public interface AgentResult {

    public Integer getExitCode();

    public String getStdOut();

    public String getStdErr();

    public UUID getAgentUUID();

}
