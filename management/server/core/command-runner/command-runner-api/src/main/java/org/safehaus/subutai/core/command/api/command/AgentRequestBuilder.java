/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.command.api.command;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.command.RequestBuilder;

import com.google.common.base.Preconditions;


/**
 * Represents command to agent. This class is used when for each agent there is a specific custom command, not common to
 * all agents
 */
public class AgentRequestBuilder extends RequestBuilder
{

    //target agent
    private final Agent agent;


    /**
     * Constructor
     *
     * @param agent - target agent
     * @param command - command to run
     */
    public AgentRequestBuilder( Agent agent, String command )
    {
        super( command );
        Preconditions.checkNotNull( agent, "Agent is null" );

        this.agent = agent;
    }


    /**
     * Returns target agent
     *
     * @return - agent
     */
    public Agent getAgent()
    {
        return agent;
    }


    public Request build( final UUID taskUUID )
    {
        return super.build( agent.getUuid(), taskUUID );
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof AgentRequestBuilder ) )
        {
            return false;
        }
        if ( !super.equals( o ) )
        {
            return false;
        }

        final AgentRequestBuilder that = ( AgentRequestBuilder ) o;

        return agent.equals( that.agent );
    }


    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + agent.hashCode();
        return result;
    }
}
