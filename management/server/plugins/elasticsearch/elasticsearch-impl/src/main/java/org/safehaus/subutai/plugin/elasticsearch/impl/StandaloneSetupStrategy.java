package org.safehaus.subutai.plugin.elasticsearch.impl;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.Node;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class StandaloneSetupStrategy implements ClusterSetupStrategy {

    private final ElasticsearchClusterConfiguration config;
    private final ElasticsearchImpl elasticsearchManager;
    private final ProductOperation po;
    private final Environment environment;


    public StandaloneSetupStrategy( final Environment environment, final ElasticsearchClusterConfiguration elasticsearchClusterConfiguration,
                                    ProductOperation po, ElasticsearchImpl elasticsearchManager ) {
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
    public ElasticsearchClusterConfiguration setup() throws ClusterSetupException {
        if ( Strings.isNullOrEmpty( config.getClusterName() ) ||
                Strings.isNullOrEmpty( ElasticsearchClusterConfiguration.getTemplateName() ) ||
                config.getNumberOfNodes() <= 0 ) {
            throw new ClusterSetupException( "Malformed configuration" );
        }

        if ( elasticsearchManager.getCluster( config.getClusterName() ) != null ) {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists", config.getClusterName() ) );
        }

        if ( environment.getNodes().size() < config.getNumberOfNodes() ) {
            throw new ClusterSetupException( String.format( "Environment needs to have %d nodes but has only %d nodes",
                    config.getNumberOfNodes(), environment.getNodes().size() ) );
        }

        Set<Agent> elasticsearhcNodes = new HashSet<Agent>();
        for ( Node node : environment.getNodes() ) {
            elasticsearhcNodes.add( node.getAgent() );
        }
        config.setNodes( elasticsearhcNodes );

        Iterator nodesItr = elasticsearhcNodes.iterator();
        Set<Agent> masterNodes = new HashSet<Agent>();
        while ( nodesItr.hasNext() ) {
            masterNodes.add( ( Agent ) nodesItr.next() );
            if ( masterNodes.size() == config.getNumberOfMasterNodes() ) {
                break;
            }
        }
        config.setMasterNodes( masterNodes );


        Set<Agent> dataNodes = new HashSet<Agent>();
        while ( nodesItr.hasNext() ) {
            Agent temp = ( Agent ) nodesItr.next();
            if ( ! masterNodes.contains( temp  ) ){
                dataNodes.add( temp );
            }
        }
        config.setDataNodes( dataNodes );


        //check if node agent is connected
        for ( Agent node : config.getNodes() ) {
            if ( elasticsearchManager.getAgentManager().getAgentByHostname( node.getHostname() ) == null ) {
                throw new ClusterSetupException( String.format( "Node %s is not connected", node.getHostname() ) );
            }
        }

        try {
            new ClusterConfiguration( elasticsearchManager, po ).configureCluster( config );
        }
        catch ( ClusterConfigurationException ex ) {
            throw new ClusterSetupException( ex.getMessage() );
        }

        po.addLog( "Saving cluster information to database..." );

        try {
            elasticsearchManager.getPluginDAO()
                            .saveInfo( ElasticsearchClusterConfiguration.PRODUCT_KEY.toLowerCase(), config.getClusterName(), config );
            po.addLog( "Cluster information saved to database" );
        }
        catch ( DBException e ) {
            throw new ClusterSetupException(
                    String.format( "Failed to save cluster information to database, %s", e.getMessage() ) );
        }


        return config;
    }
}
