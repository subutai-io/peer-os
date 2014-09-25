package org.safehaus.subutai.plugin.shark.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.shark.api.SharkClusterConfig;


public class SetupStrategyOverSpark extends SetupStartegyBase implements ClusterSetupStrategy
{

    public SetupStrategyOverSpark( SharkImpl manager, SharkClusterConfig config, ProductOperation po )
    {
        super( manager, config, po );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }


}

