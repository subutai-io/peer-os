package org.safehaus.subutai.core.network.impl;


import java.util.List;
import java.util.Set;

import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;
import org.safehaus.subutai.core.network.api.NetworkManager;
import org.safehaus.subutai.core.peer.api.ContainerHost;

import com.google.common.base.Preconditions;


/**
 * Implementation of Network Manager
 */
public class NetworkManagerImpl implements NetworkManager
{


    public NetworkManagerImpl( final CommandDispatcher commandDispatcher )
    {
        Preconditions.checkNotNull( commandDispatcher, "Command Dispatcher is null" );
    }


    @Override
    public boolean configSshOnAgents( Set<ContainerHost> containerHosts )
    {
        return new SshManager( containerHosts ).execute();
    }


    @Override
    public boolean configSshOnAgents( Set<ContainerHost> containerHosts, ContainerHost containerHost )
    {
        return new SshManager( containerHosts ).execute( containerHost );
    }
    @Override
    public boolean configHostsOnAgents( Set<ContainerHost> containerHosts, String domainName )
    {
        return new HostManager( containerHosts, domainName ).execute();
    }
}
