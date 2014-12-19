package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.MongoConfigNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoDataNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoException;
import org.safehaus.subutai.plugin.mongodb.api.MongoRouterNode;
import org.safehaus.subutai.plugin.mongodb.impl.common.CommandDef;
import org.safehaus.subutai.plugin.mongodb.impl.common.Commands;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.Expose;


public class MongoRouterNodeImpl extends MongoNodeImpl implements MongoRouterNode
{
    @Expose
    Set<MongoConfigNodeImpl> configServers = new HashSet<>();

    @Expose
    int cfgSrvPort;


    public MongoRouterNodeImpl( final ContainerHost containerHost, final String domainName, final int port,
                                final int cfgSrvPort )
    {
        super( containerHost, domainName, port );
        this.cfgSrvPort = cfgSrvPort;
    }


    @Override
    public void start( MongoClusterConfig config ) throws MongoException
    {
        Preconditions.checkNotNull( configServers, "Config servers is null" );
        CommandDef commandDef = Commands.getStartRouterCommandLine( port, cfgSrvPort, domainName,
                config.getConfigServers() );
        try
        {
            CommandResult commandResult = containerHost.execute( commandDef.build( true ) );

            if ( !commandResult.getStdOut().contains( "child process started successfully, parent exiting" ) )
            {
                throw new CommandException( "Could not start mongo route instance." );
            }
        }
        catch ( CommandException e )
        {
            LOG.error( e.toString(), e );
            throw new MongoException( "Could not start mongo router node:" );
        }
    }


    @Override
    public void registerDataNodesWithReplica( final Set<MongoDataNode> dataNodes, final String replicaName )
            throws MongoException
    {
        CommandDef cmd = Commands.getRegisterReplicaWithRouterCommandLine( this, dataNodes, replicaName );
        try
        {
            CommandResult commandResult = containerHost.execute( cmd.build() );
            if ( !commandResult.hasSucceeded() )
            {
                throw new MongoException( "Could not register data nodes." );
            }
        }
        catch ( CommandException e )
        {
            LOG.error( e.toString(), e );
            throw new MongoException( "Could not register data nodes." );
        }
    }


    @Override
    public void setConfigServers( Set<MongoConfigNode> configServers )
    {
        this.configServers.clear();
        for ( final MongoConfigNode configServer : configServers )
        {
            this.configServers.add( ( MongoConfigNodeImpl ) configServer );
        }
    }
}
