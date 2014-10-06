package org.safehaus.subutai.core.agent.cli;


import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * Displays list of lxc agents
 */
@Command( scope = "agent", name = "get-lxc-agents", description = "get list of lxc agents" )
public class GetLxcAgentsCommand extends OsgiCommandSupport
{

    private final AgentManager agentManager;


    public GetLxcAgentsCommand( final AgentManager agentManager )
    {
        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );

        this.agentManager = agentManager;
    }


    protected Object doExecute()
    {
        Set<Agent> agentSet = agentManager.getLxcAgents();
        StringBuilder sb = new StringBuilder();
        for ( Agent agent : agentSet )
        {
            sb.append( agent.getHostname() ).append( "\n" );
        }
        System.out.println( sb.toString() );

        return null;
    }
}
