package org.safehaus.subutai.plugin.solr.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.Node;
import org.safehaus.subutai.plugin.solr.api.SolrClusterConfig;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.settings.Common;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Solr cluster setup strategy
 */
public class SolrSetupStrategy implements ClusterSetupStrategy {

    private SolrImpl manager;
    private ProductOperation po;
    private SolrClusterConfig config;
    private Environment environment;


    public SolrSetupStrategy( final SolrImpl manager, final ProductOperation po, final SolrClusterConfig config,
                              final Environment environment ) {
        Preconditions.checkNotNull( manager, "Solr Manager is null" );
        Preconditions.checkNotNull( po, "Product operation is null" );
        Preconditions.checkNotNull( config, "Solr config is null" );
        Preconditions.checkNotNull( environment, "Environment is null" );
        this.manager = manager;
        this.po = po;
        this.config = config;
        this.environment = environment;
    }


    public static PlacementStrategy getPlacementStrategy() {
        return PlacementStrategy.ROUND_ROBIN;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException {

        if ( Strings.isNullOrEmpty( config.getClusterName() ) || config.getNumberOfNodes() <= 0 ) {
            throw new ClusterSetupException( "Malformed configuration" );
        }

        if ( manager.getCluster( config.getClusterName() ) != null ) {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists", config.getClusterName() ) );
        }

        if ( environment.getNodes().isEmpty() ) {
            throw new ClusterSetupException( "Environment has no nodes" );
        }

        if ( environment.getNodes().size() < config.getNumberOfNodes() ) {
            throw new ClusterSetupException(
                    String.format( "Environment has %d nodes but %d nodes are required", environment.getNodes().size(),
                            config.getNumberOfNodes() ) );
        }

        Set<Node> solrNodes = new HashSet<>();
        for ( Node node : environment.getNodes() ) {
            if ( node.getTemplate().getProducts().contains( Common.PACKAGE_PREFIX + SolrClusterConfig.PRODUCT_NAME ) ) {
                solrNodes.add( node );
            }
        }

        if ( solrNodes.size() < config.getNumberOfNodes() ) {
            throw new ClusterSetupException(
                    String.format( "Number of nodes with Solr installed is %d, but %d is required", solrNodes.size(),
                            config.getNumberOfNodes() ) );
        }

        Set<Agent> solrAgents = new HashSet<>();
        Iterator<Node> it = solrNodes.iterator();
        for ( int i = 0; i < config.getNumberOfNodes(); i++ ) {
            solrAgents.add( it.next().getAgent() );
        }

        config.setNodes( solrAgents );

        po.addLog( "Saving cluster information to database..." );

        try {
            manager.getPluginDAO().saveInfo( SolrClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
            po.addLog( "Cluster information saved to database" );
        }
        catch ( DBException e ) {
            throw new ClusterSetupException(
                    String.format( "Error saving cluster information to database, %s", e.getMessage() ) );
        }

        return config;
    }
}
