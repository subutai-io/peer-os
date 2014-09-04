package org.safehaus.subutai.plugin.mahout.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.mahout.api.MahoutClusterConfig;


/**
 * Created by bahadyr on 9/4/14.
 */
public class OverHadoopSetupStrategy extends MahoutSetupStrategy {


    public OverHadoopSetupStrategy( MahoutImpl manager, ProductOperation po, MahoutClusterConfig config ) {
        super( manager, po, config );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException {


        return config;
    }
}
