package org.safehaus.subutai.plugin.hipi.impl;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentContainer;
import org.safehaus.subutai.plugin.hipi.api.HipiConfig;


class WithHadoopSetupStrategy extends HipiSetupStrategy
{

    Environment environment;


    public WithHadoopSetupStrategy( HipiImpl manager, HipiConfig config, TrackerOperation po )
    {
        super( manager, config, po );
    }


    public Environment getEnvironment()
    {
        return environment;
    }


    public void setEnvironment( Environment env )
    {
        this.environment = env;
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

        Set<Agent> hipiNodes = new HashSet<>(), allNodes = new HashSet<>();
        for ( EnvironmentContainer n : environment.getContainers() )
        {
            allNodes.add( n.getAgent() );
            if ( n.getTemplate().getProducts().contains( Commands.PACKAGE_NAME ) )
            {
                hipiNodes.add( n.getAgent() );
            }
        }
        if ( hipiNodes.isEmpty() )
        {
            throw new ClusterSetupException( "Environment has no nodes with Flume installed" );
        }

        config.setNodes( hipiNodes );
        config.setHadoopNodes( allNodes );

        for ( Agent a : config.getNodes() )
        {
            if ( manager.getAgentManager().getAgentByHostname( a.getHostname() ) == null )
            {
                throw new ClusterSetupException( "Node is not connected: " + a.getHostname() );
            }
        }

        trackerOperation.addLog( "Saving to db..." );
        manager.getPluginDao().saveInfo( HipiConfig.PRODUCT_KEY, config.getClusterName(), config );
        trackerOperation.addLog( "Cluster info successfully saved" );

        return config;
    }
}
