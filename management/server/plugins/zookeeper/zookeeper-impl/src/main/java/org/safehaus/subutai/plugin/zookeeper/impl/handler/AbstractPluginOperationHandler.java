package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.List;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ApiBase;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


public abstract class AbstractPluginOperationHandler<T extends ApiBase, V extends ConfigBase> extends AbstractOperationHandler<T, V>
{

    private static final Logger LOG = LoggerFactory.getLogger( AbstractPluginOperationHandler.class );


    public AbstractPluginOperationHandler( final T manager, final V config )
    {
        super( manager, config );
    }

    public AbstractPluginOperationHandler( final T manager, final String clusterName )
    {
        super( manager, clusterName );
    }

    protected CommandResult executeCommand( ContainerHost containerHost, String command )
    {
        CommandResult result = null;
        try
        {
            result = containerHost.execute( new RequestBuilder( command ).withTimeout( 1800 ) );
        }
        catch ( CommandException e )
        {
            LOG.error( "Could not execute command succesfully! ", command );
            LOG.error( "Error: ", e );
        }
        return result;
    }


    public void logResults( TrackerOperation po, List<CommandResult> commandResultList )
    {
        Preconditions.checkNotNull( commandResultList );
        for ( CommandResult commandResult : commandResultList )
            po.addLog( commandResult.getStdOut() );
        po.addLogDone( "" );
    }
}
