package org.safehaus.subutai.plugin.cassandra.impl.handler;


import java.util.Iterator;
import java.util.UUID;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StartServiceHandler extends AbstractOperationHandler<CassandraImpl>
{

    private static final Logger LOG = LoggerFactory.getLogger( StartServiceHandler.class.getName() );
    private String clusterName;
    private UUID agentUUID;


    public StartServiceHandler( final CassandraImpl manager, final String clusterName, UUID agentUUID )
    {
        super( manager, clusterName );
        this.agentUUID = agentUUID;
        this.clusterName = clusterName;
        trackerOperation = manager.getTracker().createTrackerOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
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

        if ( !config.getNodes().contains( UUID.fromString( host.getId().toString() ) ) )
        {
            trackerOperation.addLogFailed(
                    String.format( "Agent with ID %s does not belong to cluster %s", host.getId(), clusterName ) );
            return;
        }

        try
        {
            CommandResult result = host.execute( new RequestBuilder( "service cassandra start" ) );
            if ( result.getExitCode() == 0 )
            {
                result = host.execute( new RequestBuilder( "service cassandra status" ) );
                if ( result.getExitCode() == 0 )
                {
                    if ( result.getStdOut().contains( "running..." ) )
                    {
                        trackerOperation.addLog( result.getStdOut() );
                        trackerOperation.addLogDone( "Start succeeded" );
                    }
                    else
                    {
                        trackerOperation.addLogFailed( String.format( "Unexpected result, %s", result.getStdErr() ) );
                    }
                }
                else
                {
                    trackerOperation.addLogFailed( String.format( "Start failed, %s", result.getStdErr() ) );
                }
            }
            else
            {
                trackerOperation.addLogFailed( String.format( "Start failed, %s", result.getStdErr() ) );
            }
        }
        catch ( CommandException e )
        {
            trackerOperation.addLogFailed( String.format( "Start failed, %s", e.getMessage() ) );
        }
    }
}