package org.safehaus.subutai.plugin.hadoop.impl.handler;


import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.core.container.api.lxcmanager.LxcDestroyException;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.impl.HadoopImpl;

import com.google.common.collect.Sets;


public class UninstallOperationHandler extends AbstractOperationHandler<HadoopImpl> {

    public UninstallOperationHandler( HadoopImpl manager, String clusterName ) {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( HadoopClusterConfig.PRODUCT_KEY,
                String.format( "Destroying installation %s", clusterName ) );
    }


    @Override
    public void run() {
        HadoopClusterConfig hadoopClusterConfig = manager.getCluster( clusterName );

        if ( hadoopClusterConfig == null ) {
            productOperation.addLogFailed( String.format( "Installation with name %s does not exist", clusterName ) );
            return;
        }

        productOperation.addLog( "Destroying lxc containers..." );

        try {
            manager.getContainerManager().clonesDestroy( Sets.newHashSet( hadoopClusterConfig.getAllNodes() ) );
            productOperation.addLog( "Lxc containers successfully destroyed" );
        }
        catch ( LxcDestroyException ex ) {
            productOperation.addLog( String.format( "%s, skipping...", ex.getMessage() ) );
        }

        productOperation.addLog( "Updating db..." );

        try {
            manager.getPluginDAO().deleteInfo( HadoopClusterConfig.PRODUCT_KEY, hadoopClusterConfig.getClusterName() );
            productOperation.addLogDone( "Information updated in database" );
        }
        catch ( DBException e ) {
            productOperation
                    .addLogFailed( String.format( "Failed to update information in database, %s", e.getMessage() ) );
        }
    }
}
