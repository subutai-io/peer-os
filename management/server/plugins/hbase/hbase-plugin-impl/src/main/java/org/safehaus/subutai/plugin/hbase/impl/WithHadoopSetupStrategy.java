package org.safehaus.subutai.plugin.hbase.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;


/**
 * Created by bahadyr on 9/4/14.
 */
public class WithHadoopSetupStrategy extends HBaseSetupStrategy {

    private Environment environment;


    public WithHadoopSetupStrategy( HBaseImpl manager, ProductOperation po, HBaseClusterConfig config ) {
        super( manager, po, config );
    }


    public Environment getEnvironment() {
        return environment;
    }


    public void setEnvironment( final Environment environment ) {
        this.environment = environment;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException {


        return config;
    }
}
