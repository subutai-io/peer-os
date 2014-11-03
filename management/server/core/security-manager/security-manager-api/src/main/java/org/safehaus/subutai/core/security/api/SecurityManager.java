package org.safehaus.subutai.core.security.api;


import java.util.Set;

import org.safehaus.subutai.core.peer.api.ContainerHost;


public interface SecurityManager
{
    public boolean configSshOnAgents( Set<ContainerHost> containers ) throws SecurityManagerException;

    public boolean configSshOnAgents( Set<ContainerHost> containerHosts, ContainerHost containerHost ) throws
            SecurityManagerException;

    public boolean configHostsOnAgents( Set<ContainerHost> containerHosts, String domainName ) throws
            SecurityManagerException;

}

