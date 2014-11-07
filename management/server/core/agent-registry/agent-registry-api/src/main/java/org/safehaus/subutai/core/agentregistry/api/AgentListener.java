package org.safehaus.subutai.core.agentregistry.api;


import org.safehaus.subutai.common.protocol.Agent;


/**
 * Listens to agent connections / disconnections
 */
public interface AgentListener
{
    public void onAgentConnect( Agent agent );

    public void onAgentDisconnect( Agent agent );
}
