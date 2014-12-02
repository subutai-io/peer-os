package org.safehaus.subutai.plugin.elasticsearch.impl;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class ESSetupStrategy implements ClusterSetupStrategy
{

    private final ElasticsearchClusterConfiguration config;
    private final ElasticsearchImpl elasticsearchManager;
    private final TrackerOperation po;
    private final Environment environment;


    public ESSetupStrategy( final Environment environment,
                            final ElasticsearchClusterConfiguration elasticsearchClusterConfiguration,
                            TrackerOperation po, ElasticsearchImpl elasticsearchManager )
    {
        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( elasticsearchClusterConfiguration, "Cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( elasticsearchManager, "elasticsearchManager manager is null" );

        this.config = elasticsearchClusterConfiguration;
        this.po = po;
        this.elasticsearchManager = elasticsearchManager;
        this.environment = environment;
    }


    @Override
    public ElasticsearchClusterConfiguration setup() throws ClusterSetupException
    {
        if ( Strings.isNullOrEmpty( config.getClusterName() ) ||
                Strings.isNullOrEmpty( config.getTemplateName() ) ||
                config.getNumberOfNodes() <= 0 )
        {
            throw new ClusterSetupException( "Malformed configuration" );
        }

        if ( elasticsearchManager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists", config.getClusterName() ) );
        }

        if ( environment.getContainerHosts().size() < config.getNumberOfNodes() )
        {
            throw new ClusterSetupException( String.format( "Environment needs to have %d nodes but has only %d nodes",
                    config.getNumberOfNodes(), environment.getContainerHosts().size() ) );
        }

        Set<UUID> esNodes = new HashSet<>();
        for ( ContainerHost containerHost : environment.getContainerHosts() )
        {
            esNodes.add( containerHost.getId() );
        }
        config.setNodes( esNodes );

        try
        {
            new ClusterConfiguration( elasticsearchManager, po ).configureCluster( config , environment );
        }
        catch ( ClusterConfigurationException ex )
        {
            throw new ClusterSetupException( ex.getMessage() );
        }

        po.addLog( "Saving cluster information to database..." );

        elasticsearchManager.getPluginDAO()
                            .saveInfo( ElasticsearchClusterConfiguration.PRODUCT_KEY, config.getClusterName(), config );
        po.addLog( "Cluster information saved to database" );

        return config;
    }
}
