package org.safehaus.subutai.core.agent.cli;


import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * Displays agent details searching by agents' container hostname
 */
@Command(scope = "agent", name = "get-agent-by-hostname", description = "get agent by hostname")
public class GetAgentByHostnameCommand extends OsgiCommandSupport
{

    @Argument(index = 0, name = "hostname", required = true, multiValued = false, description = "agent hostname")
    String hostname;
    private final AgentManager agentManager;


    public GetAgentByHostnameCommand( final AgentManager agentManager )
    {
        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );

        this.agentManager = agentManager;
    }


    public void setHostname( final String hostname )
    {
        this.hostname = hostname;
    }


    protected Object doExecute()
    {

        Agent agent = agentManager.getAgentByHostname( hostname );
        if ( agent != null )
        {
            System.out.println( agent );
        }
        else
        {
            System.out.println( "Agent not found" );
        }
        return null;
    }
}
