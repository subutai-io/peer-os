package org.safehaus.subutai.plugin.spark.impl.handler;


import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.ClusterOperationHandlerInterface;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClusterOperationHandler extends AbstractOperationHandler<SparkImpl, SparkClusterConfig>
        implements ClusterOperationHandlerInterface
{

    private static final Logger LOG = LoggerFactory.getLogger( ClusterOperationHandler.class.getName() );
    private ClusterOperationType operationType;
    private Environment environment;
    private ContainerHost master;


    public ClusterOperationHandler( final SparkImpl manager, final SparkClusterConfig config,
                                    final ClusterOperationType operationType )
    {
        super( manager, config );
        this.operationType = operationType;
        trackerOperation = manager.getTracker().createTrackerOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Executing %s operation on cluster %s", operationType.name(), clusterName ) );
    }


    @Override
    public void runOperationOnContainers( final ClusterOperationType operationType )
    {
        switch ( operationType )
        {
            case INSTALL:
                setupCluster();
                break;
            case UNINSTALL:
                destroyCluster();
                break;
            case START_ALL:
                startCluster();
                break;
            case STOP_ALL:
                stopCluster();
                break;
            case STATUS_ALL:
                checkCluster();
                break;
        }
    }


    public void checkPrerequisites() throws ClusterException
    {
        if ( manager.getCluster( clusterName ) == null )
        {
            throw new ClusterException( String.format( "Cluster with name %s does not exist", clusterName ) );
        }


        environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );

        if ( environment == null )
        {
            throw new ClusterException( String.format( "Environment not found by id %s", config.getEnvironmentId() ) );
        }

        master = environment.getContainerHostById( config.getMasterNodeId() );

        if ( master == null )
        {
            throw new ClusterException(
                    String.format( "Master node not found in environment by id %s", config.getMasterNodeId() ) );
        }

        if ( !master.isConnected() )
        {
            throw new ClusterException( "Master node is not connected" );
        }
    }


    public void checkCluster()
    {
        try
        {
            checkPrerequisites();

            CommandResult result = executeCommand( master, manager.getCommands().getStatusAllCommand() );

            trackerOperation.addLogDone( String.format( "Cluster status: %s", result.getStdOut() ) );
        }
        catch ( ClusterException e )
        {
            LOG.error( "Error in checkCluster", e );
            trackerOperation.addLogFailed( String.format( "Failed to check cluster: %s", e.getMessage() ) );
        }
    }


    public void startCluster()
    {
        try
        {
            checkPrerequisites();

            CommandResult result = executeCommand( master, manager.getCommands().getStartAllCommand() );

            if ( !result.getStdOut().contains( "starting" ) )
            {
                trackerOperation.addLogFailed( "Failed to start cluster" );
            }
            else
            {

                trackerOperation.addLogDone( "Cluster started successfully" );
            }
        }
        catch ( ClusterException e )
        {
            LOG.error( "Error in startCluster", e );
            trackerOperation.addLogFailed( String.format( "Failed to start cluster : %s", e.getMessage() ) );
        }
    }


    public void stopCluster()
    {
        try
        {
            checkPrerequisites();

            executeCommand( master, manager.getCommands().getStopAllCommand() );

            trackerOperation.addLogDone( "Cluster stopped successfully" );
        }
        catch ( ClusterException e )
        {
            LOG.error( "Error in stopCluster", e );
            trackerOperation.addLogFailed( String.format( "failed to stop cluster: %s", e.getMessage() ) );
        }
    }


    @Override
    public void destroyCluster()
    {
        try
        {
            checkPrerequisites();

            Set<ContainerHost> allNodes = environment.getContainerHostsByIds( config.getSlaveIds() );

            for ( ContainerHost node : allNodes )
            {
                if ( !node.isConnected() )
                {
                    throw new ClusterException( String.format( "Node %s is not connected", node.getHostname() ) );
                }
            }

            allNodes.add( master );

            trackerOperation.addLog( "Uninstalling Spark..." );

            RequestBuilder uninstallCommand = manager.getCommands().getUninstallCommand();
            for ( ContainerHost node : allNodes )
            {
                try
                {
                    CommandResult result = node.execute( uninstallCommand );
                    if ( !result.hasSucceeded() )
                    {
                        throw new ClusterException(
                                String.format( "Could not uninstall Spark from node %s : %s", node.getHostname(),
                                        result.hasCompleted() ? result.getStdErr() : "Command timed out" ) );
                    }
                }
                catch ( CommandException e )
                {
                    throw new ClusterException(
                            String.format( "Error uninstalling Spark on node %s", node.getHostname() ), e );
                }
            }

            if ( !manager.getPluginDAO().deleteInfo( SparkClusterConfig.PRODUCT_KEY, clusterName ) )
            {
                throw new ClusterException( "Could not remove cluster info" );
            }

            trackerOperation.addLogDone( "Cluster uninstalled successfully" );
        }
        catch ( ClusterException e )
        {
            LOG.error( "Error in destroyCluster", e );
            trackerOperation.addLogFailed( String.format( "Failed to uninstall cluster: %s", e.getMessage() ) );
        }
    }


    @Override
    public void setupCluster()
    {
        try
        {
            HadoopClusterConfig hadoopConfig = manager.getHadoopManager().getCluster( config.getHadoopClusterName() );

            if ( hadoopConfig == null )
            {
                throw new ClusterException(
                        String.format( "Could not find Hadoop cluster %s", config.getHadoopClusterName() ) );
            }

            Environment env = manager.getEnvironmentManager().getEnvironmentByUUID( hadoopConfig.getEnvironmentId() );
            if ( env == null )
            {
                throw new ClusterException( String.format( "Could not find environment of Hadoop cluster by id %s",
                        hadoopConfig.getEnvironmentId() ) );
            }


            ClusterSetupStrategy s = manager.getClusterSetupStrategy( trackerOperation, config, env );
            try
            {
                trackerOperation.addLog( "Setting up cluster..." );
                s.setup();
                trackerOperation.addLogDone( "Cluster setup completed" );
            }
            catch ( ClusterSetupException ex )
            {
                throw new ClusterException( "Failed to setup cluster: " + ex.getMessage() );
            }
        }
        catch ( ClusterException e )
        {
            LOG.error( "Error in setupCluster", e );
            trackerOperation.addLogFailed( String.format( "Failed to setup cluster : %s", e.getMessage() ) );
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


    @Override
    public void run()
    {
        runOperationOnContainers( operationType );
    }
}
