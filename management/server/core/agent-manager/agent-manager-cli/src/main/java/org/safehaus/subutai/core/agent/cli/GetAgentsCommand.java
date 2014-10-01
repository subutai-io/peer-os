package org.safehaus.subutai.core.agent.cli;


import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * Displays list of connected agents
 */
@Command( scope = "agent", name = "get-agents", description = "get list of all agents" )
public class GetAgentsCommand extends OsgiCommandSupport
{

    private final AgentManager agentManager;


    public GetAgentsCommand( final AgentManager agentManager )
    {
        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );

        this.agentManager = agentManager;
    }


    protected Object doExecute()
    {
        Set<Agent> agentSet = agentManager.getAgents();
        StringBuilder sb = new StringBuilder();
        for ( Agent agent : agentSet )
        {
            sb.append( agent.getHostname() ).append( "\n" );
        }

        System.out.println( sb.toString() );

        return null;
    }
}
