package org.safehaus.subutai.core.agent.cli;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Displays the last log entries
 */
@Command( scope = "agent", name = "get-agent-by-uuid", description = "get agent by uuid" )
public class GetAgentByUUIDCommand extends OsgiCommandSupport
{

    @Argument( index = 0, name = "uuid", required = true, multiValued = false, description = "agent UUID" )
    String uuid;
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

        Agent agent = agentManager.getAgentByUUID( UUID.fromString( uuid ) );
        StringBuilder sb = new StringBuilder();
        sb.append( "Hostname: " ).append( agent.getHostname() ).append( "\n" );
        for ( String ip : agent.getListIP() )
        {
            sb.append( "IP: " ).append( ip ).append( "\n" );
        }
        sb.append( "MAC address: " ).append( agent.getMacAddress() ).append( "\n" );
        sb.append( "Parent hostname: " ).append( agent.getParentHostName() ).append( "\n" );
        sb.append( "Transport ID: " ).append( agent.getTransportId() ).append( "\n" );
        sb.append( "UUID: " ).append( agent.getUuid() ).append( "\n" );
        System.out.println( sb.toString() );
        return null;
    }
}
