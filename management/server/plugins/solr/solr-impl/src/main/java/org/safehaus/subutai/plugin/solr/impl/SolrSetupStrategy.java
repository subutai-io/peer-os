package org.safehaus.subutai.plugin.solr.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Solr cluster setup strategy
 */
public class SolrSetupStrategy implements ClusterSetupStrategy
{

    private SolrImpl manager;
    private TrackerOperation po;
    private SolrClusterConfig config;
    private Environment environment;


    public SolrSetupStrategy( final SolrImpl manager, final TrackerOperation po, final SolrClusterConfig config,
                              final Environment environment )
    {
        Preconditions.checkNotNull( manager, "Solr Manager is null" );
        Preconditions.checkNotNull( po, "Product operation is null" );
        Preconditions.checkNotNull( config, "Solr config is null" );
        Preconditions.checkNotNull( environment, "Environment is null" );
        this.manager = manager;
        this.po = po;
        this.config = config;
        this.environment = environment;
    }



    @Override
    public ConfigBase setup() throws ClusterSetupException
    {

        if ( Strings.isNullOrEmpty( config.getClusterName() ) || config.getNumberOfNodes() <= 0 )
        {
            throw new ClusterSetupException( "Malformed configuration" );
        }

        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists", config.getClusterName() ) );
        }

        if ( environment.getContainers().isEmpty() )
        {
            throw new ClusterSetupException( "Environment has no nodes" );
        }

        if ( environment.getContainers().size() < config.getNumberOfNodes() )
        {
            throw new ClusterSetupException( String.format( "Environment has %d nodes but %d nodes are required",
                    environment.getContainers().size(), config.getNumberOfNodes() ) );
        }

        Set<UUID> solrNodes = new HashSet<>();
        for ( ContainerHost host : environment.getContainers() )
        {
            solrNodes.add( host.getAgent().getUuid() );
        }

        config.setNodes( solrNodes );

        po.addLog( "Saving cluster information to database..." );

        config.setEnvironmentId( environment.getId() );

        manager.getPluginDAO().saveInfo( SolrClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
        po.addLog( "Cluster information saved to database" );

        return config;
    }
}
