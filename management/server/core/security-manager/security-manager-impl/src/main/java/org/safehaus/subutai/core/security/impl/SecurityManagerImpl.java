package org.safehaus.subutai.core.security.impl;


import java.util.Set;

import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.security.api.SecurityManager;
import org.safehaus.subutai.core.security.api.SecurityManagerException;


/**
 * Implementation of Network Manager
 */
public class SecurityManagerImpl implements SecurityManager
{


    @Override
    public void configSshOnAgents( Set<ContainerHost> containerHosts ) throws SecurityManagerException
    {
        try
        {
            new SshManager( containerHosts ).execute();
        }
        catch ( SSHManagerException e )
        {
            throw new SecurityManagerException( e );
        }
    }


    @Override
    public void addSshKeyToAuthorizedKeys( final String sshKey, Set<ContainerHost> containerHosts )
            throws SecurityManagerException
    {
        try
        {
            new SshManager( containerHosts ).append( sshKey );
        }
        catch ( SSHManagerException e )
        {
            throw new SecurityManagerException( e );
        }
    }


    @Override
    public void configSshOnAgents( Set<ContainerHost> containerHosts, ContainerHost containerHost )
            throws SecurityManagerException
    {
        try
        {
            new SshManager( containerHosts ).execute( containerHost );
        }
        catch ( SSHManagerException e )
        {
            throw new SecurityManagerException( e );
        }
    }


    @Override
    public void configHostsOnAgents( Set<ContainerHost> containerHosts, String domainName )
            throws SecurityManagerException
    {
        try
        {
            new HostManager( containerHosts, domainName ).execute();
        }
        catch ( HostManagerException e )
        {
            throw new SecurityManagerException( e );
        }
    }
}
