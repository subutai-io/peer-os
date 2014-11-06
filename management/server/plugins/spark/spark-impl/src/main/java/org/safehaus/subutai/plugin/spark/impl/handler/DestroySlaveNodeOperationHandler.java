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


public class DestroySlaveNodeOperationHandler extends AbstractOperationHandler<SparkImpl>
{

    private final String hostname;


    public DestroySlaveNodeOperationHandler( SparkImpl manager, String clusterName, String hostname )
    {
        super( manager, clusterName );
        this.hostname = hostname;
        trackerOperation = manager.getTracker().createTrackerOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Destroying %s in %s", hostname, clusterName ) );
    }


    @Override
    public void run()
    {
        try
        {
            final SparkClusterConfig config = manager.getCluster( clusterName );
            if ( config == null )
            {
                throw new ClusterException( String.format( "Cluster with name %s does not exist", clusterName ) );
            }

            if ( config.getSlaveIds().size() == 1 )
            {
                throw new ClusterException(
                        "This is the last slave node in the cluster. Please, destroy cluster instead" );
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

            if ( node.getId().equals( config.getMasterNodeId() ) )
            {
                throw new ClusterException( "Can not destroy master node, change master first" );
            }

            if ( !node.isConnected() )
            {
                throw new ClusterException( String.format( "Container %s is not connected", hostname ) );
            }

            if ( !config.getAllNodesIds().contains( node.getId() ) )
            {
                throw new ClusterException( String.format( "Node %s does not belong to this cluster", hostname ) );
            }


            ContainerHost master = environment.getContainerHostByUUID( config.getMasterNodeId() );

            if ( master == null )
            {
                throw new ClusterException(
                        String.format( "Master node not found in environment by id %s", config.getMasterNodeId() ) );
            }

            trackerOperation.addLog( "Unregistering slave from master..." );

            RequestBuilder clearSlavesCommand = manager.getCommands().getClearSlaveCommand( hostname );

            executeCommand( master, clearSlavesCommand );

            trackerOperation.addLog( "Successfully unregistered slave from master\nRestarting master..." );

            RequestBuilder restartMasterCommand = manager.getCommands().getRestartMasterCommand();

            executeCommand( master, restartMasterCommand );


            boolean uninstall = !node.getId().equals( config.getMasterNodeId() );

            if ( uninstall )
            {
                trackerOperation.addLog( "Uninstalling Spark..." );

                RequestBuilder uninstallCommand = manager.getCommands().getUninstallCommand();

                executeCommand( node, uninstallCommand );

                //TODO destroy node when environment resize is implemented
            }
            else
            {
                trackerOperation.addLog( "Stopping slave..." );

                RequestBuilder stopSlaveCommand = manager.getCommands().getStopSlaveCommand();

                executeCommand( node, stopSlaveCommand );
            }

            config.getSlaveIds().remove( node.getId() );

            trackerOperation.addLog( "Updating db..." );

            manager.getPluginDAO().saveInfo( SparkClusterConfig.PRODUCT_KEY, config.getClusterName(), config );

            trackerOperation.addLogDone( "Cluster info updated in DB\nDone" );
        }
        catch ( ClusterException e )
        {
            trackerOperation.addLogFailed( String.format( "Failed to uninstall node: %s", e.getMessage() ) );
        }
    }


    public void executeCommand( ContainerHost host, RequestBuilder command ) throws ClusterException
    {

        CommandResult result;
        try
        {
            result = host.execute( command );
        }
        catch ( CommandException e )
        {
            throw new ClusterException( e );
        }
        if ( !result.hasSucceeded() )
        {
            throw new ClusterException( String.format( "Error on container %s: %s", host.getHostname(),
                    result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
        }
    }
}
