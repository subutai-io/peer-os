package org.safehaus.subutai.plugin.hbase.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;
import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;


/**
 * Created by bahadyr on 8/25/14.
 */
public class HBaseSetupStrategy implements ClusterSetupStrategy {

    private Environment environment;
    private HBaseConfig config;
    private ProductOperation productOperation;
    private HBaseImpl hbase;

    public HBaseSetupStrategy( final Environment environment, final HBaseConfig config, final ProductOperation po,
                               final HBaseImpl hBase ) {
        this.environment = environment;
        this.config = config;
        this.productOperation = po;
        this.hbase = hBase;

    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {
        return config;
    }
}
