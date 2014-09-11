package org.safehaus.subutai.plugin.cassandra.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.safehaus.subutai.plugin.cassandra.impl.Commands;


/**
 * Created by bahadyr on 8/25/14.
 */
public class CheckNodeHandler extends AbstractOperationHandler<CassandraImpl> {

    private ProductOperation po;
    private String clusterName;
    private String lxcHostname;


    public CheckNodeHandler( final CassandraImpl manager, String clusterName, String lxcHostname ) {
        super( manager, clusterName );
        this.clusterName = clusterName;
        this.lxcHostname = lxcHostname;
        po = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Checking cassandra on %s of %s cluster...", lxcHostname, clusterName ) );
    }


    @Override
    public void run() {
        CassandraClusterConfig cassandraConfig = manager.getCluster( clusterName );
        if ( cassandraConfig == null ) {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        final Agent node = manager.getAgentManager().getAgentByHostname( lxcHostname );
        if ( node == null ) {
            productOperation.addLogFailed( String.format( "Agent with hostname %s is not connected", lxcHostname ) );
            return;
        }
        if ( !cassandraConfig.getNodes().contains( node ) ) {
            productOperation.addLogFailed(
                    String.format( "Agent with hostname %s does not belong to cluster %s", lxcHostname, clusterName ) );
            return;
        }

        Command checkNodeCommand = Commands.getStatusCommand( node );
        manager.getCommandRunner().runCommand( checkNodeCommand );

        if ( checkNodeCommand.hasSucceeded() ) {
            productOperation.addLogDone( String.format( "Status on %s is %s", lxcHostname,
                    checkNodeCommand.getResults().get( node.getUuid() ).getStdOut() ) );
        }
        else {
            productOperation.addLogFailed(
                    String.format( "Failed to check status of %s, %s", lxcHostname, checkNodeCommand.getAllErrors() ) );
        }
    }
}
