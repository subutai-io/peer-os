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


public class CheckServiceHandler extends AbstractOperationHandler<CassandraImpl> {

    private String agentUUID;
    private String clusterName;


    public CheckServiceHandler( final CassandraImpl manager, final String clusterName, final String agentUUID ) {
        super( manager, clusterName );
        this.agentUUID = agentUUID;
        this.clusterName = clusterName;
        this.productOperation = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
    }


    @Override
    public void run() {
        Agent agent = manager.getAgentManager().getAgentByUUID( UUID.fromString( agentUUID ) );
        Command statusServiceCommand = Commands.getStatusCommand( Sets.newHashSet( agent ) );
        manager.getCommandRunner().runCommand( statusServiceCommand );
        if ( statusServiceCommand.hasSucceeded() ) {
            AgentResult ar = statusServiceCommand.getResults().get( agent.getUuid() );
            if ( ar.getStdOut().contains( "is running" ) ) {
                productOperation.addLogDone( "Cassandra is running" );
            }
            else {
                productOperation.addLogFailed( "Cassandra is not running" );
            }
        }
        else {
            productOperation.addLogFailed( "Cassandra is not running" );
        }
    }
}