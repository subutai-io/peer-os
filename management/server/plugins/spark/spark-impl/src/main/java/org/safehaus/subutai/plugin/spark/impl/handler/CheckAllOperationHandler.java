package org.safehaus.subutai.plugin.spark.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;


public class CheckAllOperationHandler extends AbstractOperationHandler<SparkImpl>
{

    public CheckAllOperationHandler( SparkImpl manager, String clusterName )
    {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Checking state of all nodes in %s", clusterName ) );
    }


    @Override
    public void run()
    {
        SparkClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Command checkStatusCommand = manager.getCommands().getStatusAllCommand( ( Agent ) config.getAllNodes() );
        manager.getCommandRunner().runCommand( checkStatusCommand );

        if ( checkStatusCommand.hasSucceeded() )
        {
            trackerOperation.addLogDone( "All nodes are running." );
        }
        else
        {
            trackerOperation.addLogFailed( "Could not check all nodes successfully !" );
        }
    }
}
