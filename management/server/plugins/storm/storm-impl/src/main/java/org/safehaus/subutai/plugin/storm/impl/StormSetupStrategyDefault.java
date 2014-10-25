package org.safehaus.subutai.plugin.storm.impl;


import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentContainer;
import org.safehaus.subutai.plugin.storm.api.StormConfig;
import org.safehaus.subutai.plugin.zookeeper.api.ZookeeperClusterConfig;


public class StormSetupStrategyDefault implements ClusterSetupStrategy
{

    private final StormImpl manager;
    private final StormConfig config;
    private final Environment environment;
    private final TrackerOperation po;


    public StormSetupStrategyDefault( StormImpl manager, StormConfig config, Environment environment,
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
        for ( EnvironmentContainer n : environment.getContainers() )
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
            for ( EnvironmentContainer n : environment.getContainers() )
            {
                if ( n.getNodeGroupName().equals( StormService.NIMBUS.toString() ) )
                {
                    config.setNimbus( n.getAgent() );
                }
            }
        }

        // collect worker nodes in environment
        for ( EnvironmentContainer n : environment.getContainers() )
        {
            if ( n.getNodeGroupName().equals( StormService.SUPERVISOR.toString() ) )
            {
                config.getSupervisors().add( n.getAgent() );
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

        // check if nodes are connected
        String nimbusHostname = config.getNimbus().getHostname();
        if ( manager.agentManager.getAgentByHostname( nimbusHostname ) == null )
        {
            throw new ClusterSetupException( "Nimbus node is not connected" );
        }
        for ( Agent a : config.getSupervisors() )
        {
            if ( manager.agentManager.getAgentByHostname( a.getHostname() ) == null )
            {
                throw new ClusterSetupException( "Not all worker nodes are connected" );
            }
        }

        configure();

        manager.getPluginDao().saveInfo( StormConfig.PRODUCT_NAME, config.getClusterName(), config );
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
        paramValues.put( "nimbus.host", config.getNimbus().getListIP().get( 0 ) );

        Set<Agent> allNodes = new HashSet<>( config.getSupervisors() );
        allNodes.add( config.getNimbus() );

        for ( Map.Entry<String, String> e : paramValues.entrySet() )
        {
            String s = Commands.configure( "add", "storm.xml", e.getKey(), e.getValue() );
            Command cmd = manager.getCommandRunner().createCommand( new RequestBuilder( s ), allNodes );
            manager.getCommandRunner().runCommand( cmd );
            if ( !cmd.hasSucceeded() )
            {
                throw new ClusterSetupException( "Failed to configure: " + cmd.getAllErrors() );
            }
        }
    }


    private String makeZookeeperServersList( StormConfig config )
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
            return config.getNimbus().getListIP().get( 0 );
        }

        return null;
    }
}
