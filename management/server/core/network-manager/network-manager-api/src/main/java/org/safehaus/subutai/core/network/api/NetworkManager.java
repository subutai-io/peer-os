package org.safehaus.subutai.core.network.api;


import java.util.Set;

import org.safehaus.subutai.core.peer.api.ContainerHost;


public interface NetworkManager
{
    public boolean configSshOnAgents( Set<ContainerHost> containers );

    public boolean configSshOnAgents( Set<ContainerHost> containerHosts, ContainerHost containerHost );

    public boolean configHostsOnAgents( Set<ContainerHost> containerHosts, String domainName );

}

