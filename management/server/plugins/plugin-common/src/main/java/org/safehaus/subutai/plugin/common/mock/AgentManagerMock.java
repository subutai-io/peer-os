package org.safehaus.subutai.plugin.common.mock;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentListener;
import org.safehaus.subutai.core.agent.api.AgentManager;


public class AgentManagerMock implements AgentManager
{

    @Override
    public Set<Agent> getAgents()
    {
        return null;
    }


    @Override
    public Set<Agent> getPhysicalAgents()
    {
        return null;
    }


    @Override
    public Set<Agent> getLxcAgents()
    {
        return null;
    }


    @Override
    public Agent getAgentByHostname( String hostname )
    {
        return null;
    }


    @Override
    public Agent getAgentByUUID( UUID uuid )
    {
        return null;
    }


    @Override
    public Set<Agent> getLxcAgentsByParentHostname( String parentHostname )
    {
        return null;
    }


    @Override
    public void addListener( AgentListener listener )
    {

    }


    @Override
    public void removeListener( AgentListener listener )
    {

    }

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
