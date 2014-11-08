package org.safehaus.subutai.plugin.spark.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;

import com.google.common.base.Preconditions;


public class SetupStrategyWithHadoop implements ClusterSetupStrategy
{

    final TrackerOperation po;
    final SparkImpl manager;
    final SparkClusterConfig config;
    private Environment environment;


    public SetupStrategyWithHadoop( TrackerOperation po, SparkImpl manager, SparkClusterConfig config,
                                    Environment environment )
    {
        Preconditions.checkNotNull( config, "Cluster config is null" );
        Preconditions.checkNotNull( po, "Product operation tracker is null" );
        Preconditions.checkNotNull( manager, "Manager is null" );
        Preconditions.checkNotNull( environment, "Environment is null" );

        this.po = po;
        this.manager = manager;
        this.config = config;
        this.environment = environment;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {
        try
        {

            if ( manager.getCluster( config.getClusterName() ) != null )
            {
                throw new ClusterSetupException( "Cluster already exists: " + config.getClusterName() );
            }

            if ( environment == null )
            {
                throw new ClusterSetupException( "Environment not specified" );
            }

            if ( CollectionUtil.isCollectionEmpty( environment.getContainers() ) )
            {
                throw new ClusterSetupException( "Environment has no containers" );
            }

            config.setMasterNodeId( null );
            config.getSlaveIds().clear();
            config.getHadoopNodeIds().clear();
            config.setEnvironmentId( environment.getId() );

            for ( ContainerHost container : environment.getContainers() )
            {
                if ( !container.isConnected() )
                {
                    throw new ClusterSetupException(
                            String.format( "Container %s is not connected", container.getHostname() ) );
                }

                config.getHadoopNodeIds().add( container.getId() );
                if ( container.getTemplate().getProducts().contains( Commands.PACKAGE_NAME ) )
                {
                    if ( config.getMasterNodeId() == null )
                    {
                        config.setMasterNodeId( container.getId() );
                    }
                    else
                    {
                        config.getSlaveIds().add( container.getId() );
                    }
                }
            }
            if ( config.getMasterNodeId() == null )
            {
                throw new ClusterSetupException( "Environment has no master node" );
            }
            if ( config.getSlaveIds().isEmpty() )
            {
                throw new ClusterSetupException( "Environment has no slave nodes" );
            }


            po.addLog( "Saving cluster info..." );
            manager.getPluginDAO().saveInfo( SparkClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
            po.addLog( "Cluster info saved to DB" );

            SetupHelper helper = new SetupHelper( manager, config, environment, po );
            helper.configureMasterIP();
            helper.registerSlaves();
            helper.startCluster();

            return config;
        }
        catch ( PeerException e )
        {
            throw new ClusterSetupException( e );
        }
    }
}
