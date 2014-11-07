package org.safehaus.subutai.plugin.storm.impl;


import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.plugin.storm.api.StormClusterConfiguration;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;


public class StormSetupStrategyDefault implements ClusterSetupStrategy
{

    private final StormImpl manager;
    private final StormClusterConfiguration config;
    private final Environment environment;
    private final TrackerOperation po;


    public StormSetupStrategyDefault( StormImpl manager, StormClusterConfiguration config, Environment environment,
                                      TrackerOperation po )
    {
        this.manager = manager;
        this.config = config;
        this.environment = environment;
        this.po = po;
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
                else if ( !config.isExternalZookeeper() )
                {
                    // check nimbus node if embedded Zookeeper is used
                    String zk_pack = Common.PACKAGE_PREFIX + ZookeeperClusterConfig.PRODUCT_NAME;
                    if ( n.getNodeGroupName().equals( StormService.NIMBUS.toString() ) )
                    {
                        if ( !n.getTemplate().getProducts().contains( zk_pack ) )
                        {
                            throw new ClusterSetupException( "Nimbus node does not have Zookeeper installed" );
                        }
                    }
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
            ZookeeperClusterConfig zk = manager.zookeeperManager.getCluster( n );
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

        if ( !containerHost.isConnected() )
        {
            throw new ClusterSetupException( "Nimbus node is not connected" );
        }
        for ( UUID supervisorUuids : config.getSupervisors() )
        {
            if ( ! manager.getEnvironmentManager().getEnvironmentByUUID( config.getEnvironmentId() )
                    .getContainerHostByUUID( supervisorUuids ).isConnected() )
            {
                throw new ClusterSetupException( "Not all worker nodes are connected" );
            }
        }

        configure();

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
        ContainerHost nimbusHost = environment.getContainerHostByUUID( config.getNimbus() );
        paramValues.put( "nimbus.host", nimbusHost.getAgent().getListIP().get( 0 ) );

        Set<UUID> allNodes = new HashSet<>( config.getSupervisors() );
        allNodes.add( config.getNimbus() );

        for ( Map.Entry<String, String> e : paramValues.entrySet() )
        {
            String s = Commands.configure( "add", "storm.xml", e.getKey(), e.getValue() );

            Iterator<UUID> iterator = allNodes.iterator();

            while( iterator.hasNext() ) {
                ContainerHost stormNode = environment.getContainerHostByUUID( iterator.next() );
                try
                {
                    CommandResult commandResult = stormNode.execute( new RequestBuilder( s ).withTimeout( 60 ) );
                    po.addLog( String.format( "Storm %s %s configured on %s", nimbusHost.getNodeGroupName(),
                            commandResult.hasSucceeded() ? "" : "not", stormNode.getHostname() ) );
                }
                catch ( CommandException exception )
                {
                    po.addLogFailed("Failed to configure " + stormNode + ": " + exception );
                    exception.printStackTrace();
                }
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
                for ( Agent a : zk_config.getNodes() )
                {
                    if ( sb.length() > 0 )
                    {
                        sb.append( "," );
                    }
                    sb.append( a.getListIP().get( 0 ) );
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
}
