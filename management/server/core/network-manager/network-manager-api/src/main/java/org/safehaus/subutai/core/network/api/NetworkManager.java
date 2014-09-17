package org.safehaus.subutai.core.network.api;


import java.util.List;

import org.safehaus.subutai.common.protocol.Agent;


public interface NetworkManager {
    public boolean configSshOnAgents( List<Agent> agentList );

    public boolean configSshOnAgents( List<Agent> agentList, Agent agent );

    public boolean configHostsOnAgents( List<Agent> agentList, String domainName );

    public boolean configHostsOnAgents( List<Agent> agentList, Agent agent, String domainName );
}
