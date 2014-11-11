package org.safehaus.subutai.plugin.cassandra.impl.handler;


import java.util.Iterator;
import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CheckNodeHandler extends AbstractOperationHandler<CassandraImpl, CassandraClusterConfig>
{

    private static final Logger LOG = LoggerFactory.getLogger( CheckServiceHandler.class.getName() );
    private UUID agentUUID;
    String serviceStatusCommand = "service cassandra status";


    public CheckNodeHandler( final CassandraImpl manager, final String clusterName, UUID agentUUID )
    {
        super( manager, clusterName );
        this.agentUUID = agentUUID;
        trackerOperation = manager.getTracker().createTrackerOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Checking %s cluster...", clusterName ) );
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
        Iterator iterator = environment.getContainers().iterator();

        ContainerHost host = null;
        while ( iterator.hasNext() )
        {
            host = ( ContainerHost ) iterator.next();
            if ( host.getId().equals( agentUUID ) )
            {
                break;
            }
        }

        if ( host == null )
        {
            trackerOperation.addLogFailed( String.format( "No Container with ID %s", agentUUID ) );
            return;
        }

        try
        {
            CommandResult result = host.execute( new RequestBuilder( serviceStatusCommand ) );
            logStatusResults( trackerOperation, result );
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Command failed, %s", e.getMessage() ) );
        }
    }


    private void logStatusResults( TrackerOperation po, CommandResult result )
    {

        StringBuilder log = new StringBuilder();

        String status = "UNKNOWN";
        if ( result.getExitCode() == 0 )
        {
            status = "Cassandra is running";
        }
        else if ( result.getExitCode() == 768 )
        {
            status = "Cassandra is not running";
        }
        else
        {
            status = result.getStdOut();
        }

        log.append( String.format( "%s", status ) );

        po.addLogDone( log.toString() );
    }
}
