package org.safehaus.subutai.plugin.storm.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.protocol.PlacementStrategy;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.EnvironmentManager;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.plugin.common.api.NodeType;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
import org.safehaus.subutai.plugin.zookeeper.api.CommandType;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;

import com.google.common.collect.Sets;


public class StormSetupStrategyDefault implements ClusterSetupStrategy
{

    private final StormImpl manager;
    private final StormClusterConfiguration config;
    private final Environment environment;
    private final TrackerOperation po;
    private final EnvironmentManager environmentManager;

    public StormSetupStrategyDefault( StormImpl manager, StormClusterConfiguration config, Environment environment,
                                      TrackerOperation po, EnvironmentManager environmentManager )
    {
        this.manager = manager;
        this.config = config;
        this.environment = environment;
        this.po = po;
        this.environmentManager = environmentManager;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {

        if ( environment == null )
        {
            throw new ClusterSetupException( "Environment not specified" );
        }

        if ( environment.getContainers() == null || environment.getContainers().isEmpty() )
        {
            throw new ClusterSetupException( "Environment has no nodes" );
        }

        // check installed packages
        for ( ContainerHost n : environment.getContainers() )
        {
            try
            {
                if ( !n.getTemplate().getProducts().contains( Commands.PACKAGE_NAME ) )
                {
                    throw new ClusterSetupException(
                            String.format( "Node %s does not have Storm installed", n.getAgent().getHostname() ) );
                }
            }
            catch ( PeerException e )
            {
                e.printStackTrace();
            }
        }

        if ( manager.getCluster( config.getClusterName() ) != null )
        {
            throw new ClusterSetupException( String.format( "Cluster '%s' already exists", config.getClusterName() ) );
        }

        if ( config.isExternalZookeeper() )
        {
            if ( config.getNimbus() == null )
            {
                throw new ClusterSetupException( "Nimbus node not specified" );
            }

            String n = config.getZookeeperClusterName();
            ZookeeperClusterConfig zk = manager.getZookeeperManager().getCluster( n );
            if ( zk == null )
            {
                throw new ClusterSetupException( "Zookeeper cluster not found: " + config.getZookeeperClusterName() );
            }

            if ( !zk.getNodes().contains( config.getNimbus() ) )
            {
                throw new ClusterSetupException(
                        "Specified nimbus node is not part of Zookeeper cluster " + config.getZookeeperClusterName() );
            }
        }
        else
        // find out nimbus node in environment
        {
            for ( ContainerHost n : environment.getContainers() )
            {
                if ( n.getNodeGroupName().equals( StormService.NIMBUS.toString() ) )
                {
                    config.setNimbus( n.getId() );
                }
            }
        }

        // collect worker nodes in environment
        for ( ContainerHost n : environment.getContainers() )
        {
            if ( n.getNodeGroupName().equals( StormService.SUPERVISOR.toString() ) )
            {
                config.getSupervisors().add( n.getId() );
            }
        }
        if ( config.getNimbus() == null )
        {
            throw new ClusterSetupException( "Environment has no Nimbus node" );
        }
        if ( config.getSupervisorsCount() != config.getSupervisors().size() )
        {
            throw new ClusterSetupException(
                    String.format( "Environment has %d nodes instead of %d", config.getSupervisors().size(),
                            config.getSupervisorsCount() ) );
        }

        ContainerHost containerHost = environment.getContainerHostByUUID( config.getNimbus() );

        //TODO enable these checks when isConnected method working OK
//        if ( !containerHost.isConnected() )
//        {
//            throw new ClusterSetupException( "Nimbus node is not connected" );
//        }
//        for ( UUID supervisorUuids : config.getSupervisors() )
//        {
//            if ( ! environment
//                    .getContainerHostByUUID( supervisorUuids ).isConnected() )
//            {
//                throw new ClusterSetupException( "Not all worker nodes are connected" );
//            }
//        }

        configure();

        config.setEnvironmentId( environment.getId() );
        manager.getPluginDAO().saveInfo( StormClusterConfiguration.PRODUCT_NAME, config.getClusterName(), config );
        po.addLog( "Cluster info successfully saved" );

        return config;
    }


    public void configure() throws ClusterSetupException
    {
        String zk_servers = makeZookeeperServersList( config );
        if ( zk_servers == null )
        {
            throw new ClusterSetupException( "No Zookeeper instances" );
        }

        Map<String, String> paramValues = new LinkedHashMap<>();
        paramValues.put( "storm.zookeeper.servers", zk_servers );
        paramValues.put( "storm.local.dir", "/var/lib/storm" );
        ContainerHost nimbusHost;
        if ( config.isExternalZookeeper() ) {
            ZookeeperClusterConfig zookeeperClusterConfig =
                    manager.getZookeeperManager().getCluster( config.getZookeeperClusterName() );
            Environment zookeeperEnvironment =
                    environmentManager.getEnvironmentByUUID( zookeeperClusterConfig.getEnvironmentId() );
            nimbusHost = zookeeperEnvironment.getContainerHostByUUID( config.getNimbus() );
        }
        else {
            nimbusHost = environment.getContainerHostByUUID( config.getNimbus() );
        }
        paramValues.put( "nimbus.host", nimbusHost.getAgent().getListIP().get( 0 ) );

        Set<ContainerHost> supervisorNodes = environment.getHostsByIds( config.getSupervisors() );

        Set<ContainerHost> allNodes = new HashSet<>(  );
        allNodes.add( nimbusHost );
        allNodes.addAll( supervisorNodes );

        ContainerHost stormNode;
        Iterator<ContainerHost> iterator = allNodes.iterator();

        while( iterator.hasNext() )
        {
            stormNode = iterator.next();
            int operation_count = 0;

            for ( Map.Entry<String, String> entry : paramValues.entrySet() )
            {
                String s = Commands.configure( "add", "storm.xml", entry.getKey(), entry.getValue() );


                // Install zookeeper on nimbus node if embedded zookeeper is selected
                if ( ! config.isExternalZookeeper() && config.getNimbus().equals( stormNode.getId() )
                        && operation_count == 0 )
                {
                    String installZookeeperCommand = manager.getZookeeperManager().getCommand( CommandType.INSTALL );
                    CommandResult commandResult = null;
                    try
                    {
                        commandResult = stormNode.execute( new RequestBuilder( installZookeeperCommand ).withTimeout( 1800 ) );
                    }
                    catch ( CommandException e )
                    {
                        e.printStackTrace();
                    }
                    po.addLog( String.format( "Zookeeper %s installed on Storm nimbus node %s",
                            commandResult.hasSucceeded() ? "" : "not", stormNode.getHostname() ) );
                }
                // Install storm on zookeeper node if external zookeeper is selected
                else if ( config.isExternalZookeeper() && config.getNimbus().equals( stormNode.getId() )
                        && operation_count == 0 )
                {
                    String installStormCommand = Commands.make( org.safehaus.subutai.plugin.storm.impl.CommandType.INSTALL );
                    CommandResult commandResult = null;
                    try
                    {
                        commandResult = stormNode.execute( new RequestBuilder( installStormCommand ).withTimeout( 1800 ) );
                    }
                    catch ( CommandException e )
                    {
                        e.printStackTrace();
                    }
                    po.addLog( String.format( "Storm %s installed on zookeeper node %s",
                            commandResult.hasSucceeded() ? "" : "not", stormNode.getHostname() ) );

                }
                try
                {
                    CommandResult commandResult = stormNode.execute( new RequestBuilder( s ).withTimeout( 60 ) );
                    po.addLog( String.format( "Storm %s%s configured for entry %s on %s", stormNode.getNodeGroupName(),
                            commandResult.hasSucceeded() ? "" : " not", entry, stormNode.getHostname() ) );
                }
                catch ( CommandException exception )
                {
                    po.addLogFailed("Failed to configure " + stormNode + ": " + exception );
                    exception.printStackTrace();
                }
                operation_count++;
            }
        }
    }


    private String makeZookeeperServersList( StormClusterConfiguration config )
    {
        if ( config.isExternalZookeeper() )
        {
            String zk_name = config.getZookeeperClusterName();
            ZookeeperClusterConfig zk_config;
            zk_config = manager.getZookeeperManager().getCluster( zk_name );
            if ( zk_config != null )
            {
                StringBuilder sb = new StringBuilder();
                Environment zookeeperEnvironment = environmentManager.getEnvironmentByUUID( zk_config.getEnvironmentId() );
                Set<ContainerHost> zookeeperNodes = zookeeperEnvironment.getHostsByIds( zk_config.getNodes() );
                for ( ContainerHost containerHost : zookeeperNodes )
                {
                    if ( sb.length() > 0 )
                    {
                        sb.append( "," );
                    }
                    sb.append( containerHost.getAgent().getListIP().get( 0 ) );
                }
                return sb.toString();
            }
        }
        else if ( config.getNimbus() != null )
        {
            ContainerHost nimbusHost = environment.getContainerHostByUUID( config.getNimbus() );
            return nimbusHost.getAgent().getListIP().get( 0 );
        }

        return null;
    }


    public static PlacementStrategy getNodePlacementStrategyByNodeType( NodeType nodeType )
    {
        switch ( nodeType )
        {
            case STORM_NIMBUS:
                return new PlacementStrategy( "BEST_SERVER", Sets.newHashSet( new Criteria( "MORE_CPU", true ) ) );
            case STORM_SUPERVISOR:
                return new PlacementStrategy( "BEST_SERVER", Sets.newHashSet( new Criteria( "MORE_RAM", true ) ) );

            default:
                return new PlacementStrategy( "ROUND_ROBIN" );

        }
    }
}
