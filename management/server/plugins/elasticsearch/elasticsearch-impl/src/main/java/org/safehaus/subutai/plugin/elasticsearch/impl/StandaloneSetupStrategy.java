package org.safehaus.subutai.plugin.elasticsearch.impl;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.Node;
import org.safehaus.subutai.plugin.elasticsearch.api.Config;
import org.safehaus.subutai.shared.operation.ProductOperation;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ClusterConfigurationException;
import org.safehaus.subutai.shared.protocol.ClusterSetupException;
import org.safehaus.subutai.shared.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.shared.protocol.settings.Common;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class StandaloneSetupStrategy implements ClusterSetupStrategy {

    private final Config config;
    private final ElasticsearchImpl elasticsearchManager;
    private final ProductOperation po;
    private final Environment environment;


    public StandaloneSetupStrategy( final Environment environment, final Config config,
                                    ProductOperation po, ElasticsearchImpl elasticsearchManager ) {
        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( config, "Cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( elasticsearchManager, "elasticsearchManager manager is null" );

        this.config = config;
        this.po = po;
        this.elasticsearchManager = elasticsearchManager;
        this.environment = environment;
    }


    @Override
    public Config setup() throws ClusterSetupException {
        if ( Strings.isNullOrEmpty( config.getClusterName() ) ||
                Strings.isNullOrEmpty( config.getTemplateName() ) ||
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

        Set<Agent> agents = new HashSet<>();
        for ( Node node : environment.getNodes() ) {
            if ( node.getTemplate().getProducts()
                     .contains( Common.PACKAGE_PREFIX + config.PRODUCT_NAME ) ) {
                agents.add( node.getAgent() );
            }
        }

        config.getNodes().addAll( agents );

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
                            .saveInfo( config.PRODUCT_KEY, config.getClusterName(), config );
            po.addLog( "Cluster information saved to database" );
        }
        catch ( DBException e ) {
            throw new ClusterSetupException(
                    String.format( "Failed to save cluster information to database, %s", e.getMessage() ) );
        }


        return config;
    }
}
