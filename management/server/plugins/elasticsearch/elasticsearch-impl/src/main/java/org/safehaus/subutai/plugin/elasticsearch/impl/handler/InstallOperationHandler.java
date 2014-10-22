package org.safehaus.subutai.plugin.elasticsearch.impl.handler;


import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentBuildException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;
import org.safehaus.subutai.plugin.elasticsearch.impl.ElasticsearchImpl;

import com.google.common.base.Strings;


public class InstallOperationHandler extends AbstractOperationHandler<ElasticsearchImpl>
{

    private final ElasticsearchClusterConfiguration elasticsearchClusterConfiguration;


    public InstallOperationHandler( ElasticsearchImpl manager,
                                    ElasticsearchClusterConfiguration elasticsearchClusterConfiguration )
    {

        super( manager, elasticsearchClusterConfiguration.getClusterName() );
        this.elasticsearchClusterConfiguration = elasticsearchClusterConfiguration;
        trackerOperation = manager.getTracker().createTrackerOperation( ElasticsearchClusterConfiguration.PRODUCT_KEY,
                String.format( "Setting up %s cluster...", elasticsearchClusterConfiguration.getClusterName() ) );
    }


    @Override
    public UUID getTrackerId()
    {
        return trackerOperation.getId();
    }


    @Override
    public void run()
    {
        if ( Strings.isNullOrEmpty( elasticsearchClusterConfiguration.getClusterName() ) )
        {
            trackerOperation.addLogFailed( "Malformed configuration" );
            return;
        }

        if ( manager.getCluster( clusterName ) != null )
        {
            trackerOperation.addLogFailed( String.format( "Cluster with name '%s' already exists", clusterName ) );
            return;
        }

        setupStandalone();
    }


    private void setupStandalone()
    {
        try
        {
            Environment env = manager.getEnvironmentManager().buildEnvironment(
                    manager.getDefaultEnvironmentBlueprint( elasticsearchClusterConfiguration ) );

            ClusterSetupStrategy clusterSetupStrategy =
                    manager.getClusterSetupStrategy( env, elasticsearchClusterConfiguration, trackerOperation );
            clusterSetupStrategy.setup();

            trackerOperation.addLogDone( String.format( "Cluster %s set up successfully", clusterName ) );
        }
        catch ( EnvironmentBuildException | ClusterSetupException e )
        {
            trackerOperation.addLogFailed(
                    String.format( "Failed to setup Elasticsearch cluster %s : %s", clusterName, e.getMessage() ) );
        }
    }
}
