package org.safehaus.subutai.plugin.spark.impl.handler;


import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;


public class AddSlaveNodeOperationHandler extends AbstractOperationHandler<SparkImpl>
{

    private final String hostname;


    public AddSlaveNodeOperationHandler( SparkImpl manager, String clusterName, String hostname )
    {
        super( manager, clusterName );
        this.hostname = hostname;
        trackerOperation = manager.getTracker().createTrackerOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Adding node %s to %s", hostname, clusterName ) );
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


            if ( !master.isConnected() )
            {
                throw new ClusterException( "Master node is not connected" );
            }

            ContainerHost node = environment.getContainerHostByHostname( hostname );

            if ( node == null )
            {
                throw new ClusterException( String.format( "Node not found in environment by name %s", hostname ) );
            }

            //check if node is in the cluster
            if ( config.getAllNodesIds().contains( node.getId() ) )
            {
                throw new ClusterException( String.format( "Node %s already belongs to this cluster", hostname ) );
            }

            // check if node is one of Hadoop cluster nodes
            if ( !config.getHadoopNodeIds().contains( node.getId() ) )
            {
                throw new ClusterException( "Node does not belong to Hadoop cluster" );
            }

            trackerOperation.addLog( "Checking prerequisites..." );

            boolean install = !node.getId().equals( config.getMasterNodeId() );

            //check installed subutai packages
            RequestBuilder checkInstalledCommand = manager.getCommands().getCheckInstalledCommand();
            CommandResult result = executeCommand( node, checkInstalledCommand );


            if ( result.getStdOut().contains( Common.PACKAGE_PREFIX + SparkClusterConfig.PRODUCT_KEY.toLowerCase() )
                    && install )
            {
                throw new ClusterException( String.format( "Node %s already has Spark installed", hostname ) );
            }
            else if ( !result.getStdOut()
                             .contains( Common.PACKAGE_PREFIX + HadoopClusterConfig.PRODUCT_KEY.toLowerCase() ) )
            {
                throw new ClusterException( String.format( "Node %s has no Hadoop installation", hostname ) );
            }

            config.getSlaveIds().add( node.getId() );

            trackerOperation.addLog( "Updating db..." );

            //save to db
            manager.getPluginDAO().saveInfo( SparkClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
            trackerOperation.addLog( "Cluster info updated in DB" );

            //install spark
            if ( install )
            {
                trackerOperation.addLog( "Installing Spark..." );
                RequestBuilder installCommand = manager.getCommands().getInstallCommand();
                executeCommand( node, installCommand );
            }

            trackerOperation.addLog( "Setting master IP on slave..." );
            RequestBuilder setMasterIPCommand = manager.getCommands().getSetMasterIPCommand( master.getHostname() );
            executeCommand( node, setMasterIPCommand );


            trackerOperation.addLog( "Registering slave with master..." );

            RequestBuilder addSlaveCommand = manager.getCommands().getAddSlaveCommand( node.getHostname() );
            executeCommand( master, addSlaveCommand );


            trackerOperation.addLog( " Restarting master..." );

            RequestBuilder restartMasterCommand = manager.getCommands().getRestartMasterCommand();
            executeCommand( master, restartMasterCommand );

            trackerOperation.addLog( "Starting Spark on new node..." );

            RequestBuilder startSlaveCommand = manager.getCommands().getStartSlaveCommand();
            executeCommand( node, startSlaveCommand );

            trackerOperation.addLogDone( "Node addition succeeded" );
        }
        catch ( ClusterException e )
        {
            trackerOperation.addLogFailed( String.format( "Node addition failed: %s", e.getMessage() ) );
        }
    }


    public CommandResult executeCommand( ContainerHost host, RequestBuilder command ) throws ClusterException
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
        return result;
    }
}
