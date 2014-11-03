package org.safehaus.subutai.core.security.impl;


import java.util.Set;

import org.safehaus.subutai.core.security.api.SecurityManager;
import org.safehaus.subutai.core.security.api.SecurityManagerException;
import org.safehaus.subutai.core.peer.api.ContainerHost;


/**
 * Implementation of Network Manager
 */
public class SecurityManagerImpl implements SecurityManager
{


    @Override
    public boolean configSshOnAgents( Set<ContainerHost> containerHosts ) throws SecurityManagerException
    {
        try
        {
            return new SshManager( containerHosts ).execute();
        }
        catch ( SSHManagerException e )
        {
            throw new SecurityManagerException( e.getMessage() );
        }
    }


    @Override
    public boolean configSshOnAgents( Set<ContainerHost> containerHosts, ContainerHost containerHost )
            throws SecurityManagerException
    {
        try
        {
            return new SshManager( containerHosts ).execute( containerHost );
        }
        catch ( SSHManagerException e )
        {
            throw new SecurityManagerException( e.getMessage() );
        }
    }


    @Override
    public boolean configHostsOnAgents( Set<ContainerHost> containerHosts, String domainName )
            throws SecurityManagerException
    {
        try
        {
            return new HostManager( containerHosts, domainName ).execute();
        }
        catch ( HostManagerException e )
        {
            throw new SecurityManagerException( e.getMessage() );
        }
    }
}
