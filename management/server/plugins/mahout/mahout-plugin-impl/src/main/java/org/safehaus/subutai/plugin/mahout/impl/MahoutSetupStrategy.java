package org.safehaus.subutai.plugin.mahout.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ClusterSetupStrategy;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.mahout.api.MahoutClusterConfig;


/**
 * Created by bahadyr on 8/26/14.
 */
public class MahoutSetupStrategy implements ClusterSetupStrategy {


    public MahoutClusterConfig config;
    private ProductOperation productOperation;
    private MahoutImpl mahout;


    public MahoutSetupStrategy( MahoutImpl mahout, final ProductOperation po, final MahoutClusterConfig config ) {

        this.config = config;
        this.productOperation = po;
        this.mahout = mahout;
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException {
        return config;
    }
}
