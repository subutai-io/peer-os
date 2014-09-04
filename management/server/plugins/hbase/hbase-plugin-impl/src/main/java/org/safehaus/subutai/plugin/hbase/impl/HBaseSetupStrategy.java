package org.safehaus.subutai.plugin.hbase.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.hbase.api.HBaseConfig;


/**
 * Created by bahadyr on 8/25/14.
 */
public class HBaseSetupStrategy implements ClusterSetupStrategy {

    HBaseConfig config;
    private ProductOperation productOperation;
    private HBaseImpl hbase;


    public HBaseSetupStrategy( final HBaseImpl hBase, final ProductOperation po, final HBaseConfig config ) {
        this.config = config;
        this.productOperation = po;
        this.hbase = hBase;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException {


        return config;
    }
}
