package org.safehaus.subutai.plugin.spark.impl.handler;


import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;


public class StopNodeOperationHandler extends AbstractOperationHandler<SparkImpl>
{

    private final String hostname;
    private final boolean master;


    public StopNodeOperationHandler( SparkImpl manager, String clusterName, String hostname, boolean master )
    {
        super( manager, clusterName );
        this.hostname = hostname;
        this.master = master;
        trackerOperation = manager.getTracker().createTrackerOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Stopping node %s in %s", hostname, clusterName ) );
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


            ContainerHost node = environment.getContainerHostByHostname( hostname );

            if ( node == null )
            {
                throw new ClusterException( String.format( "Node not found in environment by name %s", hostname ) );
            }


            if ( !config.getAllNodesIds().contains( node.getId() ) )
            {
                throw new ClusterException( String.format( "Node %s does not belong to this cluster", hostname ) );
            }


            trackerOperation
                    .addLog( String.format( "Stopping %s on %s...", master ? "master" : "slave", node.getHostname() ) );

            RequestBuilder stopCommand;
            if ( master )
            {
                stopCommand = manager.getCommands().getStopMasterCommand();
            }
            else
            {
                stopCommand = manager.getCommands().getStopSlaveCommand();
            }

            try
            {
                CommandResult result = node.execute( stopCommand );
                if ( !result.hasSucceeded() )
                {
                    throw new ClusterException( String.format( "Could not stop node : %s",
                            result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
                }
            }
            catch ( CommandException e )
            {
                throw new ClusterException( e );
            }

            trackerOperation.addLogDone( String.format( "Node %s stopped", node.getHostname() ) );
        }
        catch ( ClusterException e )
        {
            trackerOperation.addLogFailed( String.format( "Stopping node failed, %s", e.getMessage() ) );
        }
    }
}
