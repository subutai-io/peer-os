package org.safehaus.subutai.plugin.elasticsearch.impl.handler;


import java.util.Iterator;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.Commands;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;

import com.google.common.base.Preconditions;


/**
 * This class handles operations that are related to just one node.
 *
 * TODO: add nodes and delete node operation should be implemented.
 */
public class NodeOperationHandler extends AbstractOperationHandler<ElasticsearchImpl, ElasticsearchClusterConfiguration>
{

    private String clusterName;
    private String hostname;
    private NodeOperationType operationType;


    public NodeOperationHandler( final ElasticsearchImpl manager, final String clusterName, final String hostname,
                                 NodeOperationType operationType )
    {
        super( manager, clusterName );
        this.hostname = hostname;
        this.clusterName = clusterName;
        this.operationType = operationType;
        this.trackerOperation = manager.getTracker()
                                       .createTrackerOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                                               String.format( "Creating %s tracker object...", clusterName ) );
    }


    @Override
    public void run()
    {
        ElasticsearchClusterConfiguration config = manager.getCluster( clusterName );
        if ( config == null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        Iterator iterator = environment.getContainerHosts().iterator();
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

        try
        {
            CommandResult result = null;
            switch ( operationType )
            {
                case START:
                    result = host.execute( new RequestBuilder( Commands.startCommand ) );
                    break;
                case STOP:
                    result = host.execute( new RequestBuilder( Commands.stopCommand ) );
                    break;
                case STATUS:
                    result = host.execute( new RequestBuilder( Commands.statusCommand ) );
                    break;
            }
            logResults( trackerOperation, result );
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Command failed, %s", e.getMessage() ) );
        }
    }


    public static void logResults( TrackerOperation po, CommandResult result )
    {
        Preconditions.checkNotNull( result );
        StringBuilder log = new StringBuilder();
        String status = "UNKNOWN";
        if ( result.getExitCode() == 0 )
        {
            status = result.getStdOut();
        }
        else if ( result.getExitCode() == 768 )
        {
            status = "elasticsearch is not running";
        }
        else
        {
            status = result.getStdOut();
        }
        log.append( String.format( "%s", status ) );
        po.addLogDone( log.toString() );
    }
}
