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


public class CheckNodeOperationHandler extends AbstractOperationHandler<SparkImpl>
{

    private final String hostname;
    private final boolean master;


    public CheckNodeOperationHandler( SparkImpl manager, String clusterName, String hostname, boolean master )
    {
        super( manager, clusterName );
        this.hostname = hostname;
        this.master = master;
        trackerOperation = manager.getTracker().createTrackerOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Checking state of %s in %s", hostname, clusterName ) );
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


            RequestBuilder checkNodeCommand;
            if ( master )
            {
                checkNodeCommand = manager.getCommands().getStatusMasterCommand();
            }
            else
            {
                checkNodeCommand = manager.getCommands().getStatusSlaveCommand();
            }
            CommandResult result;
            try
            {
                result = node.execute( checkNodeCommand );
            }
            catch ( CommandException e )
            {
                throw new ClusterException( e );
            }

            if ( result.hasSucceeded() )
            {
                trackerOperation.addLogDone( String.format( "%s", result.getStdOut() ) );
            }
            else
            {
                throw new ClusterException( String.format( "Error on container: %s",
                        result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
            }
        }
        catch ( ClusterException e )
        {
            trackerOperation.addLogFailed( String.format( "Failed to check node status: %s", e.getMessage() ) );
        }
    }
}
