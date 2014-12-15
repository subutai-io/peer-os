package org.safehaus.subutai.core.security.api;


import java.util.Set;

import org.safehaus.subutai.core.peer.api.ContainerHost;


public interface SecurityManager
{
    public void configSshOnAgents( Set<ContainerHost> containers ) throws SecurityManagerException;

    public void addSshKeyToAuthorizedKeys( String sshKey, Set<ContainerHost> containerHosts )
            throws SecurityManagerException;

    public void configSshOnAgents( Set<ContainerHost> containerHosts, ContainerHost containerHost )
            throws SecurityManagerException;

    public void configHostsOnAgents( Set<ContainerHost> containerHosts, String domainName )
            throws SecurityManagerException;
}

