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
public class StartClusterHandler extends AbstractOperationHandler<CassandraImpl> {

    private ProductOperation po;
    //    private CassandraConfig config;

    private String clusterName;


    public StartClusterHandler( final CassandraImpl manager, final String clusterName ) {
        super( manager, clusterName );
        this.clusterName = clusterName;
        po = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Starting %s cluster...", clusterName ) );
    }


    @Override
    public void run() {
        final ProductOperation po = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Starting cluster %s", clusterName ) );

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
