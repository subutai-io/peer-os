package org.safehaus.subutai.plugin.cassandra.impl.handler;


import org.safehaus.subutai.plugin.cassandra.api.CassandraConfig;
import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;


/**
 * Created by bahadyr on 8/25/14.
 */
public class UninstallOperationHandler extends AbstractOperationHandler<CassandraImpl> {

    private ProductOperation po;
    private String clusterName;


    public UninstallOperationHandler( final CassandraImpl manager, final String clusterName) {
        super( manager, clusterName );
        this.clusterName = clusterName;
        po = manager.getTracker().createProductOperation( CassandraConfig.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );
    }





    @Override
    public void run() {
        po.addLog( "Building environment..." );
        CassandraConfig config = manager.getCluster( clusterName );
        if ( config == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        po.addLog( "Destroying lxc containers" );
        try {
            manager.getContainerManager().clonesDestroy( config.getNodes() );
            po.addLog( "Lxc containers successfully destroyed" );
        }
        catch ( LxcDestroyException ex ) {
            po.addLog( String.format( "%s, skipping...", ex.getMessage() ) );
        }

        po.addLog( "Deleting cluster information from database.." );

        try {
            manager.getDbManager().deleteInfo2( CassandraConfig.PRODUCT_KEY, config.getClusterName() );
            po.addLogDone( "Cluster info deleted from database" );
        }
        catch ( DBException e ) {
            po.addLogFailed( String.format( "Error while deleting cluster info from database, %s", e.getMessage() ) );
        }
    }
}
