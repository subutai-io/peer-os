package org.safehaus.subutai.plugin.cassandra.impl.handler;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;
import org.safehaus.subutai.plugin.cassandra.impl.CassandraImpl;

import java.util.UUID;


public class InstallClusterHandler extends AbstractOperationHandler<CassandraImpl> {

    private CassandraClusterConfig config;


    public InstallClusterHandler( final CassandraImpl manager, final CassandraClusterConfig config ) {
        super( manager, config.getClusterName() );
        this.config = config;
        productOperation = manager.getTracker().createProductOperation( CassandraClusterConfig.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", config.getClusterName() ) );
    }


    @Override
    public UUID getTrackerId() {
        return productOperation.getId();
    }


    @Override
    public void run() {
        productOperation.addLog( "Building environment..." );

        try {
            Environment env = manager.getEnvironmentManager()
                                     .buildEnvironmentAndReturn( manager.getDefaultEnvironmentBlueprint( config ) );

            ClusterSetupStrategy clusterSetupStrategy = manager.getClusterSetupStrategy( env, config, productOperation );
            clusterSetupStrategy.setup();

            productOperation.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( EnvironmentBuildException | ClusterSetupException e ) {
            productOperation.addLogFailed( String.format( "Failed to setup cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }
}
