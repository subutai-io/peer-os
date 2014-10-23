package org.safehaus.subutai.plugin.cassandra.impl.handler;


import java.util.Set;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;


public class StartClusterHandler extends AbstractOperationHandler<CassandraImpl>
{

    private String clusterName;


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

        Set<Agent> agentSet = manager.getAgentManager().returnAgentsByGivenUUIDSet( config.getNodes() );
        Command startServiceCommand = manager.getCommands().getStartCommand( agentSet );
        manager.getCommandRunner().runCommand( startServiceCommand );

        if ( startServiceCommand.hasSucceeded() )
        {
            trackerOperation.addLogDone( "Start succeeded" );
        }
        else
        {
            trackerOperation.addLogFailed( String.format( "Start failed, %s", startServiceCommand.getAllErrors() ) );
        }
    }
}
