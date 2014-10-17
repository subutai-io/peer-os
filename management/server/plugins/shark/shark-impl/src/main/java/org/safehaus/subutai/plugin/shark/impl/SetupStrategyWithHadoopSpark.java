package org.safehaus.subutai.plugin.shark.impl;


import java.util.HashSet;

import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentContainer;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;
import org.safehaus.subutai.plugin.spark.api.SparkClusterConfig;


public class SetupStrategyWithHadoopSpark extends SetupStartegyBase implements ClusterSetupStrategy
{
    Environment environment;


    public SetupStrategyWithHadoopSpark( SharkImpl manager, SharkClusterConfig config, ProductOperation po )
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
        // check underlying Spark cluster
        SparkClusterConfig spark = checkAndGetSparkConfig();
        if ( manager.getAgentManager().getAgentByHostname( spark.getMasterNode().getHostname() ) == null )
        {
            throw new ClusterSetupException( "Spark master is not connected" );
        }

        if ( config.getNodes() == null )
        {
            config.setNodes( new HashSet<Agent>() );
        }
        for ( EnvironmentContainer n : environment.getEnvironmentContainerNodes() )
        {
            if ( n.getTemplate().getProducts().contains( Commands.PACKAGE_NAME ) )
            {
                config.getNodes().add( n.getAgent() );
            }
        }
        if ( config.getNodes().isEmpty() )
        {
            throw new ClusterSetupException( "Environment has no Shark nodes" );
        }

        checkConnected();
        setupMasterIp( spark.getMasterNode() );

        po.addLog( "Saving cluster info..." );
        try
        {
            manager.getPluginDao().saveInfo( SharkClusterConfig.PRODUCT_KEY, config.getClusterName(), config );
            po.addLog( "Cluster info saved to DB" );
        }
        catch ( Exception ex )
        {
            throw new ClusterSetupException( "Failed to save cluster info: " + ex.getMessage() );
        }

        return config;
    }
}

