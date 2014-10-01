package org.safehaus.subutai.core.agent.cli;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.agent.api.AgentManager;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Preconditions;


/**
 * Displays agent details searching by agents' id
 */
@Command( scope = "agent", name = "get-agent-by-uuid", description = "get agent by uuid" )
public class GetAgentByUUIDCommand extends OsgiCommandSupport
{

    @Argument( index = 0, name = "uuid", required = true, multiValued = false, description = "agent uuid" )
    String uuid;

    private final AgentManager agentManager;


    public void setUuid( final String uuid )
    {
        this.uuid = uuid;
    }


    public GetAgentByUUIDCommand( final AgentManager agentManager )
    {
        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );

        this.agentManager = agentManager;
    }


    protected Object doExecute()
    {

        UUID agentId = UUID.fromString( uuid );
        Agent agent = agentManager.getAgentByUUID( agentId );
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
