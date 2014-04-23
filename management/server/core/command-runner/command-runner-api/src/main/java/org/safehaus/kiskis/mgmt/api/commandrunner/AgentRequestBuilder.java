/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.commandrunner;

import com.google.common.base.Preconditions;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 *
 * @author dilshat
 */
public class AgentRequestBuilder extends RequestBuilder {

    private final Agent agent;

    public AgentRequestBuilder(Agent agent, String command) {
        super(command);
        Preconditions.checkNotNull(agent, "Agent is null");

        this.agent = agent;
    }

    public Agent getAgent() {
        return agent;
    }

}
