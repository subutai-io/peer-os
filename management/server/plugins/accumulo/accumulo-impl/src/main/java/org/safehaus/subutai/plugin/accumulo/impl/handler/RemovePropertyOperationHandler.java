package org.safehaus.subutai.plugin.accumulo.impl.handler;//package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.Commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Handles remove accumulo config property operation
 */
public class RemovePropertyOperationHandler extends AbstractOperationHandler<AccumuloImpl, AccumuloClusterConfig>
{
    private final String propertyName;


    public RemovePropertyOperationHandler( AccumuloImpl manager, String clusterName, String propertyName )
    {
        super( manager, manager.getCluster( clusterName ) );
        this.propertyName = propertyName;
        Preconditions.checkArgument( !Strings.isNullOrEmpty( propertyName ), "Property name is null or empty" );
        trackerOperation = manager.getTracker().createTrackerOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Removing property %s", propertyName ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return trackerOperation.getId();
    }


    @Override
    public void run()
    {
        AccumuloClusterConfig accumuloClusterConfig = manager.getCluster( clusterName );

        if ( accumuloClusterConfig == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        CommandResult result = null;
        boolean allSuccess = true;
        for ( ContainerHost containerHost : environment.getHostsByIds( accumuloClusterConfig.getAllNodes() ) )
        {
            try
            {
                result = containerHost
                        .execute( new RequestBuilder( Commands.getRemovePropertyCommand( propertyName ) ) );
                if ( result.hasSucceeded() )
                {
                    trackerOperation.addLog( "Property removed successfully to node " + containerHost.getHostname() );
                }
                else
                {
                    allSuccess = false;
                }
            }
            catch ( CommandException e )
            {
                allSuccess = false;
                trackerOperation.addLogFailed( String.format( "Removing property failed, %s", result.getStdErr() ) );
                e.printStackTrace();
            }
        }
        if ( allSuccess )
        {
            trackerOperation.addLog( "Restarting cluster... " );
            ContainerHost master = environment.getContainerHostByUUID( accumuloClusterConfig.getMasterNode() );
            try
            {
                master.execute( new RequestBuilder( Commands.stopCommand ) );
                master.execute( new RequestBuilder( Commands.startCommand ) );
            }
            catch ( CommandException e )
            {
                e.printStackTrace();
                trackerOperation.addLogFailed( String.format( "Accumulo cluster restart operation failed !!!" ) );
            }
        }
        trackerOperation.addLogDone( "Done" );
    }
}
