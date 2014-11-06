package org.safehaus.subutai.plugin.hadoop.impl.handler;


import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.OperationType;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;
import org.safehaus.subutai.plugin.hadoop.impl.common.Commands;


/**
 * This class handles operations that are related to just one node.
 *
 * TODO: add nodes and delete node operation should be implemented.
 */
public class NodeOperationHandler extends AbstractOperationHandler<HadoopImpl>
{

    private String clusterName;
    private String hostname;
    private OperationType operationType;
    private NodeType nodeType;
    private ExecutorService executor = Executors.newCachedThreadPool();


    public NodeOperationHandler( final HadoopImpl manager, final String clusterName, final String hostname,
                                 OperationType operationType, NodeType nodeType )
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


    private void runCommand( ContainerHost host, OperationType operationType, NodeType nodeType )
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
                    break;
                case STATUS:
                    switch ( nodeType )
                    {
                        case NAMENODE:
                            result = host.execute( new RequestBuilder( Commands.getStatusNameNodeCommand() ) );
                            break;
                        case JOBTRACKER:
                            result =  host.execute( new RequestBuilder( Commands.getStatusJobTrackerCommand() ) );
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
            ClusterOperationHandler.logStatusResults( trackerOperation, result, nodeType );
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Command failed, %s", e.getMessage() ) );
        }
    }


    private void excludeNode(){
        HadoopClusterConfig config = manager.getCluster( clusterName );
        ContainerHost host = findNodeInCluster( hostname );

        try
        {
            // TaskTracker
            host.execute( new RequestBuilder( Commands.getRemoveTaskTrackerCommand( host.getHostname() ) ) );
            host.execute( new RequestBuilder( Commands.getIncludeTaskTrackerCommand( host.getAgent().getListIP().get( 0 ) ) ) );

            // DataNode
            host.execute( new RequestBuilder( Commands.getRemoveDataNodeCommand( host.getHostname() ) ) );
            host.execute( new RequestBuilder( Commands.getIncludeDataNodeCommand( host.getAgent().getListIP().get( 0 ) ) ) );


            // refresh NameNode and JobTracker
            config.getNameNode().execute( new RequestBuilder( Commands.getRefreshNameNodeCommand() ) );
            config.getJobTracker().execute( new RequestBuilder( Commands.getRefreshJobTrackerCommand() ) );

        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Error running command, %s", e.getMessage() ) );
        }

        manager.getPluginDAO()
               .saveInfo( HadoopClusterConfig.PRODUCT_KEY, config.getClusterName(),
                       config );
        trackerOperation.addLogDone( "Cluster info saved to DB" );
    }


    private void includeNode(){

        HadoopClusterConfig config = manager.getCluster( clusterName );
        ContainerHost host = findNodeInCluster( hostname );

        try
        {
            /** DataNode Operations */
            // set data node
            config.getNameNode().execute(
                    new RequestBuilder( Commands.getSetDataNodeCommand( host.getHostname() ) ) );

            // remove data node from dfs.exclude
            config.getNameNode().execute(
                    new RequestBuilder( Commands.getExcludeDataNodeCommand( host.getAgent().getListIP().get( 0 ) ) ) );

            // stop data node
            host.execute( new RequestBuilder( Commands.getStopDataNodeCommand() ) );

            // start data node
            host.execute( new RequestBuilder( Commands.getStartDataNodeCommand() ) );

            // refresh name node
            config.getNameNode().execute( new RequestBuilder( Commands.getRefreshNameNodeCommand() ) );


            /** TaskTracker Operations */
            // set task tracker
            config.getJobTracker().execute(
                    new RequestBuilder( Commands.getSetTaskTrackerCommand( host.getHostname() ) ) );

            // remove task tracker from dfs.exclude
            config.getJobTracker().execute(
                    new RequestBuilder( Commands.getExcludeTaskTrackerCommand( host.getAgent().getListIP().get( 0 ) ) ) );

            // stop task tracker
            host.execute( new RequestBuilder( Commands.getStopTaskTrackerCommand() ) );

            // start task tracker
            host.execute( new RequestBuilder( Commands.getStartTaskTrackerCommand() ) );

            // refresh job tracker
            config.getNameNode().execute( new RequestBuilder( Commands.getRefreshJobTrackerCommand() ) );
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Error running command, %s", e.getMessage() ) );
        }

        manager.getPluginDAO()
               .saveInfo( HadoopClusterConfig.PRODUCT_KEY, config.getClusterName(),
                       config );
        trackerOperation.addLogDone( "Cluster info saved to DB" );
    }


    private ContainerHost findNodeInCluster( String hostname ){
        HadoopClusterConfig config = manager.getCluster( clusterName );

        if ( config == null )
        {
            trackerOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return null ;
        }

        if ( config.getNameNode() == null )
        {
            trackerOperation.addLogFailed( String.format( "NameNode on %s does not exist", clusterName ) );
            return null;
        }

        Environment environment =
                manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
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
}
