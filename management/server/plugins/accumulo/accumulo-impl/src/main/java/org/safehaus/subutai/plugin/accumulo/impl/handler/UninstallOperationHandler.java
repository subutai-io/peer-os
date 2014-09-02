package org.safehaus.subutai.plugin.accumulo.impl.handler;


import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.ClusterConfiguration;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.protocol.ClusterConfigurationException;

import java.util.UUID;


/**
 * Handles destroy cluster operation
 */
public class UninstallOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
    
    public UninstallOperationHandler( AccumuloImpl manager, String clusterName ) {
        super( manager, clusterName );
        productOperation = manager.getTracker().createProductOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return productOperation.getId();
    }


    @Override
    public void run() {
        AccumuloClusterConfig accumuloClusterConfig = manager.getCluster( clusterName );
        if ( accumuloClusterConfig == null ) {
            productOperation.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        try {
            new ClusterConfiguration( productOperation, manager ).destroyCluster( accumuloClusterConfig );
            productOperation.addLogDone( "Cluster successfully destroyed" );
        }
        catch ( ClusterConfigurationException e ) {
            productOperation.addLogFailed( String.format( "Failed to destroy cluster, %s", e.getMessage() ) );
        }
    }
}
