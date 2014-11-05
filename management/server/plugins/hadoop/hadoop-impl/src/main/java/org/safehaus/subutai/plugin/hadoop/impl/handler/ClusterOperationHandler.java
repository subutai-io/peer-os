package org.safehaus.subutai.plugin.hadoop.impl.handler;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.safehaus.subutai.common.enums.NodeState;
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
import org.safehaus.subutai.plugin.common.api.ClusterOperationHandlerInterface;
import org.safehaus.subutai.plugin.common.api.OperationType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.plugin.hadoop.impl.common.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * This class handles operations that are related to whole cluster.
 */
public class ClusterOperationHandler extends AbstractOperationHandler<HadoopImpl>
        implements ClusterOperationHandlerInterface
{
    private static final Logger LOG = LoggerFactory.getLogger( ClusterOperationHandler.class.getName() );
    private OperationType operationType;
    private HadoopClusterConfig config;
    private NodeType nodeType;
    private ExecutorService executor = Executors.newCachedThreadPool();


    public ClusterOperationHandler( final HadoopImpl manager, final HadoopClusterConfig config,
                                    final OperationType operationType, NodeType nodeType )
    {
        super( manager, config.getClusterName() );
        this.operationType = operationType;
        this.config = config;
        this.nodeType = nodeType;
        trackerOperation = manager.getTracker().createTrackerOperation( HadoopClusterConfig.PRODUCT_KEY,
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
            case DESTROY:
                executor.execute( new Runnable()
                {
                    public void run()
                    {
                        destroyCluster();
                    }
                } );
                break;
            case START:
                runOperationOnContainers( OperationType.START );
                break;
            case STOP:
                runOperationOnContainers( OperationType.STOP );
                break;
            case STATUS:
                runOperationOnContainers( OperationType.STATUS );
                break;
        }
    }


    @Override
    public void runOperationOnContainers( OperationType operationType )
    {
        try
        {
            CommandResult result = null;
            switch ( operationType )
            {
                case START:
                    switch ( nodeType )
                    {
                        case NAMENODE:
                            result = config.getNameNode()
                                           .execute( new RequestBuilder( Commands.getStartNameNodeCommand() ) );
                            break;
                        case JOBTRACKER:
                            result = config.getJobTracker()
                                           .execute( new RequestBuilder( Commands.getStartJobTrackerCommand() ) );
                            break;
                        //                        case SECONDARY_NAMENODE:
                        //                            break;
                        //                        case DATANODE:
                        //                            break;
                        //                        case TASKTRACKER:
                        //                            break;
                    }
                    logStatusResults( trackerOperation, result );
                    break;
                case STOP:
                    switch ( nodeType )
                    {
                        case NAMENODE:
                            result = config.getNameNode()
                                           .execute( new RequestBuilder( Commands.getStopNameNodeCommand() ) );
                            break;
                        case JOBTRACKER:
                            result = config.getJobTracker()
                                           .execute( new RequestBuilder( Commands.getStopJobTrackerCommand() ) );
                            break;
                        //                        case SECONDARY_NAMENODE:
                        //                            break;
                        //                        case DATANODE:
                        //                            break;
                        //                        case TASKTRACKER:
                        //                            break;
                    }
                    logStatusResults( trackerOperation, result );
                    break;
                case STATUS:
                    switch ( nodeType )
                    {
                        case NAMENODE:
                            result = config.getNameNode()
                                           .execute( new RequestBuilder( Commands.getStatusNameNodeCommand() ) );
                            break;
                        case JOBTRACKER:
                            result = config.getJobTracker()
                                           .execute( new RequestBuilder( Commands.getStatusJobTrackerCommand() ) );
                            break;
                        case SECONDARY_NAMENODE:
                            result = config.getSecondaryNameNode()
                                           .execute( new RequestBuilder( Commands.getStatusNameNodeCommand() ) );
                            break;
                        //                        case DATANODE:
                        //                            break;
                        //                        case TASKTRACKER:
                        //                            break;
                    }
                    logStatusResults( trackerOperation, result );
                    break;
            }
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Command failed, %s", e.getMessage() ) );
        }
    }


    private void logStatusResults( TrackerOperation trackerOperation, CommandResult result )
    {
        NodeState nodeState = NodeState.UNKNOWN;
        if ( result.getStdOut() != null )
        {
            String[] array = result.getStdOut().split( "\n" );

            for ( String status : array )
            {
                if ( status.contains( "NameNode" ) )
                {
                    String temp = status.replaceAll(
                            Pattern.quote( "!(SecondaryNameNode is not running on this " + "machine)" ), "" ).
                                                replaceAll( "NameNode is ", "" );
                    if ( temp.toLowerCase().contains( "not" ) )
                    {
                        nodeState = NodeState.STOPPED;
                    }
                    else
                    {
                        nodeState = NodeState.RUNNING;
                    }
                    break;
                }
                else if ( status.contains( "JobTracker" ) )
                {
                    String temp = status.replaceAll( "JobTracker is ", "" );
                    if ( temp.toLowerCase().contains( "not" ) )
                    {
                        nodeState = NodeState.STOPPED;
                    }
                    else
                    {
                        nodeState = NodeState.RUNNING;
                    }
                    break;
                }
                else if ( status.contains( "DataNode" ) )
                {
                    String temp = status.replaceAll( "DataNode is ", "" );
                    if ( temp.toLowerCase().contains( "not" ) )
                    {
                        nodeState = NodeState.STOPPED;
                    }
                    else
                    {
                        nodeState = NodeState.RUNNING;
                    }
                    break;
                }
                else if ( status.contains( "TaskTracker" ) )
                {
                    String temp = status.replaceAll( "TaskTracker is ", "" );
                    if ( temp.toLowerCase().contains( "not" ) )
                    {
                        nodeState = NodeState.STOPPED;
                    }
                    else
                    {
                        nodeState = NodeState.RUNNING;
                    }
                    break;
                }
            }
        }

        if ( NodeState.UNKNOWN.equals( nodeState ) )
        {
            trackerOperation.addLogFailed( String.format( "Failed to check status of node" ) );
        }
        else
        {
            trackerOperation.addLogDone( String.format( "Node state is %s", nodeState ) );
        }
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
                    String.format( "Failed to setup Elasticsearch cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }


    @Override
    public void destroyCluster()
    {
        HadoopClusterConfig config = manager.getCluster( clusterName );
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
            manager.getPluginDAO().deleteInfo( HadoopClusterConfig.PRODUCT_KEY, config.getClusterName() );
            trackerOperation.addLogDone( "Cluster destroyed" );
        }
        catch ( EnvironmentDestroyException e )
        {
            trackerOperation.addLogFailed( String.format( "Error running command, %s", e.getMessage() ) );
            LOG.error( e.getMessage(), e );
        }
    }
}
