package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.mongodb.api.MongoDataNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoException;
import org.safehaus.subutai.plugin.mongodb.impl.common.CommandDef;
import org.safehaus.subutai.plugin.mongodb.impl.common.Commands;

import com.google.common.base.Strings;


public class MongoDataNodeImpl extends ContainerHost implements MongoDataNode
{
    int port;


    public MongoDataNodeImpl( final Agent agent, final UUID peerId, final UUID environmentId, int port )
    {
        super( agent, peerId, environmentId );
        this.port = port;
    }


    @Override
    public void start() throws MongoException
    {
        try
        {
            CommandDef commandDef = Commands.getStartDataNodeCommandLine( port );
            CommandResult commandResult = execute( commandDef.build() );
            if ( !commandResult.getStdOut().contains( "child process started successfully, parent exiting" ) )
            {
                throw new CommandException( "Could not start data instance." );
            }
        }
        catch ( CommandException e )
        {
            throw new MongoException( "Error on starting data node: " + e.toString() );
        }
    }


    @Override
    public void setReplicaSetName( final String replicaSetName ) throws MongoException
    {
        try
        {
            CommandDef commandDef = Commands.getSetReplicaSetNameCommandLine( replicaSetName );
            execute( commandDef.build() );
        }
        catch ( CommandException e )
        {
            throw new MongoException( "Error on setting replica set name: " + e.toString() );
        }
    }


    @Override
    public String getPrimaryNodeName( String domainName ) throws MongoException
    {
        CommandDef commandDef = Commands.getFindPrimaryNodeCommandLine( port );
        try
        {
            CommandResult commandResult = execute( commandDef.build() );
            Pattern p = Pattern.compile( "primary\" : \"(.*)\"" );
            Matcher m = p.matcher( commandResult.getStdOut() );
            if ( m.find() )
            {
                String primaryNodeHost = m.group( 1 );
                if ( !Strings.isNullOrEmpty( primaryNodeHost ) )
                {
                    String hostname = primaryNodeHost.split( ":" )[0].replace( "." + domainName, "" );
                    return hostname;
                }
            }
            throw new MongoException( "Primary node not found" );
        }
        catch ( CommandException e )
        {
            throw new MongoException( "Error on getting primary node name" );
        }
    }


    @Override
    public void registerSecondaryNode( final MongoDataNode newDataNodeAgent, final int dataNodePort,
                                       final String domainName ) throws MongoException
    {
        CommandDef registerSecondaryNodeCommandDef =
                Commands.getRegisterSecondaryNodeWithPrimaryCommandLine( newDataNodeAgent.getHostname(), port,
                        domainName );
        try
        {
            execute( registerSecondaryNodeCommandDef.build() );
        }
        catch ( CommandException e )
        {
            throw new MongoException( "Error on registering secondary node." + e.toString() );
        }
    }
}
