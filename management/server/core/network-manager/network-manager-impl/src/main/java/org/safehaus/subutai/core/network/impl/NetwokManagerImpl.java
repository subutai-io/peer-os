package org.safehaus.subutai.core.network.impl;


import java.util.List;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.network.api.NetworkManager;

import com.google.common.base.Preconditions;


/**
 * Created by daralbaev on 04.04.14.
 */
public class NetwokManagerImpl implements NetworkManager {
    private final Commands commands;


    public NetwokManagerImpl( final CommandRunner commandRunner ) {
        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );

        this.commands = new Commands( commandRunner );
    }


    @Override
    public boolean configSshOnAgents( List<Agent> agentList ) {
        return new SshManager( commands, agentList ).execute();
    }


    @Override
    public boolean configSshOnAgents( List<Agent> agentList, Agent agent ) {
        return new SshManager( commands, agentList ).execute( agent );
    }


    @Override
    public boolean configHostsOnAgents( List<Agent> agentList, String domainName ) {
        return new HostManager( commands, agentList, domainName ).execute();
    }


    @Override
    public boolean configHostsOnAgents( List<Agent> agentList, Agent agent, String domainName ) {
        return new HostManager( commands, agentList, domainName ).execute( agent );
    }
}
