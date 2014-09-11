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

public class StopServiceHandler extends AbstractOperationHandler<CassandraImpl> {

    private String agentUUID;
    private String clusterName;


    public StopServiceHandler( final CassandraImpl manager, final String clusterName, final String agentUUID ) {
        super( manager, clusterName );
        this.agentUUID = agentUUID;
        this.clusterName = clusterName;
        productOperation = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Stopping %s cluster...", clusterName ) );
    }


    @Override
    public void run() {
        manager.getExecutor().execute( new Runnable() {
            Agent agent = manager.getAgentManager().getAgentByUUID( UUID.fromString( agentUUID ) );


            public void run() {
                Command stopServiceCommand = Commands.getStopCommand( Sets.newHashSet( agent ) );
                manager.getCommandRunner().runCommand( stopServiceCommand );
                if ( stopServiceCommand.hasSucceeded() ) {
                    AgentResult ar = stopServiceCommand.getResults().get( agent.getUuid() );
                    productOperation.addLog( ar.getStdOut() );
                    productOperation.addLogDone( "Stop succeeded" );
                }
                else {
                    productOperation.addLogFailed( String.format( "Stop failed, %s", stopServiceCommand.getAllErrors() ) );
                }
            }
        } );
    }
}