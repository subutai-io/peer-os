package org.safehaus.subutai.plugin.cassandra.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;


public class UninstallClusterHandler extends AbstractOperationHandler<CassandraImpl> {

    private String clusterName;

    public UninstallClusterHandler( final CassandraImpl manager, final String clusterName ) {
        super( manager, clusterName );
        this.clusterName = clusterName;
        productOperation = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );
    }


    @Override
    public void run() {
        productOperation.addLog( "Building environment..." );
        CassandraClusterConfig config = manager.getCluster( clusterName );
        if ( config == null ) {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        productOperation.addLog( "Destroying lxc containers" );
        try {
            manager.getContainerManager().clonesDestroy( config.getNodes() );
            productOperation.addLog( "Lxc containers successfully destroyed" );
        }
        catch ( LxcDestroyException ex ) {
            productOperation.addLog( String.format( "%s, skipping...", ex.getMessage() ) );
        }

        productOperation.addLog( "Deleting cluster information from database.." );

        try {
            manager.getPluginDAO().deleteInfo( CassandraClusterConfig.PRODUCT_KEY, config.getClusterName() );
            productOperation.addLogDone( "Cluster info deleted from database" );
        }
        catch ( DBException e ) {
            productOperation.addLogFailed( String.format( "Error while deleting cluster info from database, %s", e.getMessage() ) );
        }
    }
}
