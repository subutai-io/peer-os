package org.safehaus.subutai.plugin.hadoop.impl.handler;


import java.util.Iterator;
import java.util.List;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
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


    public NodeOperationHandler( final HadoopImpl manager, final String clusterName, final String hostname,
                                 OperationType operationType )
    {
        super( manager, clusterName );
        this.hostname = hostname;
        this.clusterName = clusterName;
        this.operationType = operationType;
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

        List<NodeType> roles = HadoopClusterConfig.getNodeRoles( config, host );
        for ( NodeType role : roles )
        {
            runCommand( host, operationType, role );
        }
    }


    private void runCommand( ContainerHost host, OperationType operationType, NodeType nodeType )
    {
        try
        {
            switch ( operationType )
            {
                case START:
                    switch ( nodeType )
                    {
                        case NAMENODE:
                            host.execute( new RequestBuilder( Commands.getStartNameNodeCommand() ) );
                            break;
                        case JOBTRACKER:
                            host.execute( new RequestBuilder( Commands.getStartJobTrackerCommand() ) );
                            break;
                        case TASKTRACKER:
                            host.execute( new RequestBuilder( Commands.getStartTaskTrackerCommand() ) );
                            break;
                        case DATANODE:
                            host.execute( new RequestBuilder( Commands.getStartDataNodeCommand() ) );
                            break;
                    }
                    break;
                case STOP:
                    switch ( nodeType )
                    {
                        case NAMENODE:
                            host.execute( new RequestBuilder( Commands.getStopNameNodeCommand() ) );
                            break;
                        case JOBTRACKER:
                            host.execute( new RequestBuilder( Commands.getStopJobTrackerCommand() ) );
                            break;
                        case TASKTRACKER:
                            host.execute( new RequestBuilder( Commands.getStopTaskTrackerCommand() ) );
                            break;
                        case DATANODE:
                            host.execute( new RequestBuilder( Commands.getStopDataNodeCommand() ) );
                            break;
                    }

                    break;
                case STATUS:
                    switch ( nodeType )
                    {
                        case NAMENODE:
                            host.execute( new RequestBuilder( Commands.getStatusNameNodeCommand() ) );
                            break;
                        case JOBTRACKER:
                            host.execute( new RequestBuilder( Commands.getStatusJobTrackerCommand() ) );
                            break;
                        case SECONDARY_NAMENODE:
                            host.execute( new RequestBuilder( Commands.getStatusNameNodeCommand() ) );
                            break;
                        case TASKTRACKER:
                            host.execute( new RequestBuilder( Commands.getStatusTaskTrackerCommand() ) );
                            break;
                        case DATANODE:
                            host.execute( new RequestBuilder( Commands.getStatusDataNodeCommand() ) );
                            break;
                    }
                    break;
            }
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Command failed, %s", e.getMessage() ) );
        }
    }


    //    public static void logResults( TrackerOperation po, CommandResult result,
    //                                   OperationType operationType, NodeType nodeType )
    //    {
    //        switch ( operationType ){
    //            case START:
    //                break;
    //            case STOP:
    //                break;
    //            case STATUS:
    //                break;
    //        }
    //        Preconditions.checkNotNull( result );
    //        StringBuilder log = new StringBuilder();
    //        String status = "UNKNOWN";
    //        if ( result.getExitCode() == 0 )
    //        {
    //            status = result.getStdOut();
    //        }
    //        else if ( result.getExitCode() == 768 )
    //        {
    //            status = "elasticsearch is not running";
    //        }
    //        else
    //        {
    //            status = result.getStdOut();
    //        }
    //        log.append( String.format( "%s", status ) );
    //        po.addLogDone( log.toString() );
    //    }
}
