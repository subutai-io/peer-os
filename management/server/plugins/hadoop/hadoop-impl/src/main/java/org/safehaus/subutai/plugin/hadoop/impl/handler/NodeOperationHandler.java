package org.safehaus.subutai.plugin.hadoop.impl.handler;


import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.plugin.hadoop.impl.Commands;


/**
 * This class handles operations that are related to just one node.
 *
 * TODO: add nodes and delete node operation should be implemented.
 */
public class NodeOperationHandler extends AbstractOperationHandler<HadoopImpl, HadoopClusterConfig>
{

    private String clusterName;
    private String hostname;
    private NodeOperationType operationType;
    private NodeType nodeType;
    private ExecutorService executor = Executors.newCachedThreadPool();


    public NodeOperationHandler( final HadoopImpl manager, final String clusterName, final String hostname,
                                 NodeOperationType operationType, NodeType nodeType )
    {
        super( manager, clusterName );
        this.hostname = hostname;
        this.clusterName = clusterName;
        this.operationType = operationType;
        this.nodeType = nodeType;
        this.trackerOperation = manager.getTracker().createTrackerOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Creating %s tracker object...", clusterName ) );
    }


    @Override
    public void run()
    {
        HadoopClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        Iterator iterator = environment.getContainers().iterator();
        ContainerHost host = null;
        while ( iterator.hasNext() )
        {
            host = ( ContainerHost ) iterator.next();
            if ( host.getHostname().equals( hostname ) )
            {
                break;
            }
        }

        if ( host == null )
        {
            trackerOperation.addLogFailed( String.format( "No Container with ID %s", hostname ) );
            return;
        }

        runCommand( host, operationType, nodeType );
        //        List<NodeType> roles = HadoopClusterConfig.getNodeRoles( config, host );
        //        for ( NodeType role : roles )
        //        {
        //            runCommand( host, operationType, role );
        //        }
    }


    private void runCommand( ContainerHost host, NodeOperationType operationType, NodeType nodeType )
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
                            result = host.execute( new RequestBuilder( Commands.getStartNameNodeCommand() ) );
                            break;
                        case JOBTRACKER:
                            result = host.execute( new RequestBuilder( Commands.getStartJobTrackerCommand() ) );
                            break;
                        case TASKTRACKER:
                            result = host.execute( new RequestBuilder( Commands.getStartTaskTrackerCommand() ) );
                            break;
                        case DATANODE:
                            result = host.execute( new RequestBuilder( Commands.getStartDataNodeCommand() ) );
                            break;
                    }
                    ClusterOperationHandler.logStatusResults( trackerOperation, result, nodeType );
                    break;
                case STOP:
                    switch ( nodeType )
                    {
                        case NAMENODE:
                            result = host.execute( new RequestBuilder( Commands.getStopNameNodeCommand() ) );
                            break;
                        case JOBTRACKER:
                            result = host.execute( new RequestBuilder( Commands.getStopJobTrackerCommand() ) );
                            break;
                        case TASKTRACKER:
                            result = host.execute( new RequestBuilder( Commands.getStopTaskTrackerCommand() ) );
                            break;
                        case DATANODE:
                            result = host.execute( new RequestBuilder( Commands.getStopDataNodeCommand() ) );
                            break;
                    }
                    ClusterOperationHandler.logStatusResults( trackerOperation, result, nodeType );
                    break;
                case STATUS:
                    switch ( nodeType )
                    {
                        case NAMENODE:
                            result = host.execute( new RequestBuilder( Commands.getStatusNameNodeCommand() ) );
                            break;
                        case JOBTRACKER:
                            result = host.execute( new RequestBuilder( Commands.getStatusJobTrackerCommand() ) );
                            break;
                        case SECONDARY_NAMENODE:
                            result = host.execute( new RequestBuilder( Commands.getStatusNameNodeCommand() ) );
                            break;
                        case TASKTRACKER:
                            result = host.execute( new RequestBuilder( Commands.getStatusTaskTrackerCommand() ) );
                            break;
                        case DATANODE:
                            result = host.execute( new RequestBuilder( Commands.getStatusDataNodeCommand() ) );
                            break;
                    }
                    ClusterOperationHandler.logStatusResults( trackerOperation, result, nodeType );
                    break;
                case EXCLUDE:
                    executor.execute( new Runnable()
                    {
                        public void run()
                        {
                            excludeNode();
                        }
                    } );
                    break;
                case INCLUDE:
                    executor.execute( new Runnable()
                    {
                        public void run()
                        {
                            includeNode();
                        }
                    } );
                    break;
            }
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Command failed, %s", e.getMessage() ) );
        }
    }


    private void excludeNode()
    {
        HadoopClusterConfig config = manager.getCluster( clusterName );
        ContainerHost host = findNodeInCluster( hostname );

        try
        {
            // TaskTracker
            host.execute( new RequestBuilder( Commands.getRemoveTaskTrackerCommand( host.getHostname() ) ) );
            host.execute( new RequestBuilder(
                    Commands.getIncludeTaskTrackerCommand( host.getAgent().getListIP().get( 0 ) ) ) );

            // DataNode
            host.execute( new RequestBuilder( Commands.getRemoveDataNodeCommand( host.getHostname() ) ) );
            host.execute(
                    new RequestBuilder( Commands.getIncludeDataNodeCommand( host.getAgent().getListIP().get( 0 ) ) ) );


            // refresh NameNode and JobTracker
            ContainerHost namenode = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() )
                                            .getContainerHostByUUID( config.getNameNode() );
            ContainerHost jobtracker = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() )
                                              .getContainerHostByUUID( config.getJobTracker() );

            namenode.execute( new RequestBuilder( Commands.getRefreshNameNodeCommand() ) );
            jobtracker.execute( new RequestBuilder( Commands.getRefreshJobTrackerCommand() ) );
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Error running command, %s", e.getMessage() ) );
        }

        config.getBlockedAgents().add( host.getAgent().getUuid() );
        manager.getPluginDAO().saveInfo( HadoopClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
        trackerOperation.addLogDone( "Cluster info saved to DB" );
    }


    private ContainerHost findNodeInCluster( String hostname )
    {
        HadoopClusterConfig config = manager.getCluster( clusterName );

        if ( config == null )
        {
            trackerOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return null;
        }

        if ( config.getNameNode() == null )
        {
            trackerOperation.addLogFailed( String.format( "NameNode on %s does not exist", clusterName ) );
            return null;
        }

        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        Iterator iterator = environment.getContainers().iterator();

        ContainerHost host = null;
        while ( iterator.hasNext() )
        {
            host = ( ContainerHost ) iterator.next();
            if ( host.getHostname().equals( hostname ) )
            {
                break;
            }
        }

        if ( host == null )
        {
            trackerOperation.addLogFailed( String.format( "No Container with ID %s", host.getId() ) );
            return null;
        }
        return host;
    }


    private void includeNode()
    {

        HadoopClusterConfig config = manager.getCluster( clusterName );
        ContainerHost host = findNodeInCluster( hostname );

        try
        {
            ContainerHost namenode = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() )
                                            .getContainerHostByUUID( config.getNameNode() );
            ContainerHost jobtracker = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() )
                                              .getContainerHostByUUID( config.getJobTracker() );

            /** DataNode Operations */
            // set data node
            namenode.execute( new RequestBuilder( Commands.getSetDataNodeCommand( host.getHostname() ) ) );

            // remove data node from dfs.exclude
            namenode.execute(
                    new RequestBuilder( Commands.getExcludeDataNodeCommand( host.getAgent().getListIP().get( 0 ) ) ) );

            // stop data node
            host.execute( new RequestBuilder( Commands.getStopDataNodeCommand() ) );

            // start data node
            host.execute( new RequestBuilder( Commands.getStartDataNodeCommand() ) );

            // refresh name node
            namenode.execute( new RequestBuilder( Commands.getRefreshNameNodeCommand() ) );


            /** TaskTracker Operations */
            // set task tracker
            jobtracker.execute( new RequestBuilder( Commands.getSetTaskTrackerCommand( host.getHostname() ) ) );

            // remove task tracker from dfs.exclude
            jobtracker.execute( new RequestBuilder(
                    Commands.getExcludeTaskTrackerCommand( host.getAgent().getListIP().get( 0 ) ) ) );

            // stop task tracker
            host.execute( new RequestBuilder( Commands.getStopTaskTrackerCommand() ) );

            // start task tracker
            host.execute( new RequestBuilder( Commands.getStartTaskTrackerCommand() ) );

            // refresh job tracker
            jobtracker.execute( new RequestBuilder( Commands.getRefreshJobTrackerCommand() ) );
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Error running command, %s", e.getMessage() ) );
        }

        config.getBlockedAgents().remove( host.getAgent().getUuid() );
        manager.getPluginDAO().saveInfo( HadoopClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
        trackerOperation.addLogDone( "Cluster info saved to DB" );
    }
}
