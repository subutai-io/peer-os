package org.safehaus.subutai.plugin.hbase.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.hbase.api.HBaseClusterConfig;


/**
 * Created by bahadyr on 8/25/14.
 */
public class HBaseSetupStrategy implements ClusterSetupStrategy
{

    HBaseClusterConfig config;
    ProductOperation productOperation;
    HBaseImpl manager;


    public HBaseSetupStrategy( final HBaseImpl manager, final ProductOperation po, final HBaseClusterConfig config )
    {
        this.config = config;
        this.productOperation = po;
        this.manager = manager;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException
    {


        return config;
    }
}
