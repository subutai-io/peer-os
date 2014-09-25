package org.safehaus.subutai.plugin.shark.impl;


import java.util.HashSet;
import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.environment.api.helper.Node;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;


public class SetupStrategyWithHadoopSpark extends SetupStartegyBase implements ClusterSetupStrategy
{
    Environment environment;
    Agent sparkMaster;


    public SetupStrategyWithHadoopSpark( SharkImpl manager, SharkClusterConfig config, ProductOperation po )
    {
        super( manager, config, po );
    }


    public void setEnvironment( Environment environment )
    {
        this.environment = environment;
    }


    public void setSparkMaster( Agent sparkMaster )
    {
        this.sparkMaster = sparkMaster;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {
        checkConfig();

        if ( environment == null )
        {
            throw new ClusterSetupException( "Environment not specified" );
        }
        if ( environment.getNodes() == null || environment.getNodes().isEmpty() )
        {
            throw new ClusterSetupException( "Environment has no nodes" );
        }
        if ( sparkMaster == null )
        {
            throw new ClusterSetupException( "Master node not specified" );
        }

        if ( config.getNodes() == null )
        {
            config.setNodes( new HashSet<Agent>() );
        }
        for ( Node n : environment.getNodes() )
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
        setupMasterIp( sparkMaster );

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

