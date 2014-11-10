package org.safehaus.subutai.plugin.zookeeper.impl.handler;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentDestroyException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.ClusterOperationHandlerInterface;
import org.safehaus.subutai.plugin.common.api.ClusterOperationType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.impl.Commands;
import org.safehaus.subutai.plugin.zookeeper.impl.ZookeeperImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * This class handles operations that are related to whole cluster.
 */
public class ZookeeperClusterOperationHandler extends AbstractOperationHandler<ZookeeperImpl, ZookeeperClusterConfig>
        implements ClusterOperationHandlerInterface
{
    private static final Logger LOG = LoggerFactory.getLogger( ZookeeperClusterOperationHandler.class.getName() );
    private ClusterOperationType operationType;
    private ZookeeperClusterConfig zookeeperClusterConfig;
    private HadoopClusterConfig hadoopClusterConfig;
    private ExecutorService executor = Executors.newCachedThreadPool();


    public ZookeeperClusterOperationHandler( final ZookeeperImpl manager, final ZookeeperClusterConfig config,
                                             final ClusterOperationType operationType )
    {
        super( manager, config.getClusterName() );
        this.operationType = operationType;
        this.zookeeperClusterConfig = config;
        this.hadoopClusterConfig = hadoopClusterConfig;
        trackerOperation = manager.getTracker().createTrackerOperation( config.getProductKey(),
                String.format( "Creating %s tracker object...", clusterName ) );
    }


    public ZookeeperClusterOperationHandler( final ZookeeperImpl manager,
                                             final ZookeeperClusterConfig zookeeperClusterConfig,
                                             final HadoopClusterConfig hadoopClusterConfig,
                                             final ClusterOperationType operationType )
    {
        super( manager, zookeeperClusterConfig.getClusterName() );
        this.operationType = operationType;
        this.zookeeperClusterConfig = zookeeperClusterConfig;
        trackerOperation = manager.getTracker().createTrackerOperation( zookeeperClusterConfig.getProductKey(),
                String.format( "Creating %s tracker object...", clusterName ) );
    }


    public void run()
    {
        Preconditions.checkNotNull( zookeeperClusterConfig, "Configuration is null !!!" );
        switch ( operationType )
        {
            case INSTALL:
                executor.execute( new Runnable()
                {
                    public void run()
                    {
                        setupCluster();
                    }
                } );
                break;
            case UNINSTALL:
                executor.execute( new Runnable()
                {
                    public void run()
                    {
                        destroyCluster();
                    }
                } );
                break;
        }
    }


    @Override
    public void runOperationOnContainers( ClusterOperationType clusterOperationType )
    {
        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( zookeeperClusterConfig.getEnvironmentId() );
        List<CommandResult> commandResultList = new ArrayList<CommandResult>(  );
        switch ( clusterOperationType )
        {
            case START_ALL:
                for ( ContainerHost containerHost : environment.getContainers() )
                {
                        commandResultList.add( executeCommand( containerHost,
                                new Commands().getStartCommand() ) );
                }
                break;
            case STOP_ALL:
                for ( ContainerHost containerHost : environment.getContainers() )
                {
                    commandResultList.add( executeCommand( containerHost,
                            new Commands().getStopCommand() ) );
                }
                break;
            case STATUS_ALL:
                for ( ContainerHost containerHost : environment.getContainers() )
                {
                    commandResultList.add( executeCommand( containerHost,
                            new Commands().getStatusCommand() ) );
                }
                break;
        }
        logResults( trackerOperation, commandResultList );
    }


    private CommandResult executeCommand( ContainerHost containerHost, String command )
    {
        CommandResult result = null;
        try
        {
            result = containerHost.execute( new RequestBuilder( command ) );
        }
        catch ( CommandException e )
        {
            LOG.error( "Could not execute command correctly. ", command );
            e.printStackTrace();
        }
        return result;
    }


    @Override
    public void setupCluster()
    {
        if ( Strings.isNullOrEmpty( zookeeperClusterConfig.getClusterName() ) )
        {
            trackerOperation.addLogFailed( "Malformed configuration" );
            return;
        }

        if ( manager.getCluster( clusterName ) != null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name '%s' already exists", clusterName ) );
            return;
        }

        try
        {
            Environment env = manager.getEnvironmentManager()
                                     .buildEnvironment( manager.getDefaultEnvironmentBlueprint( zookeeperClusterConfig ) );

            ClusterSetupStrategy clusterSetupStrategy =
                    manager.getClusterSetupStrategy( env, zookeeperClusterConfig, trackerOperation );
            clusterSetupStrategy.setup();

            trackerOperation.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( EnvironmentBuildException | ClusterSetupException e )
        {
            trackerOperation.addLogFailed(
                    String.format( "Failed to setup %s cluster %s : %s", zookeeperClusterConfig.getProductKey(), clusterName,
                            e.getMessage() ) );
        }
    }


    @Override
    public void destroyCluster()
    {
        ZookeeperClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
            return;
        }

        try
        {
            trackerOperation.addLog( "Destroying environment..." );
            manager.getEnvironmentManager().destroyEnvironment( config.getEnvironmentId() );
            manager.getPluginDAO().deleteInfo( config.getProductKey(), config.getClusterName() );
            trackerOperation.addLogDone( "Cluster destroyed" );
        }
        catch ( EnvironmentDestroyException e )
        {
            trackerOperation.addLogFailed( String.format( "Error running command, %s", e.getMessage() ) );
            LOG.error( e.getMessage(), e );
        }
    }


    public void logResults( TrackerOperation po, List<CommandResult> commandResultList )
    {
        Preconditions.checkNotNull( commandResultList );
        for ( CommandResult commandResult : commandResultList )
            po.addLog( commandResult.getStdOut() );
        String finishMessage = String.format( "%s operation finished", operationType );
        switch ( po.getState() )
        {
            case SUCCEEDED:
                po.addLogDone( finishMessage );
                break;
            case FAILED:
                po.addLogFailed( finishMessage );
                break;
            default:
                po.addLogDone( String.format( "Still running %s operations on %s", operationType ) );
                break;
        }
    }
}
