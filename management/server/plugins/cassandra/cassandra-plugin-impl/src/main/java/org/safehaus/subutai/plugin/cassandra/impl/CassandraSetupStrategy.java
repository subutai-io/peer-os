package org.safehaus.subutai.plugin.cassandra.impl;


import org.safehaus.subutai.plugin.cassandra.api.CassandraConfig;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;

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



        /*if ( Strings.isNullOrEmpty( config.getClusterName() ) ||
                        Strings.isNullOrEmpty( config.getDomainName() ) ||
                        Strings.isNullOrEmpty( config.getReplicaSetName() ) ||
                        Strings.isNullOrEmpty( config.getTemplateName() ) ||
                        !Sets.newHashSet( 1, 3 ).contains( config.getNumberOfConfigServers() ) ||
                        !Range.closed( 1, 3 ).contains( config.getNumberOfRouters() ) ||
                        !Sets.newHashSet( 3, 5, 7 ).contains( config.getNumberOfDataNodes() ) ||
                        !Range.closed( 1024, 65535 ).contains( config.getCfgSrvPort() ) ||
                        !Range.closed( 1024, 65535 ).contains( config.getRouterPort() ) ||
                        !Range.closed( 1024, 65535 ).contains( config.getDataNodePort() ) ) {
                    throw new ClusterSetupException( "Malformed cluster configuration" );
                }*/

        return config;
    }
}
