package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.mongodb.api.MongoException;
import org.safehaus.subutai.plugin.mongodb.api.MongoNode;
import org.safehaus.subutai.plugin.mongodb.impl.common.CommandDef;
import org.safehaus.subutai.plugin.mongodb.impl.common.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by timur on 11/14/14.
 */
public abstract class MongoNodeImpl extends ContainerHost implements MongoNode
{
    static final Logger LOG = LoggerFactory.getLogger( MongoNodeImpl.class );


    String domainName;
    int port;


    public MongoNodeImpl( final Agent agent, final UUID peerId, final UUID environmentId, String domainName, int port )
    {
        super( agent, peerId, environmentId );
        this.domainName = domainName;
        this.port = port;
    }


    @Override
    public String getDomainName()
    {
        return domainName;
    }


    @Override
    public boolean isRunning()
    {
        CommandDef commandDef = Commands.getCheckInstanceRunningCommand( getHostname(), domainName, port );
        try
        {
            CommandResult commandResult = execute( commandDef.build() );
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
            execute( commandDef.build( true ) );
        }
        catch ( CommandException e )
        {
            LOG.error( e.toString(), e );
            throw new MongoException( "Could not stop mongo instance." );
        }
    }
}
