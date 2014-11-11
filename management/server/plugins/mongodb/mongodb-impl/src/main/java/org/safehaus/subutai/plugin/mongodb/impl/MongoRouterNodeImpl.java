package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.mongodb.api.MongoConfigNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoException;
import org.safehaus.subutai.plugin.mongodb.api.MongoRouterNode;
import org.safehaus.subutai.plugin.mongodb.api.Timeouts;
import org.safehaus.subutai.plugin.mongodb.impl.common.CommandDef;
import org.safehaus.subutai.plugin.mongodb.impl.common.Commands;


public class MongoRouterNodeImpl extends ContainerHost implements MongoRouterNode
{
    int routerPort;


    public MongoRouterNodeImpl( final Agent agent, final UUID peerId, final UUID environmentId, int routerPort )
    {
        super( agent, peerId, environmentId );
        this.routerPort = routerPort;
    }


    public void start( Set<MongoConfigNode> configServers, String domainName, int cfgSrvPort ) throws MongoException
    {
        CommandDef cmd = Commands.getStartRouterCommandLine( routerPort, cfgSrvPort, domainName, configServers );
        try
        {
            CommandResult commandResult = execute( cmd.build() );
            if ( !( commandResult.hasSucceeded() && commandResult.getStdOut().contains(
                    "child process started successfully, parent exiting" ) ) )
            {
                throw new MongoException( "Could not start mongo router node" );
            }
        }
        catch ( CommandException e )
        {
            throw new MongoException( "Could not start mongo router node:" + e.toString() );
        }
    }


    public void stop() throws MongoException
    {
        try
        {
            execute( new RequestBuilder( "/usr/bin/pkill -2 mongo" ).withTimeout( Timeouts.STOP_NODE_TIMEOUT_SEC ) );
        }
        catch ( CommandException e )
        {
            throw new MongoException( "Could not start mongo router node:" + e.toString() );
        }
    }
}
