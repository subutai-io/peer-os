package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.accumulo.impl.ClusterConfiguration;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterConfigurationException;


/**
 * Handles destroy cluster operation
 */
public class UninstallOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
    private final ProductOperation po;


    public UninstallOperationHandler( AccumuloImpl manager, String clusterName ) {
        super( manager, clusterName );
        po = manager.getTracker().createProductOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Destroying cluster %s", clusterName ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        AccumuloClusterConfig accumuloClusterConfig = manager.getCluster( clusterName );
        if ( accumuloClusterConfig == null ) {
            po.addLogFailed( String.format( "Cluster with name %s does not exist", clusterName ) );
            return;
        }

        try {
            new ClusterConfiguration( po, manager ).destroyCluster( accumuloClusterConfig );
            po.addLogDone( "Cluster successfully destroyed" );
        }
        catch ( ClusterConfigurationException e ) {
            po.addLogFailed( String.format( "Failed to destroy cluster, %s", e.getMessage() ) );
        }
    }
}
