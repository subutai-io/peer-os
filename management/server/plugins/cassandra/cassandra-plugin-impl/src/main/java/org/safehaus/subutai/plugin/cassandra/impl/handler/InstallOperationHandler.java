package org.safehaus.subutai.plugin.cassandra.impl.handler;


import org.safehaus.subutai.plugin.cassandra.api.CassandraConfig;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;


/**
 * Created by bahadyr on 8/25/14.
 */
public class InstallOperationHandler extends AbstractOperationHandler<CassandraImpl> {

    private ProductOperation po;
    private CassandraConfig config;


    public InstallOperationHandler( final CassandraImpl manager, final CassandraConfig config ) {
        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker().createProductOperation( CassandraConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", config.getClusterName() ) );
    }


    @Override
    public void run() {
        po.addLog( "Building environment..." );

        try {
            Environment env = manager.getEnvironmentManager()
                                     .buildEnvironmentAndReturn( manager.getDefaultEnvironmentBlueprint( config ) );

            ClusterSetupStrategy clusterSetupStrategy = manager.getClusterSetupStrategy( env, config, po );
            clusterSetupStrategy.setup();

            po.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( EnvironmentBuildException | ClusterSetupException e ) {
            po.addLogFailed( String.format( "Failed to setup cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }
}
