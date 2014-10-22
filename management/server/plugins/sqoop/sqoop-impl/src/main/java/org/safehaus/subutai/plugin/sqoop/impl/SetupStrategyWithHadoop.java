package org.safehaus.subutai.plugin.sqoop.impl;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentContainer;
import org.safehaus.subutai.plugin.sqoop.api.SqoopConfig;


class SetupStrategyWithHadoop extends SqoopSetupStrategy
{

    Environment environment;


    public SetupStrategyWithHadoop( SqoopImpl manager, SqoopConfig config, TrackerOperation po )
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

        if ( environment.getContainers() == null || environment.getContainers().isEmpty() )
        {
            throw new ClusterSetupException( "Environment has no nodes" );
        }

        Set<Agent> nodes = new HashSet<>(), allNodes = new HashSet<>();
        for ( EnvironmentContainer n : environment.getContainers() )
        {
            allNodes.add( n.getAgent() );
            if ( n.getTemplate().getProducts().contains( CommandFactory.PACKAGE_NAME ) )
            {
                nodes.add( n.getAgent() );
            }
        }
        if ( nodes.isEmpty() )
        {
            throw new ClusterSetupException( "Environment has no nodes with Sqoop installed" );
        }

        config.setNodes( nodes );
        config.setHadoopNodes( allNodes );

        for ( Agent a : config.getNodes() )
        {
            if ( manager.agentManager.getAgentByHostname( a.getHostname() ) == null )
            {
                throw new ClusterSetupException( "Node is not connected: " + a.getHostname() );
            }
        }

        po.addLog( "Saving to db..." );
        manager.getPluginDao().saveInfo( SqoopConfig.PRODUCT_KEY, config.getClusterName(), config );
        po.addLog( "Cluster info successfully saved" );

        return config;
    }
}
