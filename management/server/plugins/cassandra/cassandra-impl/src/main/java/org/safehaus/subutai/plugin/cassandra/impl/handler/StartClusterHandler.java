package org.safehaus.subutai.plugin.cassandra.impl.handler;


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

//import org.safehaus.subutai.common.protocol.Agent;


public class StartClusterHandler extends AbstractOperationHandler<CassandraImpl, CassandraClusterConfig>
{

    private static final Logger LOG = LoggerFactory.getLogger( StartClusterHandler.class.getName() );
    private String clusterName;
    String startCommand = ". /etc/profile && $CASSANDRA_HOME/bin/cassandra";
    String serviceStartCommand = "service cassandra start";
    String serviceStatusCommand = "service cassandra status";


    public StartClusterHandler( final CassandraImpl manager, final String clusterName )
    {
        super( manager, clusterName );
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
            trackerOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
            return;
        }

        Environment environment = manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() );
        for ( ContainerHost host : environment.getContainerHosts() )
        {
            try
            {
                CommandResult result = host.execute( new RequestBuilder( startCommand ) );
                if ( result.hasSucceeded() )
                {
                    trackerOperation.addLogDone( "Start succeeded" );
                }
                else
                {
                    trackerOperation.addLogFailed( String.format( "Start failed, %s", result.getStdErr() ) );
                }
            }
            catch ( CommandException e )
            {

                LOG.error( e.getMessage(), e );
            }
        }
    }
}
