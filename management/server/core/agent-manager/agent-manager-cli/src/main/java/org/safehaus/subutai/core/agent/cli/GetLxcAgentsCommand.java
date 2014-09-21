package org.safehaus.subutai.core.agent.cli;


import java.util.Set;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command( scope = "agent", name = "get-lxc-agents", description = "get lxc agents" )
public class GetLxcAgentsCommand extends OsgiCommandSupport
{

    private AgentManager agentManager;


    public AgentManager getAgentManager()
    {
        return agentManager;
    }


    public void setAgentManager( AgentManager agentManager )
    {
        this.agentManager = agentManager;
    }


    protected Object doExecute()
    {
        Set<Agent> agentSet = agentManager.getLxcAgents();
        StringBuilder sb = new StringBuilder();
        for ( Agent agent : agentSet )
        {
            sb.append( "Hostname: " ).append( agent.getHostname() ).append( " " ).append( "UUID: " )
              .append( agent.getUuid() ).append( " " ).append( "Parent hostname: " ).append( agent.getParentHostName() )
              .append( " " ).append( "MAC address: " ).append( agent.getMacAddress() ).append( " " ).append( "IPs: " )
              .append( agent.getListIP() ).append( " " ).append( "\n" );
        }

        System.out.println( sb.toString() );
        return null;
    }
}
