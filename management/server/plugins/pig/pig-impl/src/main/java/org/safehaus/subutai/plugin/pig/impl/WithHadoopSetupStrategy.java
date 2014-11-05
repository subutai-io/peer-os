//package org.safehaus.subutai.plugin.pig.impl;
//
//
//import java.util.HashSet;
//import java.util.Set;
//import java.util.UUID;
//
//import org.safehaus.subutai.common.exception.ClusterSetupException;
//import org.safehaus.subutai.common.protocol.Agent;
//import org.safehaus.subutai.common.protocol.ConfigBase;
//import org.safehaus.subutai.common.tracker.TrackerOperation;
//import org.safehaus.subutai.core.environment.api.helper.Environment;
//import org.safehaus.subutai.core.environment.api.helper.EnvironmentContainer;
//import org.safehaus.subutai.core.peer.api.ContainerHost;
//import org.safehaus.subutai.plugin.pig.api.PigConfig;
//
//
//class WithHadoopSetupStrategy extends PigSetupStrategy
//{
//
//    Environment environment;
//
//
//    public WithHadoopSetupStrategy( PigImpl manager, PigConfig config, TrackerOperation po )
//    {
//        super( manager, config, po );
//    }
//
//
//    public void setEnvironment( Environment env )
//    {
//        this.environment = env;
//    }
//
//
//    @Override
//    public ConfigBase setup() throws ClusterSetupException
//    {
//
//        checkConfig();
//
//        if ( environment == null )
//        {
//            throw new ClusterSetupException( "Environment not specified" );
//        }
//
//        if ( environment.getContainers() == null || environment.getContainers().isEmpty() )
//        {
//            throw new ClusterSetupException( "Environment has no nodes" );
//        }
//
//        Set<UUID> pigNodes = new HashSet<>(), allNodes = new HashSet<>();
//        for ( ContainerHost n : environment.getContainers() )
//        {
//            allNodes.add( n.getAgent().getUuid() );
//            if ( n.getTemplate().getProducts().contains( Commands.PACKAGE_NAME ) )
//            {
//                pigNodes.add( n.getAgent().getUuid() );
//            }
//        }
//        if ( pigNodes.isEmpty() )
//        {
//            throw new ClusterSetupException( "Environment has no nodes with Pig installed" );
//        }
//
//        config.setNodes( pigNodes );
//        config.setHadoopNodes( allNodes );
//
//        for ( Agent a : config.getNodes() )
//        {
//            if ( manager.getAgentManager().getAgentByHostname( a.getHostname() ) == null )
//            {
//                throw new ClusterSetupException( "Node is not connected: " + a.getHostname() );
//            }
//        }
//
//        trackerOperation.addLog( "Saving to db..." );
//        manager.getPluginDao().saveInfo( PigConfig.PRODUCT_KEY, config.getClusterName(), config );
//        trackerOperation.addLog( "Cluster info successfully saved" );
//
//        return config;
//    }
//}
