package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;


/**
 * Handles start cluster operation
 */
public class StartClusterOperationHandler extends AbstractOperationHandler<AccumuloImpl>
{

    public StartClusterOperationHandler( AccumuloImpl manager, String clusterName )
    {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Starting cluster %s", clusterName ) );
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

        if ( manager.getAgentManager().getAgentByHostname( accumuloClusterConfig.getMasterNode().getHostname() )
                == null )
        {
            trackerOperation.addLogFailed( String.format( "Master node '%s' is not connected",
                    accumuloClusterConfig.getMasterNode().getHostname() ) );
            return;
        }

        trackerOperation.addLog( "Starting cluster..." );

        Command startCommand = manager.getCommands().getStartCommand( accumuloClusterConfig.getMasterNode() );
        manager.getCommandRunner().runCommand( startCommand );

        if ( startCommand.hasSucceeded() )
        {
            trackerOperation.addLogDone( "Cluster started successfully" );
        }
        else
        {
            trackerOperation.addLogFailed(
                    String.format( "Failed to start cluster %s, %s", clusterName, startCommand.getAllErrors() ) );
        }
    }
}
