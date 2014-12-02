package org.safehaus.subutai.plugin.hbase.impl.handler;


import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.ClusterOperationHandlerInterface;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;
import org.safehaus.subutai.plugin.hbase.api.SetupType;
import org.safehaus.subutai.plugin.hbase.impl.Commands;
import org.safehaus.subutai.plugin.hbase.impl.HBaseImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Created by bahadyr on 11/17/14.
 */
public class ClusterOperationHandler extends AbstractOperationHandler<HBaseImpl, HBaseConfig>
        implements ClusterOperationHandlerInterface
{
    private static final Logger LOG = LoggerFactory.getLogger( ClusterOperationHandler.class.getName() );
    private ClusterOperationType operationType;
    private Environment environment;
    private HBaseConfig config;


    public ClusterOperationHandler( final HBaseImpl manager, final HBaseConfig hbaseConfig,
                                    final ClusterOperationType operationType )
    {
        this( manager, hbaseConfig );
        Preconditions.checkNotNull( hbaseConfig );
        this.operationType = operationType;
        this.trackerOperation = manager.getTracker().createTrackerOperation( HBaseConfig.PRODUCT_KEY,
                String.format( "Executing %s operation on cluster %s", operationType.name(), clusterName ) );
    }


    public ClusterOperationHandler( final HBaseImpl manager, final HBaseConfig baseClusterConfig )
    {
        super( manager, baseClusterConfig );
        Preconditions.checkNotNull( baseClusterConfig );
        this.config = baseClusterConfig;
    }


    @Override
    public void run()
    {
        runOperationOnContainers( operationType );
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
            case START_ALL:
                startCluster();
                break;
            case STOP_ALL:
                stopCluster();
                break;
        }
    }


    private void stopCluster()
    {
        environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        Set<UUID> allNodes = config.getAllNodes();
        for ( ContainerHost host : environment.getContainerHostsByIds( allNodes ) )
        {
            try
            {
                CommandResult result = host.execute( Commands.getStopCommand() );
                if ( result.hasSucceeded() )
                {
                    trackerOperation.addLog( result.getStdOut() );
                }
                else
                {
                    trackerOperation.addLogFailed( result.getStdErr() );
                }
                trackerOperation.addLogDone( "Stop cluster command executed" );
            }
            catch ( CommandException e )
            {
                trackerOperation.addLogFailed( e.getMessage() );
                LOG.error( e.getMessage(), e );
            }
        }
    }


    private void startCluster()
    {
        environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        Set<UUID> allNodes = config.getAllNodes();
        for ( ContainerHost host : environment.getContainerHostsByIds( allNodes ) )
        {
            try
            {
                CommandResult result = host.execute( Commands.getStartCommand() );
                if ( result.hasSucceeded() )
                {
                    trackerOperation.addLog( result.getStdOut() );
                }
                else
                {
                    trackerOperation.addLogFailed( result.getStdErr() );
                }
                trackerOperation.addLogDone( "Start cluster command executed" );
            }
            catch ( CommandException e )
            {
                trackerOperation.addLogFailed( e.getMessage() );
                LOG.error( e.getMessage(), e );
            }
        }
    }


    @Override
    public void setupCluster()
    {
        try
        {
            if ( config.getSetupType() == SetupType.WITH_HADOOP )
            {

                /*if ( hadoopConfig == null )
                {
                    throw new ClusterSetupException( "No Hadoop configuration specified" );
                }

                trackerOperation.addLog( "Preparing environment..." );

                //setup Hadoop cluster
                hadoopConfig.setTemplateName( HBaseConfig.TEMPLATE_NAME );
                EnvironmentBlueprint eb = manager.getHadoopManager().getDefaultEnvironmentBlueprint( hadoopConfig );
                environment = manager.getEnvironmentManager().buildEnvironment( eb );
                manager.getHadoopManager().getClusterSetupStrategy( environment, hadoopConfig, trackerOperation )
                       .setup();

                trackerOperation.addLog( "Environment built successfully" );
*/
            }
            else
            {
                environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
            }

            //setup HBase cluster
            trackerOperation.addLog( "Installing cluster..." );
            manager.getClusterSetupStrategy( trackerOperation, config, environment ).setup();
            trackerOperation.addLogDone( "Installing cluster completed" );
        }
        catch ( /*EnvironmentBuildException |*/ ClusterSetupException e )
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

            Set<ContainerHost> hbaseNodes = environment.getContainerHostsByIds( config.getAllNodes() );

            if ( hbaseNodes.size() < config.getAllNodes().size() )
            {
                throw new ClusterException( "Found fewer HBase nodes in environment than exist" );
            }


            for ( ContainerHost node : hbaseNodes )
            {
                if ( !node.isConnected() )
                {
                    throw new ClusterException( String.format( "Node %s is not connected", node.getHostname() ) );
                }
            }

            RequestBuilder uninstallCommand = manager.getCommands().getUninstallCommand();


            trackerOperation.addLog( "Uninstalling HBase..." );

            for ( ContainerHost host : hbaseNodes )
            {
                executeCommand( host, uninstallCommand );
            }

            if ( !manager.getPluginDAO().deleteInfo( HBaseConfig.PRODUCT_KEY, clusterName ) )
            {
                throw new ClusterException( "Could not remove cluster info" );
            }


            trackerOperation.addLogDone( "HBase uninstalled successfully" );
        }
        catch ( ClusterException e )
        {
            LOG.error( "Error in destroyCluster", e );
            trackerOperation.addLogFailed( String.format( "Failed to destroy cluster : %s", e.getMessage() ) );
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
