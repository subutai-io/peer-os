package org.safehaus.subutai.plugin.cassandra.impl.handler;


import java.util.Set;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;


public class StopClusterHandler extends AbstractOperationHandler<CassandraImpl>
{

    private String clusterName;


    public StopClusterHandler( final CassandraImpl manager, final String clusterName )
    {
        super( manager, clusterName );
        this.clusterName = clusterName;
        productOperation = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", clusterName ) );
    }


    @Override
    public void run()
    {
        CassandraClusterConfig config = manager.getCluster( clusterName );
        if ( config == null )
        {
            productOperation.addLogFailed(
                    String.format( "Cluster with name %s does not exist. Operation aborted", clusterName ) );
            return;
        }

        Set<Agent> agentSet = manager.getAgentManager().returnAgentsByGivenUUIDSet( config.getNodes() );
        Command stopServiceCommand = manager.getCommands().getStopCommand(agentSet );
        manager.getCommandRunner().runCommand( stopServiceCommand );

        if ( stopServiceCommand.hasSucceeded() )
        {
            productOperation.addLogDone( "Stop succeeded" );
        }
        else
        {
            productOperation.addLogFailed( String.format( "Start failed, %s", stopServiceCommand.getAllErrors() ) );
        }
    }
}
