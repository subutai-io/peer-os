package org.safehaus.subutai.plugin.solr.impl.handler;


import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.plugin.solr.impl.SolrImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;


public class UninstallOperationHandler extends AbstractOperationHandler<SolrImpl> {

    public UninstallOperationHandler( SolrImpl manager, String clusterName ) {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( SolrClusterConfig.PRODUCT_KEY,
                String.format( "Destroying installation %s", clusterName ) );
    }


    @Override
    public void run() {
        SolrClusterConfig solrClusterConfig = manager.getCluster( clusterName );

        if ( solrClusterConfig == null ) {
            productOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        productOperation.addLog( "Destroying lxc containers..." );

        try {
            manager.getContainerManager().clonesDestroy( solrClusterConfig.getNodes() );
            productOperation.addLog( "Lxc containers successfully destroyed" );
        }
        catch ( LxcDestroyException ex ) {
            productOperation.addLog( String.format( "%s, skipping...", ex.getMessage() ) );
        }

        productOperation.addLog( "Updating db..." );

        manager.getPluginDAO().deleteInfo( SolrClusterConfig.PRODUCT_KEY, solrClusterConfig.getClusterName() );
        productOperation.addLogDone( "Information updated in database" );
    }
}
