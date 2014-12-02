package org.safehaus.subutai.plugin.shark.impl.handler;


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
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.shark.impl.SharkImpl;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


public class ClusterOperationHandler extends AbstractOperationHandler<SharkImpl, SharkClusterConfig>
        implements ClusterOperationHandlerInterface
{

    private static final Logger LOG = LoggerFactory.getLogger( ClusterOperationHandler.class.getName() );
    private ClusterOperationType operationType;
    private Environment environment;
    private HadoopClusterConfig hadoopConfig;


    public ClusterOperationHandler( final SharkImpl manager, final SharkClusterConfig config,
                                    final ClusterOperationType operationType, HadoopClusterConfig hadoopConfig )
    {
        this( manager, config, operationType );
        Preconditions.checkNotNull( hadoopConfig );

        this.hadoopConfig = hadoopConfig;
    }


    public ClusterOperationHandler( final SharkImpl manager, final SharkClusterConfig config,
                                    final ClusterOperationType operationType )
    {
        super( manager, config );
        Preconditions.checkNotNull( operationType );
        this.operationType = operationType;
        trackerOperation = manager.getTracker().createTrackerOperation( SharkClusterConfig.PRODUCT_KEY,
                String.format( "Executing %s operation on cluster %s", operationType.name(), clusterName ) );
    }


    @Override
    public void runOperationOnContainers( final ClusterOperationType clusterOperationType )
    {
        switch ( operationType )
        {
            case INSTALL:
                setupCluster();
                break;
            case UNINSTALL:
                destroyCluster();
                break;
            case CUSTOM:
                actualizeMasterIP();
                break;
        }
    }


    private void actualizeMasterIP()
    {
        try
        {
            SparkClusterConfig sparkConfig = manager.getSparkManager().getCluster( config.getSparkClusterName() );
            if ( sparkConfig == null )
            {
                throw new ClusterException( String.format( "Spark cluster %s not found", config.getClusterName() ) );
            }
            environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
            if ( environment == null )
            {
                throw new ClusterException(
                        String.format( "Environment not found by id %s", config.getEnvironmentId() ) );
            }

            Set<ContainerHost> sharkNodes = environment.getHostsByIds( config.getNodeIds() );

            if ( sharkNodes.size() < config.getNodeIds().size() )
            {
                throw new ClusterException( "Found fewer Shark nodes in environment than exist" );
            }

            ContainerHost sparkMaster = environment.getContainerHostByUUID( sparkConfig.getMasterNodeId() );

            if ( sparkMaster == null )
            {
                throw new ClusterException( "Spark master not found in environment" );
            }

            for ( ContainerHost node : sharkNodes )
            {
                if ( !node.isConnected() )
                {
                    throw new ClusterException( String.format( "Node %s is not connected", node.getHostname() ) );
                }
            }


            RequestBuilder actualizeMasterIpCommand = manager.getCommands().getSetMasterIPCommand( sparkMaster );


            trackerOperation.addLog( "Setting master IP..." );

            for ( ContainerHost sharkNode : sharkNodes )
            {
                executeCommand( sharkNode, actualizeMasterIpCommand );
            }

            trackerOperation.addLogDone( "Master IP updated on all nodes" );
        }
        catch ( ClusterException e )
        {
            LOG.error( "Error in actualizeMasterIP", e );
            trackerOperation.addLogFailed( String.format( "Failed to actualize master IP : %s", e.getMessage() ) );
        }
    }


    @Override
    public void setupCluster()
    {

        try
        {

            SparkClusterConfig sparkConfig = manager.getSparkManager().getCluster( config.getSparkClusterName() );
            if ( sparkConfig == null )
            {
                throw new ClusterSetupException(
                        String.format( "Spark cluster %s not found", config.getClusterName() ) );
            }
            environment = manager.getEnvironmentManager().getEnvironmentByUUID( sparkConfig.getEnvironmentId() );
            if ( environment == null )
            {
                throw new ClusterSetupException( String.format( "Could not find environment of Spark cluster by id %s",
                        sparkConfig.getEnvironmentId() ) );
            }


            //setup Shark cluster
            ClusterSetupStrategy s = manager.getClusterSetupStrategy( trackerOperation, config, environment );

            trackerOperation.addLog( "Setting up cluster..." );
            s.setup();
            trackerOperation.addLogDone( "Cluster setup completed" );
        }
        catch ( ClusterSetupException e )
        {
            LOG.error( "Error in setupCluster", e );
            trackerOperation.addLogFailed( String.format( "Failed to setup cluster : %s", e.getMessage() ) );
        }
    }


    @Override
    public void destroyCluster()
    {
        try
        {
            environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
            if ( environment == null )
            {
                throw new ClusterException(
                        String.format( "Environment not found by id %s", config.getEnvironmentId() ) );
            }

            Set<ContainerHost> sharkNodes = environment.getHostsByIds( config.getNodeIds() );

            if ( sharkNodes.size() < config.getNodeIds().size() )
            {
                throw new ClusterException( "Found fewer Shark nodes in environment than exist" );
            }


            for ( ContainerHost node : sharkNodes )
            {
                if ( !node.isConnected() )
                {
                    throw new ClusterException( String.format( "Node %s is not connected", node.getHostname() ) );
                }
            }

            RequestBuilder uninstallCommand = manager.getCommands().getUninstallCommand();


            trackerOperation.addLog( "Uninstalling Shark..." );

            for ( ContainerHost sharkNode : sharkNodes )
            {
                executeCommand( sharkNode, uninstallCommand );
            }

            if ( !manager.getPluginDao().deleteInfo( SharkClusterConfig.PRODUCT_KEY, clusterName ) )
            {
                throw new ClusterException( "Could not remove cluster info" );
            }


            trackerOperation.addLogDone( "Shark uninstalled successfully" );
        }
        catch ( ClusterException e )
        {
            LOG.error( "Error in destroyCluster", e );
            trackerOperation.addLogFailed( String.format( "Failed to destroy cluster : %s", e.getMessage() ) );
        }
    }


    @Override
    public void run()
    {
        runOperationOnContainers( operationType );
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
