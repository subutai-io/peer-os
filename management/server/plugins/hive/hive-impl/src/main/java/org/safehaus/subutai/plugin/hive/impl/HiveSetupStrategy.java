package org.safehaus.subutai.plugin.hive.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;
import org.safehaus.subutai.plugin.hive.api.SetupType;

import com.google.common.base.Strings;


abstract class HiveSetupStrategy implements ClusterSetupStrategy
{
    public final HiveImpl hiveManager;
    public final HiveConfig config;
    public final TrackerOperation trackerOperation;
    public final Environment environment;


    public HiveSetupStrategy( final Environment environment, HiveImpl manager,
                              HiveConfig config, TrackerOperation trackerOperation )
    {
        this.hiveManager = manager;
        this.config = config;
        this.trackerOperation = trackerOperation;
        this.environment = environment;
    }

    @Override
    public ConfigBase setup() throws ClusterSetupException
    {
        if ( Strings.isNullOrEmpty( config.getClusterName() ) ||
                Strings.isNullOrEmpty( HiveConfig.getTemplateName() ) ||
                config.getNumberOfNodes() <= 0 )
        {
            throw new ClusterSetupException( "Malformed configuration" );
        }

        if ( hiveManager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    String.format( "Cluster with name '%s' already exists", config.getClusterName() ) );
        }

        if ( environment.getContainers().size() < config.getNumberOfNodes() )
        {
            throw new ClusterSetupException( String.format( "Environment needs to have %d nodes but has only %d nodes",
                    config.getNumberOfNodes(), environment.getContainers().size() ) );
        }

//        Set<UUID> allNodes = new HashSet<>();
//        for ( ContainerHost environmentContainer : environment.getContainers() )
//        {
//            allNodes.add( environmentContainer.getAgent().getUuid() );
//        }
//
//        Iterator iterator = allNodes.iterator();
//        config.setServer( ( UUID ) iterator.next() );
//
//        Set<UUID> clientNodes = new HashSet<>();
//        for ( int i=0; i<config.getNumberOfNodes()-1; i++ ){
//
//            clientNodes.add( ( UUID ) iterator.next() );
//        }
//        config.setClients( clientNodes );


        SetupStrategyOverHadoop setupStrategyOverHadoop = new SetupStrategyOverHadoop( environment, hiveManager, config, trackerOperation );
        setupStrategyOverHadoop.setup();

        trackerOperation.addLog( "Saving cluster information to database..." );
        hiveManager.getPluginDAO()
                            .saveInfo( HiveConfig.PRODUCT_KEY, config.getClusterName(), config );
        trackerOperation.addLog( "Cluster information saved to database" );
        return config;
    }

    public void checkConfig() throws ClusterSetupException
    {
        String message = "Invalid configuration: ";

        if ( config.getClusterName() == null || config.getClusterName().isEmpty() )
        {
            throw new ClusterSetupException( message + "name is not specified" );
        }

        if ( hiveManager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException(
                    message + String.format( "Sqoop installation already exists: %s", config.getClusterName() ) );
        }

        if ( config.getSetupType() == SetupType.OVER_HADOOP )
        {
            if ( config.getServer() == null )
            {
                throw new ClusterSetupException( message + "Server node not specified" );
            }
            if ( config.getClients() == null || config.getClients().isEmpty() )
            {
                throw new ClusterSetupException( message + "Target nodes not specified" );
            }
        }
    }

}
