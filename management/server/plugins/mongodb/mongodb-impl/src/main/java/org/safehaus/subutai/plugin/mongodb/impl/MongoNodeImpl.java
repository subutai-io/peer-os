package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.mongodb.api.MongoException;
import org.safehaus.subutai.plugin.mongodb.api.MongoNode;
import org.safehaus.subutai.plugin.mongodb.impl.common.CommandDef;
import org.safehaus.subutai.plugin.mongodb.impl.common.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.Expose;


/**
 * Created by timur on 11/14/14.
 */
public abstract class MongoNodeImpl implements MongoNode
{
    static final Logger LOG = LoggerFactory.getLogger( MongoNodeImpl.class );

    @Expose
    String environmentId;

    @Expose
    String containerHostId;

    ContainerHost containerHost;

    @Expose
    String domainName;

    @Expose
    int port;


    public MongoNodeImpl( final ContainerHost containerHost, String domainName, int port )
    {
        this.containerHost = containerHost;
        this.domainName = domainName;
        this.port = port;
        environmentId = containerHost.getEnvironmentId();
        containerHostId = containerHost.getId().toString();
    }


    @Override
    public String getDomainName()
    {
        return domainName;
    }


    @Override
    public boolean isRunning()
    {
        CommandDef commandDef =
                Commands.getCheckInstanceRunningCommand( containerHost.getHostname(), domainName, port );
        try
        {
            CommandResult commandResult = execute( commandDef.build().withTimeout( 10 ) );
            if ( commandResult.getStdOut().contains( "couldn't connect to server" ) )
            {
                return false;
            }
            else if ( commandResult.getStdOut().contains( "connecting to" ) )
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch ( CommandException e )
        {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public int getPort()
    {
        return port;
    }


    @Override
    public abstract void start() throws MongoException;


    @Override
    public void stop() throws MongoException
    {
        CommandDef commandDef = Commands.getStopNodeCommand();
        try
        {
            execute( commandDef.build( true ).withTimeout( 60 ) );
        }
        catch ( CommandException e )
        {
            LOG.error( e.toString(), e );
            throw new MongoException( "Could not stop mongo instance." );
        }
    }


    @Override
    public String getHostname()
    {
        return containerHost.getHostname();
    }


    @Override
    public CommandResult execute( final RequestBuilder build ) throws CommandException
    {
        return containerHost.execute( build );
    }


    @Override
    public UUID getPeerId()
    {
        return UUID.fromString( containerHost.getPeerId() );
    }


    @Override
    public ContainerHost getContainerHost()
    {
        return containerHost;
    }


    public String getEnvironmentId()
    {
        return environmentId;
    }


    public String getContainerHostId()
    {
        return containerHostId;
    }


    public void setContainerHost( ContainerHost containerHost )
    {
        this.containerHost = containerHost;
    }
}
