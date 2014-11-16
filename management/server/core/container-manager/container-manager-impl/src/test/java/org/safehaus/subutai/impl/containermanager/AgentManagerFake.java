/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.containermanager;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentListener;
import org.safehaus.subutai.core.agent.api.AgentManager;

import com.google.common.collect.Sets;


/**
 * Agent Manager fake class
 */
public class AgentManagerFake implements AgentManager
{

    private final Set<Agent> agents = new HashSet<>();


    public AgentManagerFake()
    {
        agents.add( MockUtils.getPhysicalAgent() );
        agents.add( MockUtils.getLxcAgent() );
    }


    public Set<Agent> getAgents()
    {
        return Collections.unmodifiableSet( agents );
    }


    public Set<Agent> getPhysicalAgents()
    {
        return Sets.newHashSet( MockUtils.getPhysicalAgent() );
    }


    public Set<Agent> getLxcAgents()
    {
        return Sets.newHashSet( MockUtils.getLxcAgent() );
    }


    public Agent getAgentByHostname( String hostname )
    {

        for ( Agent agent : agents )
        {
            if ( agent.getHostname().equals( hostname ) )
            {
                return agent;
            }
        }

        return MockUtils.getLxcAgent();
    }


    public Agent getAgentByUUID( UUID uuid )
    {
        for ( Agent agent : agents )
        {
            if ( agent.getUuid().equals( uuid ) )
            {
                return agent;
            }
        }
        return null;
    }


    public Set<Agent> getLxcAgentsByParentHostname( String parentHostname )
    {
        Set<Agent> lxcAgents = new HashSet<>();
        for ( Agent agent : agents )
        {
            if ( agent.getParentHostName().equals( parentHostname ) )
            {
                lxcAgents.add( agent );
            }
        }
        return lxcAgents;
    }


    public void addListener( AgentListener listener )
    {
    }


    public void removeListener( AgentListener listener )
    {
    }


//    @Override
    //    public Set<Agent> getAgentsByEnvironmentId( UUID environmentId )
    //    {
    //        return null;
    //    }


    @Override
    public Agent waitForRegistration( final String hostname, final long timeout )
    {
        for ( Agent agent : agents )
        {
            if ( agent.getHostname().equals( hostname ) )
            {
                return agent;
            }
        }
        return null;
    }


    @Override
    public Set<Agent> returnAgentsByGivenUUIDSet( final Set<UUID> agentUUIDs )
    {
        return null;
    }
}
