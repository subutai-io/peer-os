package org.safehaus.subutai.plugin.cassandra.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.command.api.Command;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.safehaus.subutai.plugin.cassandra.impl.Commands;


/**
 * Created by bahadyr on 8/25/14.
 */
public class CheckClusterHandler extends AbstractOperationHandler<CassandraImpl> {

    private ProductOperation po;
    //    private CassandraConfig config;
    private String clusterName;


    public CheckClusterHandler( final CassandraImpl manager, final String clusterName ) {
        super( manager, clusterName );
        this.clusterName = clusterName;
        po = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Checking all nodes of %s cluster...", clusterName ) );
    }


    @Override
    public void run() {
        final ProductOperation po = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Checking cluster %s", clusterName ) );

        manager.getExecutor().execute( new Runnable() {

            public void run() {
                CassandraClusterConfig config = manager.getDbManager()
                                                       .getInfo( CassandraClusterConfig.PRODUCT_KEY, clusterName,
                                                               CassandraClusterConfig.class );
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
