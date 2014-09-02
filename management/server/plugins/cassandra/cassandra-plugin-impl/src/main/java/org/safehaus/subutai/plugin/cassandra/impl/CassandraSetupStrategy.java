package org.safehaus.subutai.plugin.cassandra.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.Node;
import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.cassandra.api.CassandraConfig;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Created by bahadyr on 8/22/14.
 */
public class CassandraSetupStrategy implements ClusterSetupStrategy {

    private Environment environment;
    private CassandraConfig config;
    private CassandraImpl cassandraManager;
    private ProductOperation productOperation;


    public CassandraSetupStrategy( final Environment environment, final CassandraConfig config,
                                   final ProductOperation po, final CassandraImpl cassandra ) {

        Preconditions.checkNotNull( environment, "Environment is null" );
        Preconditions.checkNotNull( config, "Cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( cassandra, "Mongo manager is null" );
        this.environment = environment;
        this.config = config;
        this.productOperation = po;
        this.cassandraManager = cassandra;
    }


    @Override
    public CassandraConfig setup() throws ClusterSetupException {

        if ( Strings.isNullOrEmpty( config.getClusterName() ) ||
                Strings.isNullOrEmpty( config.getCommitLogDirectory() ) ||
                Strings.isNullOrEmpty( config.getDataDirectory() ) ||
                Strings.isNullOrEmpty( config.getSavedCachesDirectory() ) ||
                Strings.isNullOrEmpty( config.getDomainName() ) ||
                Strings.isNullOrEmpty( config.getProductName() ) ||
                Strings.isNullOrEmpty( config.getTemplateName() ) ) {
            throw new ClusterSetupException( "Malformed cluster configuration" );
        }

        if ( cassandraManager.getCluster( config.getClusterName() ) != null ) {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists", config.getClusterName() ) );
        }

        Set<Agent> cassNodes = new HashSet<Agent>();
        for ( Node node : environment.getNodes() ) {
            cassNodes.add( node.getAgent() );
        }
        config.setNodes( cassNodes );

        Iterator nodesItr = cassNodes.iterator();
        Set<Agent> seedNodes = new HashSet<Agent>();
        while ( nodesItr.hasNext() ) {
            seedNodes.add( ( Agent ) nodesItr.next() );
            if ( seedNodes.size() == config.getNumberOfSeeds() ) {
                break;
            }
        }

        try {
            new ClusterConfiguration( productOperation, cassandraManager ).configureCluster( config );
        }
        catch ( ClusterConfigurationException e ) {
            throw new ClusterSetupException( e.getMessage() );
        }


        return config;
    }
}
