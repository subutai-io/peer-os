package org.safehaus.subutai.plugin.elasticsearch.impl.handler;


import com.google.common.base.Strings;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;

import java.util.UUID;


public class InstallOperationHandler extends AbstractOperationHandler<ElasticsearchImpl > {

    private final ElasticsearchClusterConfiguration elasticsearchClusterConfiguration;

    public InstallOperationHandler( ElasticsearchImpl manager, ElasticsearchClusterConfiguration elasticsearchClusterConfiguration ) {

        super( manager, elasticsearchClusterConfiguration.getClusterName() );
        this.elasticsearchClusterConfiguration = elasticsearchClusterConfiguration;
        productOperation = manager.getTracker().createProductOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", elasticsearchClusterConfiguration.getClusterName() ) );
    }


    @Override
    public UUID getTrackerId() {
        return productOperation.getId();
    }


    @Override
    public void run() {
        if ( Strings.isNullOrEmpty( elasticsearchClusterConfiguration.getClusterName() ) ) {
            productOperation.addLogFailed( "Malformed configuration" );
            return;
        }

        if ( manager.getCluster( clusterName ) != null ) {
            productOperation.addLogFailed( String.format( "Cluster with name '%s' already exists", clusterName ) );
            return;
        }

        setupStandalone();
    }


    private void setupStandalone() {

        try {
            Environment env = manager.getEnvironmentManager()
                                     .buildEnvironmentAndReturn( manager.getDefaultEnvironmentBlueprint( elasticsearchClusterConfiguration ) );

            ClusterSetupStrategy clusterSetupStrategy = manager.getClusterSetupStrategy( env, elasticsearchClusterConfiguration, productOperation );
            clusterSetupStrategy.setup();

            productOperation.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( EnvironmentBuildException | ClusterSetupException e ) {
            productOperation.addLogFailed(
                    String.format( "Failed to setup Elasticsearch cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }

}
