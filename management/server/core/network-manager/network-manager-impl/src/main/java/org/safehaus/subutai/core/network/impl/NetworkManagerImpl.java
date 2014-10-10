package org.safehaus.subutai.core.network.impl;


import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;
import org.safehaus.subutai.core.network.api.NetworkManager;

import com.google.common.base.Preconditions;


/**
 * Implementation of Network Manager
 */
public class NetworkManagerImpl implements NetworkManager
{
    private final Commands commands;


    public NetworkManagerImpl( final CommandDispatcher commandDispatcher )
    {
        Preconditions.checkNotNull( commandDispatcher, "Command Dispatcher is null" );

        this.commands = new Commands( commandDispatcher );
    }


    @Override
    public boolean configSshOnAgents( List<Agent> agentList )
    {
        return new SshManager( commands, agentList ).execute();
    }


    @Override
    public boolean configSshOnAgents( List<Agent> agentList, Agent agent )
    {
        return new SshManager( commands, agentList ).execute( agent );
    }


    @Override
    public boolean configHostsOnAgents( List<Agent> agentList, String domainName )
    {
        return new HostManager( commands, agentList, domainName ).execute();
    }


    @Override
    public boolean configHostsOnAgents( List<Agent> agentList, Agent agent, String domainName )
    {
        return new HostManager( commands, agentList, domainName ).execute( agent );
    }


    @Override
    public boolean configHosts( String domainName, final Set<Container> containers )
    {
        return new HostManager( containers, domainName, commands ).execute();
    }


    @Override
    public boolean configHosts( String domainName, final Set<Container> containers, final Container container )
    {
        return new HostManager( containers, domainName, commands ).execute( container );
    }


    @Override
    public boolean configSsh( final Set<Container> containers )
    {
        return new SshManager( containers, commands ).execute();
    }


    @Override
    public boolean configSsh( final Set<Container> containers, final Container container )
    {
        return new SshManager( containers, commands ).execute( container );
    }
}
