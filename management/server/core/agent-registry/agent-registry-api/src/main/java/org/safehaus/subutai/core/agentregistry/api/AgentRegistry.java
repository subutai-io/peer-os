package org.safehaus.subutai.core.agentregistry.api;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;


/**
 * Stores currently connected agents
 */
public interface AgentRegistry
{
    public Agent getAgentById( UUID id );

    public Agent getAgentByHostname( String hostname );

    public Set<Agent> getAgents();

    public void addAgentListener( AgentListener listener );

    public void removeAgentListener( AgentListener listener );
}
