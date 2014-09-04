package org.safehaus.subutai.plugin.mahout.impl;


import org.safehaus.subutai.common.exception.ClusterSetupException;
import org.safehaus.subutai.common.protocol.ConfigBase;
import org.safehaus.subutai.common.tracker.ProductOperation;
import org.safehaus.subutai.plugin.mahout.api.MahoutConfig;


/**
 * Created by bahadyr on 9/4/14.
 */
public class OverHadoopSetupStrategy extends MahoutSetupStrategy {


    public OverHadoopSetupStrategy( MahoutImpl manager, ProductOperation po, MahoutConfig config ) {
        super( manager, po, config );
    }


    @Override
    public ConfigBase setup() throws ClusterSetupException {


        return config;
    }
}
