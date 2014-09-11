package org.safehaus.subutai.plugin.cassandra.impl.handler;


import com.google.common.collect.Sets;
import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.safehaus.subutai.plugin.cassandra.impl.Commands;

import java.util.UUID;


public class StartServiceHandler extends AbstractOperationHandler<CassandraImpl> {

    private String agentUUID;
    private String clusterName;


    public StartServiceHandler( final CassandraImpl manager, final String clusterName, final String agentUUID ) {
        super( manager, clusterName );
        this.agentUUID = agentUUID;
        this.clusterName = clusterName;
        productOperation = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
    }


    @Override
    public void run() {
        manager.getExecutor().execute( new Runnable() {
            Agent agent = manager.getAgentManager().getAgentByUUID( UUID.fromString( agentUUID ) );

            public void run() {
                Command startServiceCommand = Commands.getStartCommand( Sets.newHashSet( agent ) );
                manager.getCommandRunner().runCommand( startServiceCommand );
                if ( startServiceCommand.hasSucceeded() ) {
                    AgentResult ar = startServiceCommand.getResults().get( agent.getUuid() );
                    if ( ar.getStdOut().contains( "starting Cassandra ..." ) || ar.getStdOut().contains(
                            "is already running..." ) ) {
                        productOperation.addLog( ar.getStdOut() );
                        productOperation.addLogDone( "Start succeeded" );
                    }
                }
                else {
                    productOperation.addLogFailed( String.format( "Start failed, %s", startServiceCommand.getAllErrors() ) );
                }
            }
        } );
    }
}