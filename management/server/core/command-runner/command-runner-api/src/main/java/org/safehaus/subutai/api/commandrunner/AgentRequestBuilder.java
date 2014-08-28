/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.commandrunner;


import org.safehaus.subutai.shared.protocol.Agent;

import com.google.common.base.Preconditions;


/**
 * Represents command to agent. This class is used when for each agent there is a specific custom command, not common to
 * all agents
 */
public class AgentRequestBuilder extends RequestBuilder {

    //target agent
    private final Agent agent;


    /**
     * Constructor
     *
     * @param agent - target agent
     * @param command - command to run
     */
    public AgentRequestBuilder( Agent agent, String command ) {
        super( command );
        Preconditions.checkNotNull( agent, "Agent is null" );

        this.agent = agent;
    }


    /**
     * Returns target agent
     *
     * @return - agent
     */
    public Agent getAgent() {
        return agent;
    }
}
