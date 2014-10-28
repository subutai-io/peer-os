package org.safehaus.subutai.core.network.api;


import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.core.peer.api.ContainerHost;


public interface NetworkManager
{
    public boolean configSshOnAgents( List<Agent> agentList );

    public boolean configSshOnAgents( List<Agent> agentList, Agent agent );

    public boolean configHostsOnAgents( List<Agent> agentList, String domainName );

    public boolean configHostsOnAgents( List<Agent> agentList, Agent agent, String domainName );

    public boolean configHosts( String domainName, Set<Container> containers );

    public boolean configHosts( String domainName, Set<Container> containers, Container container );

    public boolean configSsh( Set<Container> containers );

    public boolean configSsh( Set<Container> containers, Container container );

    public boolean configSshHosts( Set<ContainerHost> containers );

    public boolean configLinkHosts( String domainName, Set<ContainerHost> containers );
}
