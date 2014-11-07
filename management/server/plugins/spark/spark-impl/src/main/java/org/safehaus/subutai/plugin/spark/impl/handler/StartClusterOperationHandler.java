package org.safehaus.subutai.plugin.spark.impl.handler;


import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;


public class StartClusterOperationHandler extends AbstractOperationHandler<SparkImpl>
{

    public StartClusterOperationHandler( SparkImpl manager, String clusterName )
    {
        super( manager, clusterName );
        trackerOperation = manager.getTracker().createTrackerOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Starting %s cluster", clusterName ) );
    }


    @Override
    public void run()
    {
        try
        {
            SparkClusterConfig config = manager.getCluster( clusterName );

            if ( config == null )
            {
                throw new ClusterException( String.format( "Cluster with name %s does not exist", clusterName ) );
            }

            Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );

            if ( environment == null )
            {
                throw new ClusterException(
                        String.format( "Environment not found by id %s", config.getEnvironmentId() ) );
            }

            ContainerHost master = environment.getContainerHostByUUID( config.getMasterNodeId() );

            if ( master == null )
            {
                throw new ClusterException(
                        String.format( "Master node not found in environment by id %s", config.getMasterNodeId() ) );
            }

            try
            {
                CommandResult result = master.execute( manager.getCommands().getStartAllCommand() );
                if ( !result.hasSucceeded() )
                {
                    throw new ClusterException( String.format( "Could not start all nodes : %s",
                            result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
                }
            }
            catch ( CommandException e )
            {
                throw new ClusterException( e );
            }

            //all is well
            trackerOperation.addLogDone( "All nodes are started successfully." );
        }
        catch ( ClusterException e )
        {
            trackerOperation.addLogFailed( String.format( "Could not start all nodes : %s", e.getMessage() ) );
        }
    }
}
