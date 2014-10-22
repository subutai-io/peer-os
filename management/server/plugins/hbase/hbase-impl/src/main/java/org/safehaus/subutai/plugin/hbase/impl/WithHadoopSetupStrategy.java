package org.safehaus.subutai.plugin.hbase.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.TrackerOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;


public class WithHadoopSetupStrategy extends HBaseSetupStrategy
{

    private Environment environment;


    public WithHadoopSetupStrategy( Environment environment, HBaseImpl manager, TrackerOperation po,
                                    HBaseClusterConfig config )
    {
        super( manager, po, config );
        this.environment = environment;
    }


    public Environment getEnvironment()
    {
        return environment;
    }


    public void setEnvironment( final Environment environment )
    {
        this.environment = environment;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {


        return config;
    }
}
