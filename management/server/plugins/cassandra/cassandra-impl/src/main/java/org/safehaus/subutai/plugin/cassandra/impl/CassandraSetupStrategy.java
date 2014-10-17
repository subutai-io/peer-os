package org.safehaus.subutai.plugin.cassandra.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.safehaus.subutai.common.exception.ClusterConfigurationException;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentContainer;
import org.safehaus.subutai.plugin.cassandra.api.CassandraClusterConfig;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class CassandraSetupStrategy implements ClusterSetupStrategy
{

    private Environment environment;
    private CassandraClusterConfig config;
    private CassandraImpl cassandraManager;
    private ProductOperation productOperation;


    public CassandraSetupStrategy( final Environment environment, final CassandraClusterConfig config,
                                   final ProductOperation po, final CassandraImpl cassandra )
    {

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
    public CassandraClusterConfig setup() throws ClusterSetupException
    {

        if ( Strings.isNullOrEmpty( config.getClusterName() ) ||
                Strings.isNullOrEmpty( config.getCommitLogDirectory() ) ||
                Strings.isNullOrEmpty( config.getDataDirectory() ) ||
                Strings.isNullOrEmpty( config.getSavedCachesDirectory() ) ||
                Strings.isNullOrEmpty( config.getDomainName() ) ||
                Strings.isNullOrEmpty( config.getProductName() ) ||
                Strings.isNullOrEmpty( config.getTemplateName() ) )
        {
            throw new ClusterSetupException( "Malformed cluster configuration" );
        }

        if ( cassandraManager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists", config.getClusterName() ) );
        }

        Set<Agent> cassNodes = new HashSet<Agent>();
        for ( EnvironmentContainer environmentContainer : environment.getEnvironmentContainerNodes() )
        {
            cassNodes.add( environmentContainer.getAgent() );
        }
        config.setNodes( cassNodes );

        Iterator nodesItr = cassNodes.iterator();
        Set<Agent> seedNodes = new HashSet<Agent>();
        while ( nodesItr.hasNext() )
        {
            seedNodes.add( ( Agent ) nodesItr.next() );
            if ( seedNodes.size() == config.getNumberOfSeeds() )
            {
                break;
            }
        }
        config.setSeedNodes( seedNodes );


        try
        {
            new ClusterConfiguration( productOperation, cassandraManager ).configureCluster( config );
        }
        catch ( ClusterConfigurationException e )
        {
            throw new ClusterSetupException( e.getMessage() );
        }
        return config;
    }
}
