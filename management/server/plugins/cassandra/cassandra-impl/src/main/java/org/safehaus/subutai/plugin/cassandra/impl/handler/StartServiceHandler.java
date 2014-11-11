package org.safehaus.subutai.plugin.cassandra.impl.handler;


import java.util.Iterator;
import java.util.UUID;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StartServiceHandler extends AbstractOperationHandler<CassandraImpl, CassandraClusterConfig>
{

    private static final Logger LOG = LoggerFactory.getLogger( StartServiceHandler.class.getName() );
    private UUID containerId;
    String serviceStartCommand = "service cassandra start";


    public StartServiceHandler( final CassandraImpl manager, final String clusterName, UUID containerId )
    {
        super( manager, clusterName );
        this.containerId = containerId;
        trackerOperation = manager.getTracker().createTrackerOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Starting %s container...", clusterName ) );
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
            if ( host.getId().equals( containerId ) )
            {
                break;
            }
        }

        if ( host == null )
        {
            trackerOperation.addLogFailed( String.format( "No Container with ID %s", containerId ) );
            return;
        }


        try
        {
            CommandResult result = host.execute( new RequestBuilder( serviceStartCommand ) );
            if ( result.hasSucceeded() )
            {
                if ( result.getStdOut().contains( "starting Cassandra ..." ) || result.getStdOut().contains(
                        "is already running..." ) )
                {
                    trackerOperation.addLog( result.getStdOut() );
                    trackerOperation.addLogDone( "Start succeeded" );
                }
            }
            else
            {
                trackerOperation.addLogFailed( String.format( "Start failed, %s", result.getStdErr() ) );
            }
        }
        catch ( CommandException e )
        {
            LOG.error( e.getMessage(), e );
            trackerOperation.addLogFailed( String.format( "Command failed, %s", e.getMessage() ) );
        }
    }
}