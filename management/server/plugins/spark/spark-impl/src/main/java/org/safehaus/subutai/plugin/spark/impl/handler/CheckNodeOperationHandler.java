package org.safehaus.subutai.plugin.spark.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;
import org.safehaus.subutai.plugin.spark.impl.Commands;
import org.safehaus.subutai.plugin.spark.impl.SparkImpl;


/**
 * Created by dilshat on 5/7/14.
 */
public class CheckNodeOperationHandler extends AbstractOperationHandler<SparkImpl> {
    private final ProductOperation po;
    private final String lxcHostname;


    public CheckNodeOperationHandler( SparkImpl manager, String clusterName, String lxcHostname ) {
        super( manager, clusterName );
        this.lxcHostname = lxcHostname;
        po = SparkImpl.getTracker().createProductOperation( SparkClusterConfig.PRODUCT_KEY,
                String.format( "Checking state of %s in %s", lxcHostname, clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        productOperation = po;
        SparkClusterConfig config = manager.getCluster( clusterName );
        if ( config == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        Agent node = SparkImpl.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null ) {
            po.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }

        if ( !config.getAllNodes().contains( node ) ) {
            po.addLogFailed( String.format( "Node %s does not belong to this cluster", lxcHostname ) );
            return;
        }

        po.addLog( "Checking node..." );

        Command checkNodeCommand = Commands.getStatusAllCommand( node );
        SparkImpl.getCommandRunner().runCommand( checkNodeCommand );

        AgentResult res = checkNodeCommand.getResults().get( node.getUuid() );
        if ( checkNodeCommand.hasSucceeded() ) {
            po.addLogDone( String.format( "%s", res.getStdOut() ) );
        }
        else {
            po.addLogFailed( String.format( "Faied to check status, %s", checkNodeCommand.getAllErrors() ) );
        }
    }
}
