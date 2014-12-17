package org.safehaus.subutai.plugin.mongodb.impl.model;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.mongodb.api.MongoConfigNode;
import org.safehaus.subutai.plugin.mongodb.api.MongoException;
import org.safehaus.subutai.plugin.mongodb.impl.common.CommandDef;
import org.safehaus.subutai.plugin.mongodb.impl.common.Commands;


/**
 * Created by talas on 12/16/14.
 */
@Entity
@DiscriminatorValue( "CONFIG" )
public class MongoConfigNodeEntity extends MongoNodeEntity implements MongoConfigNode
{
    public MongoConfigNodeEntity( final ContainerHost containerHost, final String domainName, final int port )
    {
        super( containerHost, domainName, port );
    }


    @Override
    public void start() throws MongoException
    {
        try
        {
            CommandDef commandDef = Commands.getStartConfigServerCommand( port );
            CommandResult commandResult = containerHost.execute( commandDef.build( true ) );

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
