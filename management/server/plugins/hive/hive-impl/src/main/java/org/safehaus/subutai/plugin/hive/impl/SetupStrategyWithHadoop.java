//package org.safehaus.subutai.plugin.hive.impl;
//
//
//import java.util.HashSet;
//import java.util.Set;
//import java.util.UUID;
//
//import org.safehaus.subutai.common.exception.ClusterSetupException;
//import org.safehaus.subutai.common.protocol.ConfigBase;
//import org.safehaus.subutai.common.tracker.TrackerOperation;
//import org.safehaus.subutai.core.environment.api.helper.Environment;
//import org.safehaus.subutai.core.peer.api.ContainerHost;
//import org.safehaus.subutai.core.peer.api.PeerException;
//import org.safehaus.subutai.plugin.hive.api.HiveConfig;
//
//
//class SetupStrategyWithHadoop extends HiveSetupStrategy
//{
//
//    Environment environment;
//
//
//    public SetupStrategyWithHadoop( Environment environment, HiveImpl manager, HiveConfig config,
// TrackerOperation po )
//    {
//        super( environment, manager, config, po );
//    }
//
//
//    public void setEnvironment( Environment environment )
//    {
//        this.environment = environment;
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
//        if ( environment.getContainerHosts() == null || environment.getContainerHosts().isEmpty() )
//        {
//            throw new ClusterSetupException( "Environment has no nodes" );
//        }
//
//        Set<UUID> clients = new HashSet<>(), allNodes = new HashSet<>();
//        for ( ContainerHost n : environment.getContainerHosts() )
//        {
//            allNodes.add( n.getAgent().getUuid() );
//            // if Derby installed on node and server node is not yet set
//            try
//            {
//                if ( n.getTemplate().getProducts().contains( Product.DERBY.getPackageName() ) )
//                {
//                    if ( config.getServer() == null )
//                    {
//                        config.setServer( n.getAgent().getUuid() );
//                        continue;
//                    }
//                }
//            }
//            catch ( PeerException e )
//            {
//                e.printStackTrace();
//            }
//            try
//            {
//                if ( n.getTemplate().getProducts().contains( Product.HIVE.getPackageName() ) )
//                {
//                    clients.add( n.getAgent().getUuid() );
//                }
//            }
//            catch ( PeerException e )
//            {
//                e.printStackTrace();
//            }
//        }
//        if ( config.getServer() == null )
//        {
//            throw new ClusterSetupException( "Environment has no Hive server node" );
//        }
//        if ( clients.isEmpty() )
//        {
//            throw new ClusterSetupException( "Environment has no nodes with Hive installed" );
//        }
//
//        config.setClients( clients );
//        config.setHadoopNodes( allNodes );
//
////        String serverHostname = config.getServer().getHostname();
////        if ( hiveManager.agentManager.getAgentByHostname( serverHostname ) == null )
////        {
////            throw new ClusterSetupException( "Server node is not connected" );
////        }
////        for ( Agent a : config.getClients() )
////        {
////            if ( hiveManager.agentManager.getAgentByHostname( a.getHostname() ) == null )
////            {
////                throw new ClusterSetupException( "Node is not connected: " + a.getHostname() );
////            }
////        }
//
////        configureServer();
////        configureClients();
//
//        trackerOperation.addLog( "Saving to db..." );
//        hiveManager.getPluginDAO().saveInfo( HiveConfig.PRODUCT_KEY, config.getClusterName(), config );
//        trackerOperation.addLog( "Cluster info successfully saved" );
//
//        return config;
//    }
//}
