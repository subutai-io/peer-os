package org.safehaus.subutai.plugin.mongodb.impl;


import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.mongodb.api.MongoConfigNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoException;
import org.safehaus.subutai.plugin.mongodb.impl.common.CommandDef;
import org.safehaus.subutai.plugin.mongodb.impl.common.Commands;


public class MongoConfigNodeImpl extends MongoNodeImpl implements MongoConfigNode
{

    public MongoConfigNodeImpl( final Agent agent, final UUID peerId, final UUID environmentId, final String domainName,
                                final int port )
    {
        super( agent, peerId, environmentId, domainName, port );
    }


    @Override
    public void start() throws MongoException
    {
        try
        {
            CommandDef commandDef = Commands.getStartConfigServerCommand( port );
            CommandResult commandResult = execute( commandDef.build( true ).withTimeout( 15 ) );

            if ( !commandResult.getStdOut().contains( "child process started successfully, parent exiting" ) )
            {
                throw new CommandException( "Could not start mongo config instance." );
            }
        }
        catch ( CommandException e )
        {
            LOG.error( e.getMessage(), e );
            throw new MongoException( e.getMessage() );
        }
    }
}
