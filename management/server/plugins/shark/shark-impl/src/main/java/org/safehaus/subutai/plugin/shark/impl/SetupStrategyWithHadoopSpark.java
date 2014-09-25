package org.safehaus.subutai.plugin.shark.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;


public class SetupStrategyWithHadoopSpark extends SetupStartegyBase implements ClusterSetupStrategy
{
    Environment environment;


    public SetupStrategyWithHadoopSpark( SharkImpl manager, SharkClusterConfig config, ProductOperation po )
    {
        super( manager, config, po );
    }


    public Environment getEnvironment()
    {
        return environment;
    }


    public void setEnvironment( Environment environment )
    {
        this.environment = environment;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }


}

