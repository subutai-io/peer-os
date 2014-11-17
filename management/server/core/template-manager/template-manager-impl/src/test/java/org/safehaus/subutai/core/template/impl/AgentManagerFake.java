package org.safehaus.subutai.core.template.impl;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentListener;
import org.safehaus.subutai.core.agent.api.AgentManager;

import com.google.common.collect.Sets;


/**
 * Created by timur on 10/6/14.
 */
public class AgentManagerFake implements AgentManager
{
    Set<Agent> agents = new HashSet<>();


    public AgentManagerFake()
    {
        agents.add( MockUtils.getPhysicalAgent() );
        agents.add( MockUtils.getLxcAgent() );
    }


    @Override
    public Set<Agent> getAgents()
    {
        return agents;
    }


    @Override
    public Set<Agent> getPhysicalAgents()
    {
        return Sets.newHashSet( MockUtils.getPhysicalAgent() );
    }


    @Override
    public Set<Agent> getLxcAgents()
    {
        return Sets.newHashSet( MockUtils.getLxcAgent() );
    }


    @Override
    public Agent getAgentByHostname( final String hostname )
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
    public Agent getAgentByUUID( final UUID uuid )
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


    @Override
    public Set<Agent> getLxcAgentsByParentHostname( final String parentHostname )
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


    @Override
    public void addListener( final AgentListener listener )
    {

    }


    @Override
    public void removeListener( final AgentListener listener )
    {

    }
//
//
//    @Override
//    public Set<Agent> getAgentsByEnvironmentId( final UUID environmentId )
//    {
//        return null;
//    }


    @Override
    public Agent waitForRegistration( final String hostname, final long timeout )
    {
        return null;
    }


    @Override
    public Set<Agent> returnAgentsByGivenUUIDSet( final Set<UUID> agentUUIDs )
    {
        return null;
    }
}
