package org.safehaus.subutai.impl.cassandra.handler;


import org.safehaus.subutai.api.cassandra.CassandraConfig;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.impl.cassandra.CassandraImpl;
import org.safehaus.subutai.impl.cassandra.Commands;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;


/**
 * Created by bahadyr on 8/25/14.
 */
public class StartAllNodesOperationHandler extends AbstractOperationHandler<CassandraImpl> {

    private ProductOperation po;
//    private CassandraConfig config;

    private String clusterName;

    public StartAllNodesOperationHandler( final CassandraImpl manager, final String clusterName ) {
        super( manager, clusterName );
        this.clusterName = clusterName;
        po = manager.getTracker().createProductOperation( CassandraConfig.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
    }


    @Override
    public void run() {
        final ProductOperation po = manager.getTracker().createProductOperation( CassandraConfig.PRODUCT_KEY,
                String.format( "Starting cluster %s", clusterName ) );

        manager.getExecutor().execute( new Runnable() {

            public void run() {
                CassandraConfig config = manager.getDbManager().getInfo( CassandraConfig.PRODUCT_KEY, clusterName,
                        CassandraConfig.class );
                if ( config == null ) {
                    po.addLogFailed(
                            String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
                    return;
                }
                Command startServiceCommand = Commands.getStartCommand( config.getNodes() );
                manager.getCommandRunner().runCommand( startServiceCommand );

                if ( startServiceCommand.hasSucceeded() ) {
                    po.addLogDone( "Start succeeded" );
                }
                else {
                    po.addLogFailed( String.format( "Start failed, %s", startServiceCommand.getAllErrors() ) );
                }
            }
        } );
    }
}
