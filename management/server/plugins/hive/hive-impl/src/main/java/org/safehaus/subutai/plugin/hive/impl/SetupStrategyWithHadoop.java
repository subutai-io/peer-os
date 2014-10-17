package org.safehaus.subutai.plugin.hive.impl;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentContainer;
import org.safehaus.subutai.plugin.hive.api.HiveConfig;


class SetupStrategyWithHadoop extends HiveSetupStrategy
{

    Environment environment;


    public SetupStrategyWithHadoop( HiveImpl manager, HiveConfig config, ProductOperation po )
    {
        super( manager, config, po );
    }


    public void setEnvironment( Environment environment )
    {
        this.environment = environment;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {

        checkConfig();

        if ( environment == null )
        {
            throw new ClusterSetupException( "Environment not specified" );
        }

        if ( environment.getEnvironmentContainerNodes() == null || environment.getEnvironmentContainerNodes().isEmpty() )
        {
            throw new ClusterSetupException( "Environment has no nodes" );
        }

        Set<Agent> clients = new HashSet<>(), allNodes = new HashSet<>();
        for ( EnvironmentContainer n : environment.getEnvironmentContainerNodes() )
        {
            allNodes.add( n.getAgent() );
            // if Derby installed on node and server node is not yet set
            if ( n.getTemplate().getProducts().contains( Product.DERBY.getPackageName() ) )
            {
                if ( config.getServer() == null )
                {
                    config.setServer( n.getAgent() );
                    continue;
                }
            }
            if ( n.getTemplate().getProducts().contains( Product.HIVE.getPackageName() ) )
            {
                clients.add( n.getAgent() );
            }
        }
        if ( config.getServer() == null )
        {
            throw new ClusterSetupException( "Environment has no Hive server node" );
        }
        if ( clients.isEmpty() )
        {
            throw new ClusterSetupException( "Environment has no nodes with Hive installed" );
        }

        config.setClients( clients );
        config.setHadoopNodes( allNodes );

        String serverHostname = config.getServer().getHostname();
        if ( manager.agentManager.getAgentByHostname( serverHostname ) == null )
        {
            throw new ClusterSetupException( "Server node is not connected" );
        }
        for ( Agent a : config.getClients() )
        {
            if ( manager.agentManager.getAgentByHostname( a.getHostname() ) == null )
            {
                throw new ClusterSetupException( "Node is not connected: " + a.getHostname() );
            }
        }

        configureServer();
        configureClients();

        po.addLog( "Saving to db..." );
        manager.getPluginDao().saveInfo( HiveConfig.PRODUCT_KEY, config.getClusterName(), config );
        po.addLog( "Cluster info successfully saved" );

        return config;
    }
}
