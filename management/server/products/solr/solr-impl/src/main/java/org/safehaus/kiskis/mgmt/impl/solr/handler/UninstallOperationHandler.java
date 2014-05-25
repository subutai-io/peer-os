package org.safehaus.kiskis.mgmt.impl.solr.handler;


import org.safehaus.kiskis.mgmt.api.lxcmanager.LxcDestroyException;
import org.safehaus.kiskis.mgmt.api.solr.Config;
import org.safehaus.kiskis.mgmt.impl.solr.SolrImpl;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;


public class UninstallOperationHandler extends AbstractOperationHandler<SolrImpl> {

    public UninstallOperationHandler( SolrImpl manager, String clusterName ) {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( Config.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );
    }


    @Override
    public void run() {
        Config config = manager.getCluster( clusterName );

        if ( config == null ) {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist\nOperation aborted", clusterName ) );
            return;
        }

        productOperation.addLog( "Destroying lxc containers..." );

        try {
            manager.getLxcManager().destroyLxcs( config.getNodes() );
            productOperation.addLog( "Lxc containers successfully destroyed" );
        }
        catch ( LxcDestroyException ex ) {
            productOperation.addLog( String.format( "%s, skipping...", ex.getMessage() ) );
        }

        productOperation.addLog( "Updating db..." );

        if ( manager.getDbManager().deleteInfo( Config.PRODUCT_KEY, config.getClusterName() ) ) {
            productOperation.addLogDone( "Cluster info deleted from DB\nDone" );
        }
        else {
            productOperation.addLogFailed( "Error while deleting cluster info from DB. Check logs.\nFailed" );
        }
    }
}
