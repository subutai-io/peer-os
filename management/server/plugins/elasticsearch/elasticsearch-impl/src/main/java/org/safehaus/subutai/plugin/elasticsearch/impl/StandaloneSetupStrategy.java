package org.safehaus.subutai.plugin.elasticsearch.impl;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.Node;
import org.safehaus.subutai.common.exception.*;
import org.safehaus.subutai.common.protocol.*;
import org.safehaus.subutai.common.settings.*;
import org.safehaus.subutai.common.tracker.*;
import org.safehaus.subutai.plugin.elasticsearch.api.ElasticsearchClusterConfiguration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class StandaloneSetupStrategy implements ClusterSetupStrategy {

    private final ElasticsearchClusterConfiguration elasticsearchClusterConfiguration;
    private final ElasticsearchImpl elasticsearchManager;
    private final ProductOperation po;
    private final Environment environment;


    public StandaloneSetupStrategy( final Environment environment, final ElasticsearchClusterConfiguration elasticsearchClusterConfiguration,
                                    ProductOperation po, ElasticsearchImpl elasticsearchManager ) {
        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( elasticsearchClusterConfiguration, "Cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( elasticsearchManager, "elasticsearchManager manager is null" );

        this.elasticsearchClusterConfiguration = elasticsearchClusterConfiguration;
        this.po = po;
        this.elasticsearchManager = elasticsearchManager;
        this.environment = environment;
    }


    @Override
    public ElasticsearchClusterConfiguration setup() throws ClusterSetupException {
        if ( Strings.isNullOrEmpty( elasticsearchClusterConfiguration.getClusterName() ) ||
                Strings.isNullOrEmpty( ElasticsearchClusterConfiguration.getTemplateName() ) ||
                elasticsearchClusterConfiguration.getNumberOfNodes() <= 0 ) {
            throw new ClusterSetupException( "Malformed configuration" );
        }

        if ( elasticsearchManager.getCluster( elasticsearchClusterConfiguration.getClusterName() ) != null ) {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists", elasticsearchClusterConfiguration.getClusterName() ) );
        }

        if ( environment.getNodes().size() < elasticsearchClusterConfiguration.getNumberOfNodes() ) {
            throw new ClusterSetupException( String.format( "Environment needs to have %d nodes but has only %d nodes",
                    elasticsearchClusterConfiguration.getNumberOfNodes(), environment.getNodes().size() ) );
        }

        Set<Agent> agents = new HashSet<>();
        for ( Node node : environment.getNodes() ) {
            if ( node.getTemplate().getProducts()
                     .contains( Common.PACKAGE_PREFIX + ElasticsearchClusterConfiguration.PRODUCT_NAME ) ) {
                agents.add( node.getAgent() );
            }
        }

        elasticsearchClusterConfiguration.getNodes().addAll( agents );

        //check if node agent is connected
        for ( Agent node : elasticsearchClusterConfiguration.getNodes() ) {
            if ( elasticsearchManager.getAgentManager().getAgentByHostname( node.getHostname() ) == null ) {
                throw new ClusterSetupException( String.format( "Node %s is not connected", node.getHostname() ) );
            }
        }

        try {
            new ClusterConfiguration( elasticsearchManager, po ).configureCluster( elasticsearchClusterConfiguration );
        }
        catch ( ClusterConfigurationException ex ) {
            throw new ClusterSetupException( ex.getMessage() );
        }

        po.addLog( "Saving cluster information to database..." );

        try {
            elasticsearchManager.getPluginDAO()
                            .saveInfo( ElasticsearchClusterConfiguration.PRODUCT_KEY, elasticsearchClusterConfiguration.getClusterName(), elasticsearchClusterConfiguration );
            po.addLog( "Cluster information saved to database" );
        }
        catch ( DBException e ) {
            throw new ClusterSetupException(
                    String.format( "Failed to save cluster information to database, %s", e.getMessage() ) );
        }


        return elasticsearchClusterConfiguration;
    }
}
