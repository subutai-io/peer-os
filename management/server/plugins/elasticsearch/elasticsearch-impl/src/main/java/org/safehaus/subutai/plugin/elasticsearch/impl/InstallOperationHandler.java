package org.safehaus.subutai.plugin.elasticsearch.impl;


import java.util.UUID;

import org.safehaus.subutai.api.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.elasticsearch.api.Config;

import com.google.common.base.Strings;


public class InstallOperationHandler extends AbstractOperationHandler<ElasticsearchImpl> {

    private final ProductOperation po;
    private final Config config;

    public InstallOperationHandler( ElasticsearchImpl manager, Config config ) {

        super( manager, config.getClusterName() );
        this.config = config;
        po = manager.getTracker().createProductOperation( Config.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", config.getClusterName() ) );
    }


    @Override
    public UUID getTrackerId() {
        return po.getId();
    }


    @Override
    public void run() {
        if ( Strings.isNullOrEmpty( config.getClusterName() ) ) {
            po.addLogFailed( "Malformed configuration" );
            return;
        }

        if ( manager.getCluster( clusterName ) != null ) {
            po.addLogFailed( String.format( "Cluster with name '%s' already exists", clusterName ) );
            return;
        }

        setupStandalone();
    }


    private void setupStandalone() {

        try {
            Environment env = manager.getEnvironmentManager()
                                     .buildEnvironmentAndReturn( manager.getDefaultEnvironmentBlueprint( config ) );

            ClusterSetupStrategy clusterSetupStrategy = manager.getClusterSetupStrategy( env, config, po );
            clusterSetupStrategy.setup();

            po.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( EnvironmentBuildException | ClusterSetupException e ) {
            po.addLogFailed(
                    String.format( "Failed to setup Elasticsearch cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }

}
