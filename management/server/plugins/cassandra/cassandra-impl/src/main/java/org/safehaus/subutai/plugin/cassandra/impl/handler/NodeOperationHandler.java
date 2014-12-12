package org.safehaus.subutai.plugin.cassandra.impl.handler;


import java.util.Iterator;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.safehaus.subutai.plugin.cassandra.impl.Commands;
import org.safehaus.subutai.plugin.common.api.NodeOperationType;

import com.google.common.base.Preconditions;


/**
 * This class handles operations that are related to just one node.
 */
public class NodeOperationHandler extends AbstractOperationHandler<CassandraImpl, CassandraClusterConfig>
{

    private String clusterName;
    private String hostname;
    private NodeOperationType operationType;


    public NodeOperationHandler( final CassandraImpl manager, final String clusterName, final String hostname,
                                 NodeOperationType operationType )
    {
        super( manager, clusterName );
        this.hostname = hostname;
        this.clusterName = clusterName;
        this.operationType = operationType;
        this.trackerOperation = manager.getTracker()
                                       .createTrackerOperation( CassandraClusterConfig.PRODUCT_KEY,
                                               String.format( "Creating %s tracker object...", clusterName ) );
    }


    @Override
    public void run()
    {
        CassandraClusterConfig config = manager.getCluster( clusterName );
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
                    logResults( trackerOperation, result );
                    break;
                case STOP:
                    result = host.execute( new RequestBuilder( Commands.stopCommand ) );
                    logResults( trackerOperation, result );
                    break;
                case STATUS:
                    result = host.execute( new RequestBuilder( Commands.statusCommand ) );
                    logResults( trackerOperation, result );
                    break;
                case DESTROY:
                    destroyNode( host );
                    break;
            }
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Command failed, %s", e.getMessage() ) );
        }
    }


    public void destroyNode( ContainerHost host ){
        EnvironmentManager environmentManager  = manager.getEnvironmentManager();
        try
        {
            CassandraClusterConfig config = manager.getCluster( clusterName );
            environmentManager.removeContainer( config.getEnvironmentId(), host.getId() );
            config.getNodes().remove( host.getId() );
            manager.getPluginDAO().saveInfo( CassandraClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
            trackerOperation.addLog( String.format( "Cluster information is updated" ) );
            trackerOperation.addLogDone( String.format( "Container %s is removed from cluster", host.getHostname() ) );
        }
        catch ( EnvironmentManagerException e )
        {
            e.printStackTrace();
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
