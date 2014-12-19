package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.mongodb.api.MongoClusterConfig;
import org.safehaus.subutai.plugin.mongodb.api.MongoDataNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoException;
import org.safehaus.subutai.plugin.mongodb.impl.common.CommandDef;
import org.safehaus.subutai.plugin.mongodb.impl.common.Commands;

import com.google.common.base.Strings;


public class MongoDataNodeImpl extends MongoNodeImpl implements MongoDataNode
{

    public MongoDataNodeImpl( final ContainerHost containerHost, final String domainName, final int port )
    {
        super( containerHost, domainName, port );
    }


    @Override
    public void start( MongoClusterConfig config ) throws MongoException
    {
        try
        {
            CommandDef commandDef = Commands.getStartDataNodeCommandLine( port );
            CommandResult commandResult = execute( commandDef.build( true ).withTimeout( 10 ) );


            if ( !commandResult.getStdOut().contains( "child process started successfully, parent exiting" ) )
            {
                throw new CommandException( "Could not start mongo data instance." );
            }
        }
        catch ( CommandException e )
        {
            LOG.error( "Start command failed.", e );
            throw new MongoException( "Start command failed" );
        }
    }


    @Override
    public void setReplicaSetName( final String replicaSetName ) throws MongoException
    {
        try
        {
            CommandDef commandDef = Commands.getSetReplicaSetNameCommandLine( replicaSetName );
            CommandResult commandResult = execute( commandDef.build().withTimeout( 90 ) );
            LOG.info( commandResult.toString() );
        }
        catch ( CommandException e )
        {
            throw new MongoException( "Error on setting replica set name: " );
        }
    }


    @Override
    public String getPrimaryNodeName() throws MongoException
    {
        CommandDef commandDef = Commands.getFindPrimaryNodeCommandLine( port );
        try
        {
            CommandResult commandResult = execute( commandDef.build().withTimeout( 90 ) );
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
            LOG.error( "Error on getting primary node name", e );
            throw new MongoException( "Error on getting primary node name" );
        }
    }


    @Override
    public void registerSecondaryNode( final MongoDataNode dataNode ) throws MongoException
    {
        CommandDef commandDef =
                Commands.getRegisterSecondaryNodeWithPrimaryCommandLine( dataNode.getHostname(), port, domainName );
        try
        {
            CommandResult commandResult = execute( commandDef.build().withTimeout( 90 ) );

            if ( !commandResult.getStdOut().contains( "connecting to:" ) )
            {
                throw new CommandException( "Could not register secondary node." );
            }
        }
        catch ( CommandException e )
        {
            LOG.error( commandDef.getDescription(), e );
            throw new MongoException( "Error on registering secondary node." );
        }
    }


    @Override
    public void initiateReplicaSet() throws MongoException
    {
        CommandDef commandDef = Commands.getInitiateReplicaSetCommandLine( port );
        try
        {
            CommandResult commandResult = execute( commandDef.build().withTimeout( 90 ) );

            if ( !commandResult.getStdOut().contains( "connecting to:" ) )
            {
                throw new CommandException( "Could not register secondary node." );
            }
        }
        catch ( CommandException e )
        {
            LOG.error( commandDef.getDescription(), e );
            throw new MongoException( "Initiate replica set error." );
        }
    }


    @Override
    public void unRegisterSecondaryNode( final MongoDataNode dataNode ) throws MongoException
    {
        CommandDef commandDef =
                Commands.getUnregisterSecondaryNodeFromPrimaryCommandLine( port, dataNode.getHostname(), domainName );
        try
        {
            CommandResult commandResult = execute( commandDef.build().withTimeout( 90 ) );

            if ( !commandResult.getStdOut().contains( "connecting to:" ) )
            {
                throw new CommandException( "Could not remove secondary node." );
            }
        }
        catch ( CommandException e )
        {
            LOG.error( commandDef.getDescription(), e );
            throw new MongoException( "Error on removing secondary node." );
        }
    }
}
