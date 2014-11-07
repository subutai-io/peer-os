package org.safehaus.subutai.core.agentregistry.impl;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agentregistry.api.AgentListener;
import org.safehaus.subutai.core.agentregistry.api.AgentRegistry;


/**
 * Implementation of AgentRegistry
 */
public class AgentRegistryImpl implements AgentRegistry
{
    //TODO use ExpiringCache to notify on agent expiration


    @Override
    public Agent getAgentById( final UUID id )
    {
        return null;
    }


    @Override
    public Agent getAgentByHostname( final String hostname )
    {
        return null;
    }


    @Override
    public Set<Agent> getAgents()
    {
        return null;
    }


    @Override
    public void addAgentListener( final AgentListener listener )
    {

    }


    @Override
    public void removeAgentListener( final AgentListener listener )
    {

    }
}
