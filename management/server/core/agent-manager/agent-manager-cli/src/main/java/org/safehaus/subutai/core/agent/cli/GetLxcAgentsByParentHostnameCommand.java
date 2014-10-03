package org.safehaus.subutai.core.agent.cli;


import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * Displays list of lxc agents by host server hostname
 */
@Command( scope = "agent", name = "get-lxc-agents-by-parent-hostname",
        description = "get list of lxc agents  by host server hostname" )
public class GetLxcAgentsByParentHostnameCommand extends OsgiCommandSupport
{

    @Argument( index = 0, name = "parentHostname", required = true, multiValued = false,
            description = "parent hostname" )
    String parentHostname;

    private final AgentManager agentManager;


    public GetLxcAgentsByParentHostnameCommand( final AgentManager agentManager )
    {
        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );

        this.agentManager = agentManager;
    }


    public void setParentHostname( final String parentHostname )
    {
        this.parentHostname = parentHostname;
    }


    protected Object doExecute()
    {

        Set<Agent> agentSet = agentManager.getLxcAgentsByParentHostname( parentHostname );
        StringBuilder sb = new StringBuilder();
        for ( Agent agent : agentSet )
        {
            sb.append( agent.getHostname() ).append( "\n" );
        }
        System.out.println( sb.toString() );

        return null;
    }
}
