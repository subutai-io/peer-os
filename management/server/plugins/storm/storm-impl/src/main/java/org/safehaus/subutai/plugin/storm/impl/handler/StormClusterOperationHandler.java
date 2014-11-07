package org.safehaus.subutai.plugin.storm.impl.handler;


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
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
import org.safehaus.subutai.plugin.storm.impl.CommandType;
import org.safehaus.subutai.plugin.storm.impl.Commands;
import org.safehaus.subutai.plugin.storm.impl.StormImpl;
import org.safehaus.subutai.plugin.storm.impl.StormService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * This class handles operations that are related to whole cluster.
 */
public class StormClusterOperationHandler extends AbstractOperationHandler<StormImpl>
        implements ClusterOperationHandlerInterface
{
    private static final Logger LOG = LoggerFactory.getLogger( StormClusterOperationHandler.class.getName() );
    private ClusterOperationType operationType;
    private StormClusterConfiguration config;
    private ExecutorService executor = Executors.newCachedThreadPool();


    public StormClusterOperationHandler( final StormImpl manager,
                                         final StormClusterConfiguration config,
                                         final ClusterOperationType operationType )
    {
        super( manager, config.getClusterName() );
        this.operationType = operationType;
        this.config = config;
        trackerOperation = manager.getTracker().createTrackerOperation( config.getProductKey(),
                String.format( "Creating %s tracker object...", clusterName ) );
    }


    public void run()
    {
        Preconditions.checkNotNull( config, "Configuration is null !!!" );
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
        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        List<CommandResult> commandResultList = new ArrayList<CommandResult>(  );
        switch ( clusterOperationType )
        {
            case START_ALL:
                for ( ContainerHost containerHost : environment.getContainers() )
                {
                    if ( config.getNimbus().equals( containerHost.getId() ) ) {
                        commandResultList.add( executeCommand( containerHost,
                                Commands.make( CommandType.START, StormService.NIMBUS ) ) );
                        commandResultList.add( executeCommand( containerHost, Commands
                                .make( CommandType.START, StormService.UI ) ) );
                    }
                    else if ( config.getSupervisors().equals( containerHost.getId() ) )
                        commandResultList.add( executeCommand( containerHost, Commands
                                .make( CommandType.START, StormService.SUPERVISOR ) ) );
                }
                break;
            case STOP_ALL:
                for ( ContainerHost containerHost : environment.getContainers() )
                {
                    if ( config.getNimbus().equals( containerHost.getId() ) ) {
                        commandResultList.add( executeCommand( containerHost, Commands
                                .make( CommandType.STOP, StormService.NIMBUS ) ) );
                        commandResultList.add( executeCommand( containerHost, Commands
                                .make( CommandType.STOP, StormService.UI ) ) );
                    }
                    else if ( config.getSupervisors().equals( containerHost.getId() ) )
                        commandResultList.add( executeCommand( containerHost, Commands
                                .make( CommandType.STOP, StormService.SUPERVISOR ) ) );
                }
                break;
            case STATUS_ALL:
                for ( ContainerHost containerHost : environment.getContainers() )
                {
                    if ( config.getNimbus().equals( containerHost.getId() ) ) {
                        commandResultList.add( executeCommand( containerHost, Commands
                                .make( CommandType.STATUS, StormService.NIMBUS ) ) );
                        commandResultList.add( executeCommand( containerHost, Commands
                                .make( CommandType.STATUS, StormService.UI ) ) );
                    }
                    else if ( config.getSupervisors().equals( containerHost.getId() ) )
                        commandResultList.add( executeCommand( containerHost, Commands
                                .make( CommandType.STATUS, StormService.SUPERVISOR ) ) );
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
        if ( Strings.isNullOrEmpty( config.getClusterName() ) )
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
                                     .buildEnvironment( manager.getDefaultEnvironmentBlueprint( config ) );

            ClusterSetupStrategy clusterSetupStrategy =
                    manager.getClusterSetupStrategy( env, config, trackerOperation );
            clusterSetupStrategy.setup();

            trackerOperation.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( EnvironmentBuildException | ClusterSetupException e )
        {
            trackerOperation.addLogFailed(
                    String.format( "Failed to setup %s cluster %s : %s", config.getProductKey(), clusterName, e.getMessage() ) );
        }
    }


    @Override
    public void destroyCluster()
    {
        StormClusterConfiguration config = manager.getCluster( clusterName );
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
