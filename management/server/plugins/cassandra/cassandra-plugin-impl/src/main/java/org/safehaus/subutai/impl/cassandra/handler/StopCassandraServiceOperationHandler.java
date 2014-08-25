package org.safehaus.subutai.impl.cassandra.handler;


import java.util.UUID;

import org.safehaus.subutai.api.cassandra.CassandraConfig;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.impl.cassandra.CassandraImpl;
import org.safehaus.subutai.impl.cassandra.Commands;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Util;


/**
 * Created by bahadyr on 8/25/14.
 */
public class StopCassandraServiceOperationHandler extends AbstractOperationHandler<CassandraImpl> {

    private ProductOperation po;
    //    private CassandraConfig config;
    private String agentUUID;

    private String clusterName;


    public StopCassandraServiceOperationHandler( final CassandraImpl manager, final String clusterName, final String agentUUID ) {
        super( manager, clusterName );
        this.agentUUID = agentUUID;
        this.clusterName = clusterName;
        po = manager.getTracker().createProductOperation( CassandraConfig.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
    }


    @Override
    public void run() {
        final ProductOperation po = manager.getTracker().createProductOperation( CassandraConfig.PRODUCT_KEY,
                String.format( "Stopping Cassandra service on %s", agentUUID ) );
        manager.getExecutor().execute( new Runnable() {
            Agent agent = manager.getAgentManager().getAgentByUUID( UUID.fromString( agentUUID ) );


            public void run() {
                Command stopServiceCommand = Commands.getStopCommand( Util.wrapAgentToSet( agent ) );
                manager.getCommandRunner().runCommand( stopServiceCommand );
                if ( stopServiceCommand.hasSucceeded() ) {
                    AgentResult ar = stopServiceCommand.getResults().get( agent.getUuid() );
                    po.addLog( ar.getStdOut() );
                    po.addLogDone( "Stop succeeded" );
                }
                else {
                    po.addLogFailed( String.format( "Stop failed, %s", stopServiceCommand.getAllErrors() ) );
                }
            }
        } );
    }
}