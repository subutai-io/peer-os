package org.safehaus.subutai.plugin.cassandra.impl.handler;


import org.safehaus.subutai.plugin.cassandra.api.CassandraConfig;
import org.safehaus.subutai.core.commandrunner.api.Command;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.safehaus.subutai.plugin.cassandra.impl.Commands;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;


/**
 * Created by bahadyr on 8/25/14.
 */
public class CheckAllNodesOperationHandler extends AbstractOperationHandler<CassandraImpl> {

    private ProductOperation po;
//    private CassandraConfig config;
    private String clusterName;


    public CheckAllNodesOperationHandler( final CassandraImpl manager, final String clusterName) {
        super( manager, clusterName );
        this.clusterName = clusterName;
        po = manager.getTracker().createProductOperation( CassandraConfig.PRODUCT_KEY,
                String.format( "Checking all nodes of %s cluster...", clusterName ) );
    }


    @Override
    public void run() {
        final ProductOperation po = manager.getTracker().createProductOperation( CassandraConfig.PRODUCT_KEY,
                String.format( "Checking cluster %s", clusterName ) );

        manager.getExecutor().execute( new Runnable() {

            public void run() {
                CassandraConfig config = manager.getDbManager().getInfo( CassandraConfig.PRODUCT_KEY, clusterName,
                        CassandraConfig.class );
                if ( config == null ) {
                    po.addLogFailed(
                            String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
                    return;
                }

                Command checkStatusCommand = Commands.getStatusCommand( config.getNodes() );
                manager.getCommandRunner().runCommand( checkStatusCommand );

                if ( checkStatusCommand.hasSucceeded() ) {
                    po.addLogDone( "All nodes are running." );
                }
                else {
                    po.addLogFailed( String.format( "Check status failed, %s", checkStatusCommand.getAllErrors() ) );
                }
            }
        } );
    }
}
