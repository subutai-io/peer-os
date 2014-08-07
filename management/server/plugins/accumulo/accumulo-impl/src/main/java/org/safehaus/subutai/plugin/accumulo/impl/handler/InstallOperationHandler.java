package org.safehaus.subutai.plugin.accumulo.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.plugin.accumulo.api.AccumuloClusterConfig;
import org.safehaus.subutai.plugin.accumulo.impl.AccumuloImpl;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;
import org.safehaus.subutai.shared.operation.AbstractOperationHandler;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;


/**
 * Created by dilshat on 5/6/14.
 */
public class InstallOperationHandler extends AbstractOperationHandler<AccumuloImpl> {
    private final AccumuloClusterConfig accumuloClusterConfig;
    private final HadoopClusterConfig hadoopClusterConfig;
    private final ZookeeperClusterConfig zookeeperClusterConfig;
    private final ProductOperation po;


    public InstallOperationHandler( AccumuloImpl manager, AccumuloClusterConfig accumuloClusterConfig,
                                    ZookeeperClusterConfig zookeeperClusterConfig,
                                    HadoopClusterConfig hadoopClusterConfig ) {
        super( manager, accumuloClusterConfig.getClusterName() );
        this.accumuloClusterConfig = accumuloClusterConfig;
        this.zookeeperClusterConfig = zookeeperClusterConfig;
        this.hadoopClusterConfig = hadoopClusterConfig;
        po = manager.getTracker().createProductOperation( AccumuloClusterConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", accumuloClusterConfig.getClusterName() ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        ClusterSetupStrategy accumuloSetupStrategy =
                manager.getClusterSetupStrategy( accumuloClusterConfig, hadoopClusterConfig, zookeeperClusterConfig,
                        po );

        try {
            accumuloSetupStrategy.setup();

            po.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( ClusterSetupException e ) {
            po.addLogFailed( String.format( "Failed to setup cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }
}
