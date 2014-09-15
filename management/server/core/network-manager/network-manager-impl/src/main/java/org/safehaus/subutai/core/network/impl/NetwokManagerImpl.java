package org.safehaus.subutai.core.network.impl;


import java.util.List;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.network.api.NetworkManager;


/**
 * Created by daralbaev on 04.04.14.
 */
public class NetwokManagerImpl implements NetworkManager {
    private CommandRunner commandRunner;


    public void setCommandRunner( CommandRunner commandRunner ) {
        this.commandRunner = commandRunner;
    }


    @Override
    public boolean configSshOnAgents( List<Agent> agentList ) {
        return new SshManager( commandRunner, agentList ).execute();
    }


    @Override
    public boolean configSshOnAgents( List<Agent> agentList, Agent agent ) {
        return new SshManager( commandRunner, agentList ).execute( agent );
    }


    @Override
    public boolean configHostsOnAgents( List<Agent> agentList, String domainName ) {
        return new HostManager( commandRunner, agentList, domainName ).execute();
    }


    @Override
    public boolean configHostsOnAgents( List<Agent> agentList, Agent agent, String domainName ) {
        return new HostManager( commandRunner, agentList, domainName ).execute( agent );
    }
}
